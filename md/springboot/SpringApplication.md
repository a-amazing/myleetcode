## SpringApplication构造详解

```java
@SuppressWarnings({ "unchecked", "rawtypes" })
public SpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
    // resourceLoader为null
    this.resourceLoader = resourceLoader;
    Assert.notNull(primarySources, "PrimarySources must not be null");
    // 将传入的DemoApplication启动类放入primarySources中，这样应用就知道主启动类在哪里，叫什么了
    // SpringBoot一般称呼这种主启动类叫primarySource（主配置资源来源）
    this.primarySources = new LinkedHashSet<>(Arrays.asList(primarySources));
    // 3.1 判断当前应用环境
    this.webApplicationType = WebApplicationType.deduceFromClasspath();
    // 3.2 设置初始化器
    setInitializers((Collection) getSpringFactoriesInstances(ApplicationContextInitializer.class));
    // 3.3 设置监听器
    setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));
    // 3.4 确定主配置类
    this.mainApplicationClass = deduceMainApplicationClass();
}
```

---

- WebApplicationType.deduceFromClasspath：判断当前应用环境

  ```java
  //servlet标志类
  private static final String[] SERVLET_INDICATOR_CLASSES = { "javax.servlet.Servlet",
          "org.springframework.web.context.ConfigurableWebApplicationContext" };
  //springmvc环境标志类
  private static final String WEBMVC_INDICATOR_CLASS = "org.springframework." + "web.servlet.DispatcherServlet";
  //webflux环境标志类
  private static final String WEBFLUX_INDICATOR_CLASS = "org." + "springframework.web.reactive.DispatcherHandler";
  //jersey mvc框架标志类
  private static final String JERSEY_INDICATOR_CLASS = "org.glassfish.jersey.servlet.ServletContainer";
  //普通servlet容器
  private static final String SERVLET_APPLICATION_CONTEXT_CLASS = "org.springframework.web.context.WebApplicationContext";
  //响应式servlet容器
  private static final String REACTIVE_APPLICATION_CONTEXT_CLASS = "org.springframework.boot.web.reactive.context.ReactiveWebApplicationContext";
  
  /**
  * 第一个if结构先判断是否是 Reactive 环境，发现有 WebFlux 的类但没有 WebMvc 的类，
  * 则判定为 Reactive 环境（全NIO）
  * 之后的for循环要检查是否有跟 Servlet 相关的类，如果有任何一个类没有，则判定为非Web环境
  * 如果for循环走完了，证明所有类均在当前 classpath 下，则为 Servlet（WebMvc） 环境
  */
  static WebApplicationType deduceFromClasspath() {
      //判断是否响应式环境
      if (ClassUtils.isPresent(WEBFLUX_INDICATOR_CLASS, null) && !ClassUtils.isPresent(WEBMVC_INDICATOR_CLASS, null)
              && !ClassUtils.isPresent(JERSEY_INDICATOR_CLASS, null)) {
          return WebApplicationType.REACTIVE;
      }
      //判断是否非servlet环境(非web应用)
      for (String className : SERVLET_INDICATOR_CLASSES) {
          if (!ClassUtils.isPresent(className, null)) {
              return WebApplicationType.NONE;
          }
      }
      return WebApplicationType.SERVLET;
  }
  ```

---

- setInitializers：设置初始化器

```java
public void setInitializers(Collection<? extends ApplicationContextInitializer<?>> initializers) {
    this.initializers = new ArrayList<>();
    this.initializers.addAll(initializers);
}

/**
*
# Application Context Initializers
org.springframework.context.ApplicationContextInitializer=\
org.springframework.boot.context.ConfigurationWarningsApplicationContextInitializer,\
org.springframework.boot.context.ContextIdApplicationContextInitializer,\
org.springframework.boot.context.config.DelegatingApplicationContextInitializer,\
org.springframework.boot.web.context.ServerPortInfoApplicationContextInitializer
# Initializers
org.springframework.context.ApplicationContextInitializer=\
org.springframework.boot.autoconfigure.SharedMetadataReaderFactoryContextInitializer,\
org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener

一共配置了6个 ApplicationContextInitializer，对这些Initializer作简单介绍：
ConfigurationWarningsApplicationContextInitializer：报告IOC容器的一些常见的错误配置
ContextIdApplicationContextInitializer：设置Spring应用上下文的ID
DelegatingApplicationContextInitializer：加载 application.properties 中 context.initializer.classes 配置的类
ServerPortInfoApplicationContextInitializer：将内置servlet容器实际使用的监听端口写入到 Environment 环境属性中
SharedMetadataReaderFactoryContextInitializer：创建一个 SpringBoot 和 ConfigurationClassPostProcessor 共用的 CachingMetadataReaderFactory 对象
ConditionEvaluationReportLoggingListener：将 ConditionEvaluationReport 写入日志
*
*
*/
private <T> Collection<T> getSpringFactoriesInstances(Class<T> type) {
    return getSpringFactoriesInstances(type, new Class<?>[] {});
}

private <T> Collection<T> getSpringFactoriesInstances(Class<T> type, Class<?>[] parameterTypes, Object... args) {
    ClassLoader classLoader = getClassLoader();
    // Use names and ensure unique to protect against duplicates （使用名称并确保唯一，以防止重复）
    // 3.2.1 SpringFactoriesLoader.loadFactoryNames：加载指定类型的所有已配置组件的全限定类名
    Set<String> names = new LinkedHashSet<>(SpringFactoriesLoader.loadFactoryNames(type, classLoader));
    // 3.2.2 createSpringFactoriesInstances：创建这些组件的实例(通过反射创建实例)
    List<T> instances = createSpringFactoriesInstances(type, parameterTypes, classLoader, args, names);
    AnnotationAwareOrderComparator.sort(instances);
    return instances;
}
```

---

- setListeners：设置监听器

  ```java
  # Application Listeners
  org.springframework.context.ApplicationListener=\
  org.springframework.boot.ClearCachesApplicationListener,\
  org.springframework.boot.builder.ParentContextCloserApplicationListener,\
  org.springframework.boot.context.FileEncodingApplicationListener,\
  org.springframework.boot.context.config.AnsiOutputApplicationListener,\
  org.springframework.boot.context.config.ConfigFileApplicationListener,\
  org.springframework.boot.context.config.DelegatingApplicationListener,\
  org.springframework.boot.context.logging.ClasspathLoggingApplicationListener,\
  org.springframework.boot.context.logging.LoggingApplicationListener,\
  org.springframework.boot.liquibase.LiquibaseServiceLocatorApplicationListener
      
  # Application Listeners
  org.springframework.context.ApplicationListener=\
  org.springframework.boot.autoconfigure.BackgroundPreinitializer
      
  ClearCachesApplicationListener：应用上下文加载完成后对缓存做清除工作
  ParentContextCloserApplicationListener：监听双亲应用上下文的关闭事件并往自己的子应用上下文中传播
  FileEncodingApplicationListener：检测系统文件编码与应用环境编码是否一致，如果系统文件编码和应用环境的编码不同则终止应用启动
  AnsiOutputApplicationListener：根据 spring.output.ansi.enabled 参数配置 AnsiOutput
  ConfigFileApplicationListener：从常见的那些约定的位置读取配置文件
  DelegatingApplicationListener：监听到事件后转发给 application.properties 中配置的 context.listener.classes 的监听器
  ClasspathLoggingApplicationListener：对环境就绪事件 ApplicationEnvironmentPreparedEvent 和应用失败事件 ApplicationFailedEvent 做出响应
  LoggingApplicationListener：配置 LoggingSystem。使用 logging.config 环境变量指定的配置或者缺省配置
  LiquibaseServiceLocatorApplicationListener：使用一个可以和 SpringBoot 可执行jar包配合工作的版本替换 LiquibaseServiceLocator
  BackgroundPreinitializer：使用一个后台线程尽早触发一些耗时的初始化任务
  ```

  