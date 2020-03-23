## 1. WebMvc自动配置装配的核心组件

### 1.1 WebMvcAutoConfiguration

http消息转换器

配置 `Converter`：

```java
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        this.messageConvertersProvider
              .ifAvailable((customConverters) -> converters.addAll(customConverters.getConverters()));
    }
```

`ViewResolver`：

```java
// 最常用的视图解析器
@Bean
public InternalResourceViewResolver defaultViewResolver() {}
@Bean
public BeanNameViewResolver beanNameViewResolver() {}
@Bean
public ContentNegotiatingViewResolver viewResolver(BeanFactory beanFactory) {}
// 国际化组件
@Bean
public LocaleResolver localeResolver() {}
```

静态资源映射，`webjars` 映射：

```java
// 静态资源处理器
public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // ......
    // 映射webjars
    if (!registry.hasMappingForPattern("/webjars/**")) {
        customizeResourceHandlerRegistration(registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/")
                .setCachePeriod(getSeconds(cachePeriod)).setCacheControl(cacheControl));
    }
    // 映射静态资源路径
    String staticPathPattern = this.mvcProperties.getStaticPathPattern();
    if (!registry.hasMappingForPattern(staticPathPattern)) {
        customizeResourceHandlerRegistration(registry.addResourceHandler(staticPathPattern)
                .addResourceLocations(getResourceLocations(this.resourceProperties.getStaticLocations()))
                .setCachePeriod(getSeconds(cachePeriod)).setCacheControl(cacheControl));
    }
}
```

设置 `index.html`：

```java
private Resource getIndexHtml(String location) {
    return this.resourceLoader.getResource(location + "index.html");
}
```

应用图标：

```java
@Bean
public SimpleUrlHandlerMapping faviconHandlerMapping() {
    // ......
    mapping.setUrlMap(Collections.singletonMap("**/favicon.ico", faviconRequestHandler()));
    return mapping;
}
```

### 1.2 DispatcherServletAutoConfiguration

DispatcherServlet：

```java
@Bean(name = DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)
// 向IOC容器中注册前端控制器
public DispatcherServlet dispatcherServlet() {
    DispatcherServlet dispatcherServlet = new DispatcherServlet();
    // ......
    return dispatcherServlet;
}
```

### 1.3 ServletWebServerFactoryAutoConfiguration

TomcatServletWebServerFactory：

```java
@Bean
public TomcatServletWebServerFactory tomcatServletWebServerFactory() {
    return new TomcatServletWebServerFactory();
}
```

WebServerFactoryCustomizerBeanPostProcessor + ErrorPageRegistrarBeanPostProcessor：

```java
public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
        BeanDefinitionRegistry registry) {
    if (this.beanFactory == null) {
        return;
    }
    // 编程式注入组件
    // web服务器工厂后置定制处理器(对tomcat进行参数配置?)
    registerSyntheticBeanIfMissing(registry, "webServerFactoryCustomizerBeanPostProcessor",
            WebServerFactoryCustomizerBeanPostProcessor.class);
    registerSyntheticBeanIfMissing(registry, "errorPageRegistrarBeanPostProcessor",
            ErrorPageRegistrarBeanPostProcessor.class);
}
```

### 1.4 官方文档的说明

> ### 29.1.1 Spring MVC Auto-configuration
>
> Spring Boot provides auto-configuration for Spring MVC that works well with most applications.
>
> The auto-configuration adds the following features on top of Spring’s defaults:
>
> - Inclusion of `ContentNegotiatingViewResolver` and `BeanNameViewResolver` beans.
> - Support for serving static resources, including support for WebJars (covered [later in this document](https://docs.spring.io/spring-boot/docs/2.1.9.RELEASE/reference/htmlsingle/#boot-features-spring-mvc-static-content))).
> - Automatic registration of `Converter`, `GenericConverter`, and `Formatter` beans.
> - Support for `HttpMessageConverters` (covered [later in this document](https://docs.spring.io/spring-boot/docs/2.1.9.RELEASE/reference/htmlsingle/#boot-features-spring-mvc-message-converters)).
> - Automatic registration of `MessageCodesResolver` (covered [later in this document](https://docs.spring.io/spring-boot/docs/2.1.9.RELEASE/reference/htmlsingle/#boot-features-spring-message-codes)).
> - Static `index.html` support.
> - Custom `Favicon` support (covered [later in this document](https://docs.spring.io/spring-boot/docs/2.1.9.RELEASE/reference/htmlsingle/#boot-features-spring-mvc-favicon)).
> - Automatic use of a `ConfigurableWebBindingInitializer` bean (covered [later in this document](https://docs.spring.io/spring-boot/docs/2.1.9.RELEASE/reference/htmlsingle/#boot-features-spring-mvc-web-binding-initializer)).
>
> If you want to keep Spring Boot MVC features and you want to add additional [MVC configuration](https://docs.spring.io/spring/docs/5.1.10.RELEASE/spring-framework-reference/web.html#mvc) (interceptors, formatters, view controllers, and other features), you can add your own `@Configuration` class of type `WebMvcConfigurer` but **without** `@EnableWebMvc`. If you wish to provide custom instances of `RequestMappingHandlerMapping`, `RequestMappingHandlerAdapter`, or `ExceptionHandlerExceptionResolver`, you can declare a `WebMvcRegistrationsAdapter` instance to provide such components.
>
> 如果想在默认配置基础上增加组件,不需要在自己的配置类上`@EnableWebMvc`注解(说明这个注解会标记主配置类?)
>
> If you want to take complete control of Spring MVC, you can add your own `@Configuration` annotated with `@EnableWebMvc`.
>
> 如果你想完全控制SpringMvc,你可以添加一个标记`@EnableWebMvc`注解的 `@Configuration`配置类!

SpringWebMvc的核心是 `DispatcherServlet` ，那对于WebMvc部分咱就着重来看启动、配置，以及与 `DispatcherServlet` 相关的部分。

## 2. 启动应用相关原理

在了解启动原理之前，先来了解一下Servlet3.0的一些规范，这对后续了解 **SpringWebMvc** 和 **SpringBootWebMvc** 有很大帮助。

### 2.1 Servlet3.0规范中引导应用启动的说明

在Servlet3.0的规范文档（小伙伴可点击链接下载：[download.oracle.com/otn-pub/jcp…](https://download.oracle.com/otn-pub/jcp/servlet-3.0-fr-eval-oth-JSpec/servlet-3_0-final-spec.pdf?AuthParam=1571470730_c5c9dee74deeafbfdeb7cb7f87ea17f4），8.2.4章节，有对运行时插件的描述。小册把关键部分的原文引入进来，方便小伙伴们阅读。)

> An instance of the ServletContainerInitializer is looked up via the jar services API by the container at container / application startup time. The framework providing an implementation of the ServletContainerInitializer MUST bundle in the META-INF/services directory of the jar file a file called javax.servlet.ServletContainerInitializer, as per the jar services API, that points to the implementation class of the ServletContainerInitializer.

咱也不贴正儿八经的翻译了，咱用自己的语言描述一下。

在Servlet容器（Tomcat、Jetty等）启动应用时，会扫描应用jar包中 `ServletContainerInitializer` 的实现类。框架必须在jar包的 `META-INF/services` 的文件夹中提供一个名为 `javax.servlet.ServletContainerInitializer` 的文件，文件内容要写明 `ServletContainerInitializer` 的实现类的全限定名。

而这个 `ServletContainerInitializer` 是一个接口，实现它的类必须实现一个方法：`onStartUp` 。

```java
public interface ServletContainerInitializer {
    void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException;
}
```

在这个 `ServletContainerInitializer` 的实现类上标注 `@HandlesTypes` 注解，在应用启动的时候自行加载一些附加的类，这些类会以字节码的集合形式传入 `onStartup` 方法的第一个参数中。

### 2.2 SpringBootServletInitializer的作用和原理

回顾 `SpringBoot` 应用打包启动的两种方式：

- 打jar包启动时，先创建IOC容器，在创建过程中创建了嵌入式Web容器。（详细的jar包启动会在 `JarLauncher` 篇解析）(fatjar,自带web容器)
- 打war包启动时，要先启动外部的Web服务器，Web服务器再去启动 `SpringBoot` 应用，然后才是创建IOC容器。

那么在打war包启动时，里面最核心的步骤：**Web服务器启动SpringBoot应用** 。

而这个步骤，就需要依靠 `SpringBootServletInitializer` 。下面咱来看看外置Web容器是如何成功引导 `SpringBoot` 应用启动的：

1. 外部Web容器（Tomcat、Jetty、Undertow等）启动，开始加载 SpringBoot 的war 包并解压。
2. 去 `SpringBoot` 应用中的每一个被依赖的jar中寻找 `META-INF/services/javax.servlet.SpringBootServletInitializer` 的文件。
3. 根据文件中标注的全限定类名，去找这个类（就是 `SpringServletContainerInitializer`）。
4. 这个类的 onStartup 方法中会将 `@HandlesTypes` 中标注的类型的所有普通实现类（也就是非抽象子类）都实例化出来，之后分别调他们自己的 `onStartup` 方法。

```java
@HandlesTypes(WebApplicationInitializer.class)
public class SpringServletContainerInitializer implements ServletContainerInitializer {
/**
Delegate the ServletContext to any WebApplicationInitializer implementations present on the application classpath. Because this class declares @HandlesTypes(WebApplicationInitializer.class), Servlet 3.0+ containers will automatically scan the classpath for implementations of Spring's WebApplicationInitializer interface and provide the set of all such types to the webAppInitializerClasses parameter of this method. If no WebApplicationInitializer implementations are found on the classpath, this method is effectively a no-op. An INFO-level log message will be issued notifying the user that the ServletContainerInitializer has indeed been invoked but that no WebApplicationInitializer implementations were found. Assuming that one or more WebApplicationInitializer types are detected, they will be instantiated (and sorted if the @@Order annotation is present or the Ordered interface has been implemented). Then the WebApplicationInitializer.onStartup(ServletContext) method will be invoked on each instance, delegating the ServletContext such that each instance may register and configure servlets such as Spring's DispatcherServlet, listeners such as Spring's ContextLoaderListener, or any other Servlet API componentry such as filters.

将 ServletContext 委托给应用程序类路径上存在的任何 WebApplicationInitializer 实现。 因为此类声明了 @HandlesTypes(WebApplicationInitializer.class)，所以 Servlet 3.0+ 容器将自动扫描类路径以查找 Spring 的 WebApplicationInitializer 接口的实现，并将所有此类的类型的集合提供给此方法的 webAppInitializerClasses 参数。 如果在类路径上没有找到 WebApplicationInitializer 实现，则此方法实际上是无操作的。将发出info级别的日志消息，通知用户确实已调用 ServletContainerInitializer，但是未找到 WebApplicationInitializer 实现。 假设检测到一个或多个 WebApplicationInitializer 类型，将对其进行实例化（如果存在 @Order 注解或已实现 Ordered 接口，则将对其进行排序）。然后将在每个实例上调用 WebApplicationInitializer.onStartup(ServletContext) 方法，委派 ServletContext，以便每个实例可以注册和配置 Servlet（例如 Spring 的 DispatcherServlet），监听器（例如 Spring 的 ContextLoaderListener）或任何其他 Servlet API组件（例如Filter）。 
*/
    @Override
    public void onStartup(Set<Class<?>> webAppInitializerClasses, ServletContext servletContext)
            throws ServletException {
        // SpringServletContainerInitializer会加载所有的WebApplicationInitializer类型的普通实现类
        
        List<WebApplicationInitializer> initializers = new LinkedList<WebApplicationInitializer>();

        if (webAppInitializerClasses != null) {
            for (Class<?> waiClass : webAppInitializerClasses) {
                // 如果不是接口，不是抽象类
                if (!waiClass.isInterface() && !Modifier.isAbstract(waiClass.getModifiers()) &&
                    //并且该类是WebApplicationInitializer实现类
                    WebApplicationInitializer.class.isAssignableFrom(waiClass)) {
                    try {
                        // 创建该类的实例
                        initializers.add((WebApplicationInitializer) waiClass.newInstance());
                    }
                    catch (Throwable ex) {
                        throw new ServletException("Failed to instantiate WebApplicationInitializer class", ex);
                    }
                }
            }
        }

        if (initializers.isEmpty()) {
            servletContext.log("No Spring WebApplicationInitializer types detected on classpath");
            return;
        }

        servletContext.log(initializers.size() + " Spring WebApplicationInitializers detected on classpath");
        AnnotationAwareOrderComparator.sort(initializers);
        // 调用各自的onStartup方法
        for (WebApplicationInitializer initializer : initializers) {
            initializer.onStartup(servletContext);
        }
    }
}
```

5. 因为打war包的 `SpringBoot` 工程会在启动类的同包下创建 `ServletInitializer` ，并且必须继承 `SpringBootServletInitializer`，所以会被服务器创建对象。

6. SpringBootServletInitializer` 没有重写 `onStartup` 方法，去父类 `SpringServletContainerInitializer` 中寻找

   - 父类 `SpringServletContainerInitializer` 中的 `onStartup` 方法中有一句核心源码：

   - WebApplicationContextrootAppContext rootAppContext = createRootApplicationContext(servletContext);`

```java
@Override
public void onStartup(ServletContext servletContext) throws ServletException {
    // Logger initialization is deferred in case an ordered
    // LogServletContextInitializer is being used
    this.logger = LogFactory.getLog(getClass());
    // 创建 父IOC容器
    WebApplicationContext rootAppContext = createRootApplicationContext(servletContext);
    if (rootAppContext != null) {
        servletContext.addListener(new ContextLoaderListener(rootAppContext) {
            @Override
            public void contextInitialized(ServletContextEvent event) {
                // no-op because the application context is already initialized
            }
        });
    }
    else {
        this.logger.debug("No ContextLoaderListener registered, as " + "createRootApplicationContext() did not "
                      + "return an application context");
    }
}

protected WebApplicationContext createRootApplicationContext(ServletContext servletContext) {
    // 使用Builder机制，前面也介绍过
    SpringApplicationBuilder builder = createSpringApplicationBuilder();
    builder.main(getClass());
    ApplicationContext parent = getExistingRootWebApplicationContext(servletContext);
    if (parent != null) {
        this.logger.info("Root context already created (using as parent).");
        servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, null);
        builder.initializers(new ParentContextApplicationContextInitializer(parent));
    }
    // 设置Initializer
    builder.initializers(new ServletContextApplicationContextInitializer(servletContext));
    // 在这里设置了容器启动类：AnnotationConfigServletWebServerApplicationContext
    builder.contextClass(AnnotationConfigServletWebServerApplicationContext.class);
    // 【引导】多态进入子类（自己定义）的方法中
    builder = configure(builder);
    builder.listeners(new WebEnvironmentPropertySourceInitializer(servletContext));
    // builder.build()，创建SpringApplication
    SpringApplication application = builder.build();
    if (application.getAllSources().isEmpty()
          && AnnotationUtils.findAnnotation(getClass(), Configuration.class) != null) {
        application.addPrimarySources(Collections.singleton(getClass()));
    }
    Assert.state(!application.getAllSources().isEmpty(),
               "No SpringApplication sources have been defined. Either override the "
               + "configure method or add an @Configuration annotation");
    // Ensure error pages are registered
    if (this.registerErrorPageFilter) {
        application.addPrimarySources(Collections.singleton(ErrorPageFilterConfiguration.class));
    }
    // 启动SpringBoot应用
    return run(application);
}
```

7. 在这个方法中：
   1. 先创建 `SpringApplicationBuilder` 应用构建器；

   2. 再创建一些环境配置；

   3. 下面中间部分有一句： `builder = configure(builder);`

   4. 这句源码由于多态，执行了子类（SpringBoot 工程中必须写的那个启动类的同包下的 `ServletInitializer`）重写的方法；

   5. 又因为重写的格式固定，是传入了 SpringBoot 的目标运行主程序；

      `return builder.sources(DemoApplication.class);`

   6. 所以下一步才能启动 SpringBoot 工程。

8. 之后就跟启动运行主程序 `SpringBootApplication` 没什么区别了。

## 3. @Controller标注的Bean装配MVC原理

做过Controller开发的小伙伴都知道，自己写的Controller类只需要打上 `@Controller` 或 `@RestController` 注解，即可加载到WebMvc中，被 `DispatcherServlet` 找到，这一章节咱来看WebMvc是如何将这些Bean注册到WebMvc中的。

### 3.0 回顾IOC和AOP原理

先回想一下IOC和AOP的几个原理：

- `@Autowired` 是什么时机被解析的：`AutowiredAnnotationBeanPostProcessor` 在 `postProcessMergedBeanDefinition` 中触发。
- 代理对象是什么时机创建的：**Bean的初始化之后，`AnnotationAwareAspectJAutoProxyCreator` 负责创建代理对象** 。

那由此可以猜测，解析 `@Controller` 中 `@RequestMapping` 的时机可能也在这两种情况之内，暂且保存这个猜想。

### 3.1 初始化 RequestMapping 的入口

这个入口讲真一开始我找的时候找了好久，抓后置处理器死活抓不到关键的解析部分，后来我换了一个思路（小伙伴们可以一起来跟我体会一下这个寻找的思路）：解析 `@Controller` 中的所有映射的方法，就是解析被 `@RequestMapping` 标注的方法。之前看 `WebMvcAutoConfiguration` 时又知道注册了一个 `RequestMappingHandlerMapping` 的组件，那估计可以从这个组价中找到一些端倪。

### 3.2 来到RequestMappingHandlerMapping

它实现了 `InitializingBean` ，可是它为什么要这么干呢？咱来进到实现中：

```java
public void afterPropertiesSet() {
    this.config = new RequestMappingInfo.BuilderConfiguration();
    this.config.setUrlPathHelper(getUrlPathHelper());
    this.config.setPathMatcher(getPathMatcher());
    this.config.setSuffixPatternMatch(this.useSuffixPatternMatch);
    this.config.setTrailingSlashMatch(this.useTrailingSlashMatch);
    this.config.setRegisteredSuffixPatternMatch(this.useRegisteredSuffixPatternMatch);
    this.config.setContentNegotiationManager(getContentNegotiationManager());
    super.afterPropertiesSet();
}
```

这里面都是一些设置，不稀奇啊，继续进到父类的 `afterPropertiesSet` 中：

```java
public void afterPropertiesSet() {
    initHandlerMethods();
}
```

### 3.2 initHandlerMethods

```java
private static final String SCOPED_TARGET_NAME_PREFIX = "scopedTarget.";

protected void initHandlerMethods() {
    for (String beanName : getCandidateBeanNames()) {
        if (!beanName.startsWith(SCOPED_TARGET_NAME_PREFIX)) {
            // 只要不是scopedTarget打头的bean,都可能是候选bean
            processCandidateBean(beanName);
        }
    }
    handlerMethodsInitialized(getHandlerMethods());
}
```

可以发现它把IOC容器中所有Bean的名称前缀不是 `"scopedTarget."` 的都拿出来，执行一个 `processCandidateBean` 方法。

### 3.3 processCandidateBean

```java
protected void processCandidateBean(String beanName) {
    Class<?> beanType = null;
    try {
        beanType = obtainApplicationContext().getType(beanName);
    }
    catch (Throwable ex) {
        // An unresolvable bean type, probably from a lazy bean - let's ignore it.
        if (logger.isTraceEnabled()) {
            logger.trace("Could not resolve type for bean '" + beanName + "'", ex);
        }
    }
    // 是否被标注了@Controller注解或者@RequestMapping注解
    if (beanType != null && isHandler(beanType)) {
        detectHandlerMethods(beanName);
    }
}
```

上面的步骤是根据Bean的名称来获取Bean的类型，下面有一个判断：`isHandler`

```java
protected boolean isHandler(Class<?> beanType) {
    return (AnnotatedElementUtils.hasAnnotation(beanType, Controller.class) ||
            AnnotatedElementUtils.hasAnnotation(beanType, RequestMapping.class));
}
```

很明显它要看当前Bean是否有 `@Controller` 或 `@RequestMapping` 标注。

至此发现了重大关键点：**它真的在解析 `@Controller` 和 `@RequestMapping` 了**！

那判断成功后，if中的结构体就一定是解析类中标注了 `@RequestMapping` 的方法了。

### 3.4 detectHandlerMethods

```java
protected void detectHandlerMethods(Object handler) {
    Class<?> handlerType = (handler instanceof String ?
            obtainApplicationContext().getType((String) handler) : handler.getClass());

    if (handlerType != null) {
        Class<?> userType = ClassUtils.getUserClass(handlerType);
        // 3.5 解析筛选方法
        Map<Method, T> methods = MethodIntrospector.selectMethods(userType,
                (MethodIntrospector.MetadataLookup<T>) method -> {
                    try {
                        return getMappingForMethod(method, userType);
                    }
                    catch (Throwable ex) {
                        throw new IllegalStateException("Invalid mapping on handler class [" +
                                userType.getName() + "]: " + method, ex);
                    }
                });
        if (logger.isTraceEnabled()) {
            logger.trace(formatMappings(userType, methods));
        }
        // 3.6 注册方法映射
        methods.forEach((method, mapping) -> {
            Method invocableMethod = AopUtils.selectInvocableMethod(method, userType);
            registerHandlerMethod(handler, invocableMethod, mapping);
        });
    }
}
```

上面的一开始还是拿到这个Bean的类型，下面会使用一个 `MethodInterceptor` 来筛选一些方法。

#### 3.4.0 MethodIntrospector.selectMethods

```java
public static <T> Map<Method, T> selectMethods(Class<?> targetType, final MetadataLookup<T> metadataLookup) {
    final Map<Method, T> methodMap = new LinkedHashMap<>();
    Set<Class<?>> handlerTypes = new LinkedHashSet<>();
    Class<?> specificHandlerType = null;
	
    //如果不是代理类
    if (!Proxy.isProxyClass(targetType)) {
        //实际的Class就是targetType
        specificHandlerType = ClassUtils.getUserClass(targetType);
        handlerTypes.add(specificHandlerType);
    }
    //获取当前类的所有接口?
    handlerTypes.addAll(ClassUtils.getAllInterfacesForClassAsSet(targetType));

    for (Class<?> currentHandlerType : handlerTypes) {
        final Class<?> targetClass = (specificHandlerType != null ? specificHandlerType : currentHandlerType);

        ReflectionUtils.doWithMethods(currentHandlerType, method -> {
            //获取最有可能的方法?(最具体的方法)
            Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
            // 在元数据中寻找方法对应元数据
            T result = metadataLookup.inspect(specificMethod);
            //如果存在
            if (result != null) {
                // 桥接方法是 JDK 1.5 引入泛型后，为了使Java的泛型方法生成的字节码和 1.5 版本前的字节码相兼容，由编译器自动生成的方法。
                // 一个子类在继承（或实现）一个父类（或接口）的泛型方法时，在子类中明确指定了泛型类型，那么在编译时编译器会自动生成桥接方法（当然还有其他情况会生成桥接方法，这里只是列举了其中一种情况）
                Method bridgedMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);
                //
                if (bridgedMethod == specificMethod || metadataLookup.inspect(bridgedMethod) == null) {
                    methodMap.put(specificMethod, result);
                }
            }
        }, ReflectionUtils.USER_DECLARED_METHODS);
    }

    return methodMap;
}
```

核心是中间的 for 循环：它会循环类中所有的方法，并且根据一个 `MetadataLookup` 类型来确定是否可以符合匹配条件。

注意 `MetadataLookup` 是一个函数式接口：

```
@FunctionalInterface
public interface MetadataLookup<T> {
    T inspect(Method method);
}
```

------

回到上面的方法中，筛选方法中传入的 Lambda 表达式如下：

```
    Map<Method, T> methods = MethodIntrospector.selectMethods(userType,
            (MethodIntrospector.MetadataLookup<T>) method -> {
                try {
                    // 3.5
                    return getMappingForMethod(method, userType);
                }
                catch (Throwable ex) {
                    throw new IllegalStateException("Invalid mapping on handler class [" +
                            userType.getName() + "]: " + method, ex);
                }
            });
```

它最终是调 `getMappingForMethod` 方法：