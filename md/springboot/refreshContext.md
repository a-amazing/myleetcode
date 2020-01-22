# refreshContext

> prepareContext(context, environment, listeners, applicationArguments, printedBanner);        
>
> refreshContext(context);        
>
> afterRefresh(context, applicationArguments);

---

0. refreshContext

   ```java
   private void refreshContext(ConfigurableApplicationContext context) {
       refresh(context);
       if (this.registerShutdownHook) {
           try {
               // 注册关闭钩子方法
               context.registerShutdownHook();
           }
           catch (AccessControlException ex) {
               // Not allowed in some environments.
           }
       }
   }
   ```

   它直接调了refresh方法（注意此时还是 SpringApplication，没有进到真正的IOC容器），后面又注册了一个关闭的钩子。这个 registerShutdownHook 方法的文档注释：

   > Register a shutdown hook with the JVM runtime, closing this context on JVM shutdown unless it has already been closed at that time.
   >
   > 向JVM运行时注册一个shutdown的钩子，除非JVM当时已经关闭，否则在JVM关闭时关闭上下文。

   可以大概看出来，这个钩子的作用是监听JVM关闭时销毁IOC容器和里面的Bean。这里面有一个很经典的应用：应用停止时释放数据库连接池里面的连接。

   refresh()方法

   ```java
   protected void refresh(ApplicationContext applicationContext) {
       Assert.isInstanceOf(AbstractApplicationContext.class, applicationContext);
       //调用applicationContext.refresh()
       ((AbstractApplicationContext) applicationContext).refresh();
   }
   ```

   <font color="red">`AbstractApplicationContext`</font> 中的 <font color="red">**refresh**</font> 是IOC容器启动时的最核心方法：

   ```java
   //最终调到AbstractApplicationContext的refresh方法
   public void refresh() throws BeansException, IllegalStateException {
       synchronized (this.startupShutdownMonitor) {
           // Prepare this context for refreshing.
           // 1. 初始化前的预处理
           prepareRefresh();
   
           // Tell the subclass to refresh the internal bean factory.
           // 2. 获取BeanFactory，加载所有bean的定义信息（未实例化）
           ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();
   
           // Prepare the bean factory for use in this context.
           // 3. BeanFactory的预处理配置
           prepareBeanFactory(beanFactory);
   
           try {
               // Allows post-processing of the bean factory in context subclasses.
               // 4. 准备BeanFactory完成后进行的后置处理
               postProcessBeanFactory(beanFactory);
   
               // Invoke factory processors registered as beans in the context.
               // 5. 执行BeanFactory创建后的后置处理器
               invokeBeanFactoryPostProcessors(beanFactory);
   
               // Register bean processors that intercept bean creation.
               // 6. 注册Bean的后置处理器
               registerBeanPostProcessors(beanFactory);
   
               // Initialize message source for this context.
               // 7. 初始化MessageSource
               initMessageSource();
   
               // Initialize event multicaster for this context.
               // 8. 初始化事件派发器
               initApplicationEventMulticaster();
   
               // Initialize other special beans in specific context subclasses.
               // 9. 子类的多态onRefresh
               onRefresh();
   
               // Check for listener beans and register them.
               // 10. 注册监听器
               registerListeners();
             
               //到此为止，BeanFactory已创建完成
   
               // Instantiate all remaining (non-lazy-init) singletons.
               // 11. 初始化所有剩下的单例Bean
               finishBeanFactoryInitialization(beanFactory);
   
               // Last step: publish corresponding event.
               // 12. 完成容器的创建工作
               finishRefresh();
           }
   
           catch (BeansException ex) {
               if (logger.isWarnEnabled()) {
                   logger.warn("Exception encountered during context initialization - " +
                           "cancelling refresh attempt: " + ex);
               }
   
               // Destroy already created singletons to avoid dangling resources.
               destroyBeans();
   
               // Reset 'active' flag.
               cancelRefresh(ex);
   
               // Propagate exception to caller.
               throw ex;
           }
   
           finally {
               // Reset common introspection caches in Spring's core, since we
               // might not ever need metadata for singleton beans anymore...
               // 13. 清除缓存
               resetCommonCaches();
           }
       }
   }
   ```

   ---
   
1. prepareRefresh：初始化前的预处理

   ```java
   protected void prepareRefresh() {
       this.startupDate = System.currentTimeMillis(); // 记录启动时间
       this.closed.set(false); // 标记IOC容器的关闭状态为false
       this.active.set(true); // 标记IOC容器已激活
   
       if (logger.isInfoEnabled()) {
           logger.info("Refreshing " + this);
       }
   
       // Initialize any placeholder property sources in the context environment
       // 1.1 初始化属性配置
       initPropertySources();
   
       // Validate that all properties marked as required are resolvable
       // see ConfigurablePropertyResolver#setRequiredProperties
       // 1.2 属性校验,所有必须属性已经标记为已解决状态
       getEnvironment().validateRequiredProperties();
   
       // Allow for the collection of early ApplicationEvents,
       // to be published once the multicaster is available...
       // 这个集合的作用，是保存容器中的一些事件，以便在合适的时候利用事件广播器来广播这些事件
       // 【配合registerListeners方法中的第三部分使用】
       this.earlyApplicationEvents = new LinkedHashSet<>();
   }
   ```

   1. initPropertySources：初始化属性配置

      ```java
      // 这个方法是一个模板方法，留给子类重写，默认不做任何事情。
      protected void initPropertySources() {
          // For subclasses: do nothing by default.
      }
      
      // 在 GenericWebApplicationContext 中有重写，而 AnnotationConfigServletWebServerApplicationContext 继承了它
      protected void initPropertySources() {
          // 获取环境对象
          ConfigurableEnvironment env = getEnvironment();
          if (env instanceof ConfigurableWebEnvironment) {
              //调用环境对象的初始化属性来源方法
              ((ConfigurableWebEnvironment) env).initPropertySources(this.servletContext, null);
          }
      }
      
      //StandardServletEnvironment 重写方法
      public void initPropertySources(@Nullable ServletContext servletContext, @Nullable ServletConfig servletConfig) {
          //web应用上下文工具.初始化servlet属性
          WebApplicationContextUtils.initServletPropertySources(getPropertySources(), servletContext, servletConfig);
      }
      
      //继续调用,追踪 WebApplicationContextUtils.initServletPropertySources
      //servlet上下文初始化参数
      public static final String SERVLET_CONTEXT_PROPERTY_SOURCE_NAME = "servletContextInitParams";
      //servlet配置初始化参数
      public static final String SERVLET_CONFIG_PROPERTY_SOURCE_NAME = "servletConfigInitParams";
      
      public static void initServletPropertySources(MutablePropertySources sources,
              @Nullable ServletContext servletContext, @Nullable ServletConfig servletConfig) {
      
          Assert.notNull(sources, "'propertySources' must not be null");
          String name = StandardServletEnvironment.SERVLET_CONTEXT_PROPERTY_SOURCE_NAME;
          //存在对应source类
          //把 Servlet 的一些初始化参数放入IOC容器中（类似于 web.xml 中的参数放入IOC容器）。
          if (servletContext != null && sources.contains(name) && sources.get(name) instanceof StubPropertySource) {
              sources.replace(name, new ServletContextPropertySource(name, servletContext));
          }
          name = StandardServletEnvironment.SERVLET_CONFIG_PROPERTY_SOURCE_NAME;
          if (servletConfig != null && sources.contains(name) && sources.get(name) instanceof StubPropertySource) {
              sources.replace(name, new ServletConfigPropertySource(name, servletConfig));
          }
      }
      ```

      ---

   2. validateRequiredProperties：属性校验

      ```java
      // AbstractEnvironment
      public void validateRequiredProperties() throws MissingRequiredPropertiesException {
          this.propertyResolver.validateRequiredProperties();
      }
      
      // AbstractPropertyResolver
      public void validateRequiredProperties() {
          MissingRequiredPropertiesException ex = new MissingRequiredPropertiesException();
          for (String key : this.requiredProperties) {
              // 如果必须属性中有不存在的(null)
              if (this.getProperty(key) == null) {
                  ex.addMissingRequiredProperty(key);
              }
          }
          if (!ex.getMissingRequiredProperties().isEmpty()) {
              throw ex;
          }
      }obtainFreshBeanFactory：获取BeanFactory，加载所有bean的定义信息
      ```

   ---

2. obtainFreshBeanFactory：获取BeanFactory，加载所有bean的定义信息

```java
protected ConfigurableListableBeanFactory obtainFreshBeanFactory() {
    // 2.1 刷新BeanFactory
    refreshBeanFactory();
    // 获取刷新完的beanFactory
    return getBeanFactory();
}
```

1. refreshBeanFactory

   ```java
   // 接口抽象方法
   protected abstract void refreshBeanFactory() throws BeansException, IllegalStateException;

   // GenericApplicationContext 实现
   protected final void refreshBeanFactory() throws IllegalStateException {
       // CAS设置context为已刷新状态
       if (!this.refreshed.compareAndSet(false, true)) {
           throw new IllegalStateException(
                   "GenericApplicationContext does not support multiple refresh attempts: just call 'refresh' once");
       }
       // 设置序列化id
       this.beanFactory.setSerializationId(getId());
   }
   ```
   
2. getBeanFactory

   ```java
   // 获取Bean
   public final ConfigurableListableBeanFactory getBeanFactory() {
       return this.beanFactory;
   }
   ```

---

3.prepareBeanFactory：BeanFactory的预处理配置

```java
protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
    // Tell the internal bean factory to use the context's class loader etc.
    // 设置BeanFactory的类加载器、表达式解析器等
    beanFactory.setBeanClassLoader(getClassLoader());
    beanFactory.setBeanExpressionResolver(new StandardBeanExpressionResolver(beanFactory.getBeanClassLoader()));
    // 资源编辑器是啥玩意(?)
    beanFactory.addPropertyEditorRegistrar(new ResourceEditorRegistrar(this, getEnvironment()));

    // Configure the bean factory with context callbacks.
    // 3.1 配置一个可回调注入ApplicationContext的BeanPostProcessor
    beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));
    // 忽略依赖接口是什么意思(?)
    beanFactory.ignoreDependencyInterface(EnvironmentAware.class);
    beanFactory.ignoreDependencyInterface(EmbeddedValueResolverAware.class);
    beanFactory.ignoreDependencyInterface(ResourceLoaderAware.class);
    beanFactory.ignoreDependencyInterface(ApplicationEventPublisherAware.class);
    beanFactory.ignoreDependencyInterface(MessageSourceAware.class);
    beanFactory.ignoreDependencyInterface(ApplicationContextAware.class);

    // BeanFactory interface not registered as resolvable type in a plain factory.
    // MessageSource registered (and found for autowiring) as a bean.
    // 3.2 自动注入的支持
    beanFactory.registerResolvableDependency(BeanFactory.class, beanFactory);
    beanFactory.registerResolvableDependency(ResourceLoader.class, this);
    beanFactory.registerResolvableDependency(ApplicationEventPublisher.class, this);
    beanFactory.registerResolvableDependency(ApplicationContext.class, this);

    // Register early post-processor for detecting inner beans as ApplicationListeners.
    // 3.3 配置一个可加载所有监听器的组件
    beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(this));

    // Detect a LoadTimeWeaver and prepare for weaving, if found.
    // 加载时间织入bean?(AOP统计bean创建时间?)
    if (beanFactory.containsBean(LOAD_TIME_WEAVER_BEAN_NAME)) {
        beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
        // Set a temporary ClassLoader for type matching.
        beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
    }

    // Register default environment beans.
    // 注册了默认的运行时环境、系统配置属性、系统环境的信息
    if (!beanFactory.containsLocalBean(ENVIRONMENT_BEAN_NAME)) {
        beanFactory.registerSingleton(ENVIRONMENT_BEAN_NAME, getEnvironment());
    }
    if (!beanFactory.containsLocalBean(SYSTEM_PROPERTIES_BEAN_NAME)) {
        beanFactory.registerSingleton(SYSTEM_PROPERTIES_BEAN_NAME, getEnvironment().getSystemProperties());
    }
    if (!beanFactory.containsLocalBean(SYSTEM_ENVIRONMENT_BEAN_NAME)) {
        beanFactory.registerSingleton(SYSTEM_ENVIRONMENT_BEAN_NAME, getEnvironment().getSystemEnvironment());
    }
}
```

3.0 【重要】BeanPostProcessor

> Factory hook that allows for custom modification of new bean instances, e.g. checking for marker interfaces or wrapping them with proxies. ApplicationContexts can autodetect BeanPostProcessor beans in their bean definitions and apply them to any beans subsequently created. Plain bean factories allow for programmatic registration of post-processors, applying to all beans created through this factory. Typically, post-processors that populate beans via marker interfaces or the like will implement postProcessBeforeInitialization, while post-processors that wrap beans with proxies will normally implement postProcessAfterInitialization.
>
> 这个接口允许自定义修改新的Bean的实例，例如检查它们的接口或者将他们包装成代理对象等，
>
> ApplicationContexts能自动察觉到我们在 `BeanPostProcessor` 里对对象作出的改变，并在后来创建该对象时应用其对应的改变。普通的bean工厂允许对后置处理器进行程序化注册，它适用于通过该工厂创建的所有bean。
>
> 通常，通过标记接口等填充bean的后处理器将实现 `postProcessBeforeInitialization`，而使用代理包装bean的后处理器将实现 `postProcessAfterInitialization`。

它可以在对象实例化但初始化之前，以及初始化之后进行一些后置处理。

3.0.2 【执行时机】Bean初始化的顺序及BeanPostProcessor的执行时机

结论:

- 初始化执行顺序：
  - 构造方法
  - `@PostConstruct` / `init-method`
  - `InitializingBean` 的 `afterPropertiesSet` 方法
- BeanPostProcessor的执行时机
  - before：构造方法之后，`@PostConstruct` 之前
  - after：`afterPropertiesSet` 之后

3.1 addBeanPostProcessor(new ApplicationContextAwareProcessor(this));

回到正题,springboot的初始化

```java
   // Configure the bean factory with context callbacks.
    // 3.1 配置一个可回调注入ApplicationContext的BeanPostProcessor
    beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));
    beanFactory.ignoreDependencyInterface(EnvironmentAware.class);
    beanFactory.ignoreDependencyInterface(EmbeddedValueResolverAware.class);
    beanFactory.ignoreDependencyInterface(ResourceLoaderAware.class);
    beanFactory.ignoreDependencyInterface(ApplicationEventPublisherAware.class);
    beanFactory.ignoreDependencyInterface(MessageSourceAware.class);
    beanFactory.ignoreDependencyInterface(ApplicationContextAware.class);
```

配置了一个BeanPostProcessor后又忽略了一些接口

3.1.1 ApplicationContextAwareProcessor

> BeanPostProcessor implementation that passes the ApplicationContext to beans that implement the EnvironmentAware, EmbeddedValueResolverAware, ResourceLoaderAware, ApplicationEventPublisherAware, MessageSourceAware and/or ApplicationContextAware interfaces. Implemented interfaces are satisfied in order of their mention above. Application contexts will automatically register this with their underlying bean factory. Applications do not use this directly.
>
> BeanPostProcessor 实现，它将 ApplicationContext 传递给实现 `EnvironmentAware`，`EmbeddedValueResolverAware`，`ResourceLoaderAware`，`ApplicationEventPublisherAware`，`MessageSourceAware` 和/或 `ApplicationContextAware` 接口的bean。
>
> 按照上面提到的顺序满足已实现的接口。
>
> IOC容器将自动在其基础bean工厂中注册它。应用程序不直接使用它。

```java
public Object postProcessBeforeInitialization(final Object bean, String beanName) throws BeansException {
    AccessControlContext acc = null;

    //如果bean是被忽略的接口中的任意一种的实现类(避免重复注入?)
    if (System.getSecurityManager() != null &&
            (bean instanceof EnvironmentAware || bean instanceof EmbeddedValueResolverAware ||
                    bean instanceof ResourceLoaderAware || bean instanceof ApplicationEventPublisherAware ||
                    bean instanceof MessageSourceAware || bean instanceof ApplicationContextAware)) {
        acc = this.applicationContext.getBeanFactory().getAccessControlContext();
    }

    if (acc != null) {
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            invokeAwareInterfaces(bean);
            return null;
        }, acc);
    }
    else {
        // 往下调用
        invokeAwareInterfaces(bean);
    }

    return bean;
}
//根据接口类型,分别注入需要的属性
private void invokeAwareInterfaces(Object bean) {
    if (bean instanceof Aware) {
        if (bean instanceof EnvironmentAware) {
            ((EnvironmentAware) bean).setEnvironment(this.applicationContext.getEnvironment());
        }
        if (bean instanceof EmbeddedValueResolverAware) {
            ((EmbeddedValueResolverAware) bean).setEmbeddedValueResolver(this.embeddedValueResolver);
        }
        if (bean instanceof ResourceLoaderAware) {
            ((ResourceLoaderAware) bean).setResourceLoader(this.applicationContext);
        }
        if (bean instanceof ApplicationEventPublisherAware) {
            ((ApplicationEventPublisherAware) bean).setApplicationEventPublisher(this.applicationContext);
        }
        if (bean instanceof MessageSourceAware) {
            ((MessageSourceAware) bean).setMessageSource(this.applicationContext);
        }
        if (bean instanceof ApplicationContextAware) {
            ((ApplicationContextAware) bean).setApplicationContext(this.applicationContext);
        }
    }
}
```

---

3.2 registerResolvableDependency：自动注入的支持

```java
    // BeanFactory interface not registered as resolvable type in a plain factory.
    // MessageSource registered (and found for autowiring) as a bean.
	// 简单来说,下面这些bean并没有作为bean注册到beanfactory/applicationcontext,但是又可能遇到需要注入的情况,所以将这些特殊的对象也当成bean加入到resolvableDependencies属性中,即支持自动注入
    beanFactory.registerResolvableDependency(BeanFactory.class, beanFactory);
    beanFactory.registerResolvableDependency(ResourceLoader.class, this);
    beanFactory.registerResolvableDependency(ApplicationEventPublisher.class, this);
    beanFactory.registerResolvableDependency(ApplicationContext.class, this);

// 在DefaultListableBeanFactory 中实现该方法
/** Map from dependency type to corresponding autowired value. */
private final Map<Class<?>, Object> resolvableDependencies = new ConcurrentHashMap<>(16);

/**
Register a special dependency type with corresponding autowired value. This is intended for factory/context references that are supposed to be autowirable but are not defined as beans in the factory: e.g. a dependency of type ApplicationContext resolved to the ApplicationContext instance that the bean is living in. Note: There are no such default types registered in a plain BeanFactory, not even for the BeanFactory interface itself.

用相应的自动装配值注册一个特殊的依赖类型。

这适用于应该是可自动执行但未在工厂中定义为bean的工厂/上下文引用：类型为 ApplicationContext 的依赖关系已解析为Bean所在的 ApplicationContext 实例。

注意：在普通 BeanFactory 中没有注册这样的默认类型，甚至 BeanFactory 接口本身也没有。
*/
public void registerResolvableDependency(Class<?> dependencyType, @Nullable Object autowiredValue) {
    Assert.notNull(dependencyType, "Dependency type must not be null");
    if (autowiredValue != null) {
        if (!(autowiredValue instanceof ObjectFactory || dependencyType.isInstance(autowiredValue))) {
            throw new IllegalArgumentException("Value [" + autowiredValue +
                    "] does not implement specified dependency type [" + dependencyType.getName() + "]");
        }
        this.resolvableDependencies.put(dependencyType, autowiredValue);
    }
}
```

3.3 addBeanPostProcessor(new ApplicationListenerDetector(this))

```java
    // Register early post-processor for detecting inner beans as ApplicationListeners.
    beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(this));

/**
BeanPostProcessor that detects beans which implement the ApplicationListener interface. This catches beans that can't reliably be detected by getBeanNamesForType and related operations which only work against top-level beans.

BeanPostProcessor，用于检测实现 ApplicationListener 接口的bean。这将捕获 getBeanNamesForType 和仅对顶级bean有效的相关操作无法可靠检测到的bean。
*/
ApplicationListenerDetector
    
public Object postProcessAfterInitialization(Object bean, String beanName) {
    //如果bean是Listener
    if (bean instanceof ApplicationListener) {
        // potentially not detected as a listener by getBeanNamesForType retrieval
        Boolean flag = this.singletonNames.get(beanName);
        if (Boolean.TRUE.equals(flag)) {
            // singleton bean (top-level or inner): register on the fly
            this.applicationContext.addApplicationListener((ApplicationListener<?>) bean);
        }
        //非单例情况下,不允许
        else if (Boolean.FALSE.equals(flag)) {
            if (logger.isWarnEnabled() && !this.applicationContext.containsBean(beanName)) {
                // inner bean with other scope - can't reliably process events
                logger.warn("Inner bean '" + beanName + "' implements ApplicationListener interface " +
                        "but is not reachable for event multicasting by its containing ApplicationContext " +
                        "because it does not have singleton scope. Only top-level listener beans are allowed " +
                        "to be of non-singleton scope.");
            }
            this.singletonNames.remove(beanName);
        }
    }
    return bean;
}
```

---

4. postProcessBeanFactory：BeanFactory的后置处理

```java
/** AbstractApplicationContext 中的模板方法 */
protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
}

/** AnnotationConfigServletWebServerApplicationContext 重写 */
protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
    //调用父类方法
    //父类 ServletWebServerApplicationContext 的 postProcessBeanFactory 方法
    super.postProcessBeanFactory(beanFactory);
    // 包扫描
    if (this.basePackages != null && this.basePackages.length > 0) {
        this.scanner.scan(this.basePackages);
    }
    if (!this.annotatedClasses.isEmpty()) {
        // 注册所有被注解标记的类
        this.reader.register(ClassUtils.toClassArray(this.annotatedClasses));
    }
}
```

4.1 ServletWebServerApplicationContext.postProcessBeanFactory

```java
// ServletWebServerApplicationContext
protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
    // 注册ServletContext注入器
    // 专门注入servletContext对象(一个web应用一个context)
    beanFactory.addBeanPostProcessor(new WebApplicationContextServletContextAwareProcessor(this));
    beanFactory.ignoreDependencyInterface(ServletContextAware.class);
    registerWebApplicationScopes();
}
```

4.1.1 注册WebApplicationContextServletContextAwareProcessor

> Variant of ServletContextAwareProcessor for use with a ConfigurableWebApplicationContext. Can be used when registering the processor can occur before the ServletContext or ServletConfig have been initialized.
>
> `ServletContextAwareProcessor` 的扩展，用于 `ConfigurableWebApplicationContext` 。可以在初始化 ServletContext 或 ServletConfig 之前进行处理器注册时使用。



父类ServletContextAwareProcessor相关文档

> BeanPostProcessor implementation that passes the ServletContext to beans that implement the ServletContextAware interface. Web application contexts will automatically register this with their underlying bean factory. Applications do not use this directly.
>
> 将 ServletContext 传递给实现 ServletContextAware 接口的Bean的 BeanPostProcessor 实现。
>
> Web应用程序上下文将自动将其注册到其底层bean工厂，应用程序不直接使用它。



看一下实现方法的源码,分别向ServletContextAware和ServletConfigAware中注入需要的配置对象

```java
public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    if (getServletContext() != null && bean instanceof ServletContextAware) {
        ((ServletContextAware) bean).setServletContext(getServletContext());
    }
    if (getServletConfig() != null && bean instanceof ServletConfigAware) {
        ((ServletConfigAware) bean).setServletConfig(getServletConfig());
    }
    return bean;
}
```

4.1.2 registerWebApplicationScopes

```java
private void registerWebApplicationScopes() {
    // 已存在的web应用范围
    ExistingWebApplicationScopes existingScopes = new ExistingWebApplicationScopes(getBeanFactory());
    WebApplicationContextUtils.registerWebApplicationScopes(getBeanFactory());
    existingScopes.restore();
}
```

4.1.2.1 ExistingWebApplicationScopes

部分源码如下:

```java
public static class ExistingWebApplicationScopes {

    private static final Set<String> SCOPES;

    /**
    联想到jsp的四大域对象
    page
    request
    session
    servletContext
    */
    
    static {
        Set<String> scopes = new LinkedHashSet<>();
        //request域
        scopes.add(WebApplicationContext.SCOPE_REQUEST);
        //session域
        scopes.add(WebApplicationContext.SCOPE_SESSION);
        SCOPES = Collections.unmodifiableSet(scopes);
    }
  
    /**
    把现在缓存的所有作用域，注册到 BeanFactory 中
    
    小册作者的见解:
    将Web的request域和session域注册到IOC容器，让IOC容器知道这两种作用域（学过 SpringFramework 都知道Bean的作用域有request 和 session）。
    */
    public void restore() {
        this.scopes.forEach((key, value) -> {
            if (logger.isInfoEnabled()) {
                logger.info("Restoring user defined scope " + key);
            }
            this.beanFactory.registerScope(key, value);
        });
    }
```

4.1.2.2 WebApplicationContextUtils.registerWebApplicationScopes

```java
/**
* Register web-specific scopes ("request", "session", "globalSession")
* with the given BeanFactory, as used by the WebApplicationContext.

指定的web作用域(request,session,globalSession)注册到给定的被web应用上下文使用beanFactory

* @param beanFactory the BeanFactory to configure
*/
public static void registerWebApplicationScopes(ConfigurableListableBeanFactory beanFactory) {
		registerWebApplicationScopes(beanFactory, null);
}

/**

*/
public static void registerWebApplicationScopes(ConfigurableListableBeanFactory beanFactory,
			@Nullable ServletContext sc) {
    	//String SCOPE_REQUEST = "request";
    	//String SCOPE_SESSION = "session";
		//将request域和session域注册到beanFactory容器中
		beanFactory.registerScope(WebApplicationContext.SCOPE_REQUEST, new RequestScope());
		beanFactory.registerScope(WebApplicationContext.SCOPE_SESSION, new SessionScope());
		if (sc != null) {
			ServletContextScope appScope = new ServletContextScope(sc);
			beanFactory.registerScope(WebApplicationContext.SCOPE_APPLICATION, appScope);
			// Register as ServletContext attribute, for ContextCleanupListener to detect it.
			sc.setAttribute(ServletContextScope.class.getName(), appScope);
		}
		//同理,这边解决以下特殊依赖问题,保证可以获取对象
		beanFactory.registerResolvableDependency(ServletRequest.class, new RequestObjectFactory());
		beanFactory.registerResolvableDependency(ServletResponse.class, new ResponseObjectFactory());
		beanFactory.registerResolvableDependency(HttpSession.class, new SessionObjectFactory());
		beanFactory.registerResolvableDependency(WebRequest.class, new WebRequestObjectFactory());
		if (jsfPresent) {
			FacesDependencyRegistrar.registerFacesDependencies(beanFactory);
		}
	}
```

回到 `AnnotationConfigServletWebServerApplicationContext` 中：

```java
    if (this.basePackages != null && this.basePackages.length > 0) {
        this.scanner.scan(this.basePackages);
    }
```

4.2 【重要】包扫描

```java
private final AnnotatedBeanDefinitionReader reader;
private final ClassPathBeanDefinitionScanner scanner;

/**
在 AnnotationConfigServletWebServerApplicationContext 中有声明 注解Bean定义解析器 和 类路径Bean定义扫描器 的类型，可以依此类型来查看原理。
*/

// ClassPathBeanDefinitionScanner
// 模板方法,真正的实现在doScan里
public int scan(String... basePackages) {
    //先确认已经注册了多少个beanDefinition
    int beanCountAtScanStart = this.registry.getBeanDefinitionCount();

    doScan(basePackages);

    // Register annotation config processors, if necessary.
    if (this.includeAnnotationConfig) {
        AnnotationConfigUtils.registerAnnotationConfigProcessors(this.registry);
    }
	//返回新增的数量
    return (this.registry.getBeanDefinitionCount() - beanCountAtScanStart);
}

//真正进行扫描的方法
protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
    Assert.notEmpty(basePackages, "At least one base package must be specified");
    Set<BeanDefinitionHolder> beanDefinitions = new LinkedHashSet<>();
    // 只有主启动类所在包
    for (String basePackage : basePackages) {
        // 4.2.1 - 4.2.5 扫描包及子包下的组件
        Set<BeanDefinition> candidates = findCandidateComponents(basePackage);
        // 4.3 ......
    }
    return beanDefinitions;
}
```

4.2.1 findCandidateComponents

```java
private CandidateComponentsIndex componentsIndex;
/**
父类 ClassPathScanningCandidateComponentProvider 的 findCandidateComponents 方法
*/
public Set<BeanDefinition> findCandidateComponents(String basePackage) {
    if (this.componentsIndex != null && indexSupportsIncludeFilters()) {
        // 如果由扫描索引并且索引支持include过滤器
        // 走特定扫描
        return addCandidateComponentsFromIndex(this.componentsIndex, basePackage);
    }
    else {
        // 否则正常扫描
        return scanCandidateComponents(basePackage);
    }
}
```

4.2.2 scanCandidateComponents

```java
private Set<BeanDefinition> scanCandidateComponents(String basePackage) {
		Set<BeanDefinition> candidates = new LinkedHashSet<>();
		try {
            //String CLASSPATH_ALL_URL_PREFIX = "classpath*:"; 追加classpath*:前缀
            //resolveBasePackage将包名中的.替换为/
            //static final String DEFAULT_RESOURCE_PATTERN = "**/*.class"; 追加class后缀
            //类路径下当前包路径下所有的class
			String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
					resolveBasePackage(basePackage) + '/' + this.resourcePattern;
			Resource[] resources = getResourcePatternResolver().getResources(packageSearchPath);
			for (Resource resource : resources) {
				if (resource.isReadable()) {
					try {
						MetadataReader metadataReader = getMetadataReaderFactory().getMetadataReader(resource);
                        //获取注解
						if (isCandidateComponent(metadataReader)) {
							ScannedGenericBeanDefinition sbd = new ScannedGenericBeanDefinition(metadataReader);
							sbd.setResource(resource);
							sbd.setSource(resource);
                            //如果是包扫描对象(@Component,@Controller,@Service,@Repository,@Configuration...)
							if (isCandidateComponent(sbd)) {
								candidates.add(sbd);
							}
						}
					}
					catch (Throwable ex) {
						throw new BeanDefinitionStoreException(
								"Failed to read candidate component class: " + resource, ex);
					}
				}
			}
		}
		catch (IOException ex) {
			throw new BeanDefinitionStoreException("I/O failure during classpath scanning", ex);
		}
		return candidates;
	}
```

4.2.3 getResourcePatternResolver

```java
private ResourcePatternResolver getResourcePatternResolver() {
    if (this.resourcePatternResolver == null) {
        //PathMatchingResourcePatternResolver.class
        this.resourcePatternResolver = new PathMatchingResourcePatternResolver();
    }
    return this.resourcePatternResolver;
}
```

4.2.4 getResources：包扫描

```java
String CLASSPATH_ALL_URL_PREFIX = "classpath*:";

public Resource[] getResources(String locationPattern) throws IOException {
    Assert.notNull(locationPattern, "Location pattern must not be null");
    //首先必须以classpath*:开头
    if (locationPattern.startsWith(CLASSPATH_ALL_URL_PREFIX)) {
        // a class path resource (multiple resources for same name possible)
        //开始匹配去除classpath*:头部分
        if (getPathMatcher().isPattern(locationPattern.substring(CLASSPATH_ALL_URL_PREFIX.length()))) {
            // a class path resource pattern
            return findPathMatchingResources(locationPattern);
        }
        else {
            // all class path resources with the given name
            return findAllClassPathResources(locationPattern.substring(CLASSPATH_ALL_URL_PREFIX.length()));
        }
    }
    else {
        // Generally only look for a pattern after a prefix here,
        // and on Tomcat only after the "*/" separator for its "war:" protocol.
        // war:开头的war包
        int prefixEnd = (locationPattern.startsWith("war:") ? locationPattern.indexOf("*/") + 1 :
                locationPattern.indexOf(':') + 1);
        if (getPathMatcher().isPattern(locationPattern.substring(prefixEnd))) {
            // a file pattern
            return findPathMatchingResources(locationPattern);
        }
        else {
            // a single resource with the given name
            return new Resource[] {getResourceLoader().getResource(locationPattern)};
        }
    }
}
```

PathMatcher只存在一个实现类 AntPathMatcher,即`SpringFramework` 支持的是ant规则声明包。

4.2.4.1 findPathMatchingResources：根据Ant路径进行包扫描

```java
protected Resource[] findPathMatchingResources(String locationPattern) throws IOException {
    // 4.2.4.1.1 截取扫描根路径
    String rootDirPath = determineRootDir(locationPattern);
    // 截取剩下扫描路径（**/*.class）
    String subPattern = locationPattern.substring(rootDirPath.length());
    // 4.2.4.2,3 获取扫描包路径下的所有包
    Resource[] rootDirResources = getResources(rootDirPath);
    Set<Resource> result = new LinkedHashSet<>(16);
    for (Resource rootDirResource : rootDirResources) {
        rootDirResource = resolveRootDirResource(rootDirResource);
        URL rootDirUrl = rootDirResource.getURL();
        if (equinoxResolveMethod != null && rootDirUrl.getProtocol().startsWith("bundle")) {
            URL resolvedUrl = (URL) ReflectionUtils.invokeMethod(equinoxResolveMethod, null, rootDirUrl);
            if (resolvedUrl != null) {
                rootDirUrl = resolvedUrl;
            }
            rootDirResource = new UrlResource(rootDirUrl);
        }
        if (rootDirUrl.getProtocol().startsWith(ResourceUtils.URL_PROTOCOL_VFS)) {
            result.addAll(VfsResourceMatchingDelegate.findMatchingResources(rootDirUrl, subPattern, getPathMatcher()));
        }
        else if (ResourceUtils.isJarURL(rootDirUrl) || isJarResource(rootDirResource)) {
            result.addAll(doFindPathMatchingJarResources(rootDirResource, rootDirUrl, subPattern));
        }
        else {
            result.addAll(doFindPathMatchingFileResources(rootDirResource, subPattern));
        }
    }
    if (logger.isTraceEnabled()) {
        logger.trace("Resolved location pattern [" + locationPattern + "] to resources " + result);
    }
    return result.toArray(new Resource[0]);
}
```

截取跟路径

```java
protected String determineRootDir(String location) {
    //classpath*:com/example/demo/*/*.class
    // 第一个:的索引值
    int prefixEnd = location.indexOf(':') + 1;
	// 字符串长度
    int rootDirEnd = location.length();
    // 
    while (rootDirEnd > prefixEnd && getPathMatcher().isPattern(location.substring(prefixEnd, rootDirEnd))) {
        //rootDirEnd -1 代表最后一位索引值,为什么 -2(最后一位允许是/)
        rootDirEnd = location.lastIndexOf('/', rootDirEnd - 2) + 1;
    }
    if (rootDirEnd == 0) {
        // 0 -> 1
        rootDirEnd = prefixEnd;
    }
    
    return location.substring(0, rootDirEnd);
}
```

```java
// 4.2.4.1.1 截取扫描根路径
String rootDirPath = determineRootDir(locationPattern);
// 截取剩下扫描路径（**/*.class）
String subPattern = locationPattern.substring(rootDirPath.length());
Resource[] rootDirResources = getResources(rootDirPath);

/**
截取完成后，又把根路径传入了那个 getResources 方法。
由于这一次没有后缀了，只有根路径，故进入的分支方法会不一样
classpath*:com/example/demo/
*/
public Resource[] getResources(String locationPattern) throws IOException {
    Assert.notNull(locationPattern, "Location pattern must not be null");
    if (locationPattern.startsWith(CLASSPATH_ALL_URL_PREFIX)) {
        // a class path resource (multiple resources for same name possible)
        if (getPathMatcher().isPattern(locationPattern.substring(CLASSPATH_ALL_URL_PREFIX.length()))) {
            // a class path resource pattern
            return findPathMatchingResources(locationPattern);
        }
        else {
            // ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓
            // all class path resources with the given name
            return findAllClassPathResources(locationPattern.substring(CLASSPATH_ALL_URL_PREFIX.length()));
        }
    }
    // ...
```

4.2.4.2 findAllClassPathResources

```java
protected Resource[] findAllClassPathResources(String location) throws IOException {
    String path = location;
    if (path.startsWith("/")) {
        //去除开头的/
        path = path.substring(1);
    }
    //                     ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓
    Set<Resource> result = doFindAllClassPathResources(path);
    if (logger.isTraceEnabled()) {
        logger.trace("Resolved classpath location [" + location + "] to resources " + result);
    }
    return result.toArray(new Resource[0]);
}
```

