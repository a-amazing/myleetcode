## run()方法详解

- 整体鸟瞰

  ```java
  public ConfigurableApplicationContext run(String... args) {
      // 4.1 创建StopWatch对象
      StopWatch stopWatch = new StopWatch();
      stopWatch.start();
      // 4.2 创建空的IOC容器，和一组异常报告器
      ConfigurableApplicationContext context = null;
      Collection<SpringBootExceptionReporter> exceptionReporters = new ArrayList<>();
      // 4.3 配置与awt相关的信息
      configureHeadlessProperty();
      // 4.4 获取SpringApplicationRunListeners，并调用starting方法（回调机制）
      SpringApplicationRunListeners listeners = getRunListeners(args);
      // 【回调】首次启动run方法时立即调用。可用于非常早期的初始化（准备运行时环境之前）。
      listeners.starting();
      try {
          // 将main方法的args参数封装到一个对象中
          ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
          // 4.5 准备运行时环境
          ConfigurableEnvironment environment = prepareEnvironment(listeners, applicationArguments);
          //.............
      }
  ```

  ---

- 4.1 new StopWatch()：创建StopWatch对象

  ```java
  /**
  * 用于开发时监控启动性能
  */
  public void start() throws IllegalStateException {
      start("");
  }
  
  public void start(String taskName) throws IllegalStateException {
      if (this.currentTaskName != null) {
          throw new IllegalStateException("Can't start StopWatch: it's already running");
      }
      this.currentTaskName = taskName;
      // 记录启动时的当前系统时间
      this.startTimeMillis = System.currentTimeMillis();
  }
  ```

  ---

- 4.2 创建空的IOC容器，和一组异常报告器

  ```java
  ConfigurableApplicationContext context = null;
  //一组异常报告器
  Collection<SpringBootExceptionReporter> exceptionReporters = new ArrayList<>();
  ```

  - SpringBootExceptionReporter

    ```java
    /**
    * 只有一个方法(报告异常)的接口
    */
    public interface SpringBootExceptionReporter {
        boolean reportException(Throwable failure);
    }
    ```

---

- 4.3 configureHeadlessProperty：设置awt相关(***java.awt?图形工具?spring还需要调用这玩意?***)

  ```java
  private void configureHeadlessProperty() {
      //从 System 中取了一个配置，又给设置回去了
      //SYSTEM_PROPERTY_JAVA_AWT_HEADLESS
      //private static final String SYSTEM_PROPERTY_JAVA_AWT_HEADLESS = "java.awt.headless"; ——显示器缺失
      //即显示器缺失也可以正常运行(服务端)
      System.setProperty(SYSTEM_PROPERTY_JAVA_AWT_HEADLESS,
              System.getProperty(SYSTEM_PROPERTY_JAVA_AWT_HEADLESS, Boolean.toString(this.headless)));
  }
  ```

  ---

- 4.4 getRunListeners：获取SpringApplicationRunListeners

  ```java
  //获取所有spring状态变更监听器,所以自定义的监听器怎么配置进去(服务启动监听)
  private SpringApplicationRunListeners getRunListeners(String[] args) {
      Class<?>[] types = new Class<?>[] { SpringApplication.class, String[].class };
      // 又是调getSpringFactoriesInstances方法，取spring.factories中所有SpringApplicationRunListener
      return new SpringApplicationRunListeners(logger,
              getSpringFactoriesInstances(SpringApplicationRunListener.class, types, this, args));
  }
  //默认加载EventPublishingRunListener(spring事件发布监听器)
  // listeners.starting(); //【回调】首次启动run方法时立即调用。可用于非常早期的初始化（准备运行时环境之前）。接下来马上就发布启动事件
  ```

  ---

- 4.5 prepareEnvironment：准备运行时环境

  ```java
  private ConfigurableEnvironment prepareEnvironment(SpringApplicationRunListeners listeners,
          ApplicationArguments applicationArguments) {
      // Create and configure the environment
      // 4.5.1 创建运行时环境
      ConfigurableEnvironment environment = getOrCreateEnvironment();
      // 4.5.2 配置运行时环境
      configureEnvironment(environment, applicationArguments.getSourceArgs());
      // 【回调】SpringApplicationRunListener的environmentPrepared方法（Environment构建完成，但在创建ApplicationContext之前）
      listeners.environmentPrepared(environment);
      // 4.5.3 环境与应用绑定
      bindToSpringApplication(environment);
      if (!this.isCustomEnvironment) {
          environment = new EnvironmentConverter(getClassLoader()).convertEnvironmentIfNecessary(environment,
                  deduceEnvironmentClass());
      }
      ConfigurationPropertySources.attach(environment);
      return environment;
  }
  ```

  - 4.5.1 getOrCreateEnvironment：创建运行时环境

  ```java
  private ConfigurableEnvironment getOrCreateEnvironment() {
      //如果已经创建,直接返回
      if (this.environment != null) {
          return this.environment;
      }
      // 判断当前Web应用类型
      switch (this.webApplicationType) {
          case SERVLET:
              return new StandardServletEnvironment();
          case REACTIVE:
              return new StandardReactiveWebEnvironment();
          default:
              return new StandardEnvironment();
      }
  }
  ```

  - 4.5.2 configureEnvironment：配置运行时环境

  ```java
  protected void configureEnvironment(ConfigurableEnvironment environment, String[] args) {
      if (this.addConversionService) {
          ConversionService conversionService = ApplicationConversionService.getSharedInstance();
          environment.setConversionService((ConfigurableConversionService) conversionService);
      }
      configurePropertySources(environment, args);
      configureProfiles(environment, args);
  }
  
  //关于ConversionService(类型转换服务接口)
  /**
  DefaultConversionService 默认实现类
      StringToNumberConverterFactory
      StringToBooleanConverter
      IntegerToEnumConverterFactory
      ArrayToCollectionConverter
      StringToArrayConverter
      ......
      
  在 SpringWebMvc 中做参数类型转换
  */
  ```

  ---

- 4.5.3 bindToSpringApplication：环境与应用绑定

  ```java
  protected void bindToSpringApplication(ConfigurableEnvironment environment) {
      try {
          Binder.get(environment).bind("spring.main", Bindable.ofInstance(this));
      }
      catch (Exception ex) {
          throw new IllegalStateException("Cannot bind to SpringApplication", ex);
      }
  }
  
  public <T> BindResult<T> bind(String name, Bindable<T> target) {
      return bind(ConfigurationPropertyName.of(name), target, null);
  }
  
  public <T> BindResult<T> bind(ConfigurationPropertyName name, Bindable<T> target, BindHandler handler) {
      Assert.notNull(name, "Name must not be null");
      Assert.notNull(target, "Target must not be null");
      handler = (handler != null) ? handler : BindHandler.DEFAULT;
      Context context = new Context();
      T bound = bind(name, target, handler, context, false);
      return BindResult.of(bound);
  }
  ```

  