## run()方法详解

- 整体鸟瞰

  ```java
  public ConfigurableApplicationContext run(String... args) {
      // 4.1 创建StopWatch对象
      StopWatch stopWatch = new StopWatch();
      stopWatch.start();
      // 4.2 创建空的IOC容器，和一组异常报告器(空集合)
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
      try {
          // 4.6 如果有配置 spring.beaninfo.ignore，则将该配置设置进系统参数
          configureIgnoreBeanInfo(environment);
          // 4.7 打印SpringBoot的banner
          Banner printedBanner = printBanner(environment);
          // 4.8 创建ApplicationContext
          context = createApplicationContext();
          // 初始化异常报告器
          exceptionReporters = getSpringFactoriesInstances(SpringBootExceptionReporter.class,
                  new Class[] { ConfigurableApplicationContext.class }, context);
          // 4.9 初始化IOC容器
          prepareContext(context, environment, listeners, applicationArguments, printedBanner);
          // ...
  	}
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

  ---
  
- 4.6 configureIgnoreBeanInfo：设置系统参数

  ```java
  public static final String IGNORE_BEANINFO_PROPERTY_NAME = "spring.beaninfo.ignore";
  
  private void configureIgnoreBeanInfo(ConfigurableEnvironment environment) {
      if (System.getProperty(CachedIntrospectionResults.IGNORE_BEANINFO_PROPERTY_NAME) == null) {
          Boolean ignore = environment.getProperty("spring.beaninfo.ignore", Boolean.class, Boolean.TRUE);
          System.setProperty(CachedIntrospectionResults.IGNORE_BEANINFO_PROPERTY_NAME, ignore.toString());
      }
  }
  ```

  > "spring.beaninfo.ignore", with a value of "true" skipping the search for BeanInfo classes (typically for scenarios where no such classes are being defined for beans in the application in the first place).
  >
  > "spring.beaninfo.ignore"` 的值为“true”，则跳过对BeanInfo类的搜索（通常用于未定义此类的情况）首先是应用中的bean）。

---

- 4.7 printBanner：打印Banner

  主要是打印springboot应用启动时的banner图案,不重要,略过

---

- 4.8 createApplicationContext：创建IOC容器

  ```java
  //默认上下文 AnnotationConfigApplicationContext
  public static final String DEFAULT_CONTEXT_CLASS = "org.springframework.context."
          + "annotation.AnnotationConfigApplicationContext";
  //默认servlet上下文 AnnotationConfigServletWebServerApplicationContext
  public static final String DEFAULT_SERVLET_WEB_CONTEXT_CLASS = "org.springframework.boot."
          + "web.servlet.context.AnnotationConfigServletWebServerApplicationContext";
  //默认响应式上下文 AnnotationConfigReactiveWebServerApplicationContext
  public static final String DEFAULT_REACTIVE_WEB_CONTEXT_CLASS = "org.springframework."
          + "boot.web.reactive.context.AnnotationConfigReactiveWebServerApplicationContext";
  
  protected ConfigurableApplicationContext createApplicationContext() {
      Class<?> contextClass = this.applicationContextClass;
      if (contextClass == null) {
          try {
              // 根据Web应用类型决定实例化哪个IOC容器
              switch (this.webApplicationType) {
                  case SERVLET:
                      contextClass = Class.forName(DEFAULT_SERVLET_WEB_CONTEXT_CLASS);
                      break;
                  case REACTIVE:
                      contextClass = Class.forName(DEFAULT_REACTIVE_WEB_CONTEXT_CLASS);
                      break;
                  default:
                      contextClass = Class.forName(DEFAULT_CONTEXT_CLASS);
              }
          }
          catch (ClassNotFoundException ex) {
              throw new IllegalStateException(
                      "Unable create a default ApplicationContext, " + "please specify an ApplicationContextClass",
                      ex);
          }
      }
      //反射创建上下文对象
      return (ConfigurableApplicationContext) BeanUtils.instantiateClass(contextClass);
  }
  ```

  ```java
  //从Context的构造方法可以看到,BeanFactory已经创建完成
  public GenericApplicationContext() {
      this.beanFactory = new DefaultListableBeanFactory();
  }
  ```

  > ​	每一种类型对应一种环境和一个上下文
  >
  > - Servlet - `StandardServletEnvironment` - `AnnotationConfigServletWebServerApplicationContext`
  > - Reactive - `StandardReactiveWebEnvironment` - `AnnotationConfigReactiveWebServerApplicationContext`
  > - None - `StandardEnvironment` - `AnnotationConfigApplicationContext`

---

- 4.9 prepareContext：初始化IOC容器

  ```java
  //根据环境中的属性键值对,准备上下文
  private void prepareContext(ConfigurableApplicationContext context, ConfigurableEnvironment environment,
          SpringApplicationRunListeners listeners, ApplicationArguments applicationArguments, Banner printedBanner) {
      // 将创建好的应用环境设置到IOC容器中
      context.setEnvironment(environment);
      // 4.9.1 IOC容器的后置处理
      postProcessApplicationContext(context);
      // 4.9.2 执行Initializer
      applyInitializers(context);
      // 【回调】SpringApplicationRunListeners的contextPrepared方法（在创建和准备ApplicationContext之后，但在加载之前）
      listeners.contextPrepared(context);
      if (this.logStartupInfo) {
          logStartupInfo(context.getParent() == null);
          logStartupProfileInfo(context);
      }
      // Add boot specific singleton beans
      ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
      // 创建两个组件：在控制台打印Banner的，之前把main方法中参数封装成对象的组件
      beanFactory.registerSingleton("springApplicationArguments", applicationArguments);
      if (printedBanner != null) {
          beanFactory.registerSingleton("springBootBanner", printedBanner);
      }
      if (beanFactory instanceof DefaultListableBeanFactory) {
          ((DefaultListableBeanFactory) beanFactory)
                  .setAllowBeanDefinitionOverriding(this.allowBeanDefinitionOverriding);
      }
      // Load the sources
      // 4.9.3 加载主启动类
      Set<Object> sources = getAllSources();
      Assert.notEmpty(sources, "Sources must not be empty");
      // 4.9.4 注册主启动类
      load(context, sources.toArray(new Object[0]));
      // 【回调】SpringApplicationRunListeners的contextLoaded方法（ApplicationContext已加载但在刷新之前）
      listeners.contextLoaded(context);
  }	
  ```

  - 4.9.1 postProcessApplicationContext：IOC容器的后置处理

    ```java
    // 留意一下这个名，后面Debug的时候会看到
    // 给bean赋一个默认的id?
    public static final String CONFIGURATION_BEAN_NAME_GENERATOR =
    			"org.springframework.context.annotation.internalConfigurationBeanNameGenerator";
    
    protected void postProcessApplicationContext(ConfigurableApplicationContext context) {
        // 注册BeanName生成器
        if (this.beanNameGenerator != null) {
            context.getBeanFactory().registerSingleton(AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR,
                    this.beanNameGenerator);
        }
        // 设置资源加载器和类加载器
        if (this.resourceLoader != null) {
            if (context instanceof GenericApplicationContext) {
                ((GenericApplicationContext) context).setResourceLoader(this.resourceLoader);
            }
            if (context instanceof DefaultResourceLoader) {
                ((DefaultResourceLoader) context).setClassLoader(this.resourceLoader.getClassLoader());
            }
        }
        // 设置类型转换器
        if (this.addConversionService) {
    											    context.getBeanFactory().setConversionService(ApplicationConversionService.getSharedInstance());
        }
    }
    ```

    > - 如果 `beanNameGenerator` 不为空，则把它注册到IOC容器中。 `BeanNameGenerator` 是Bean的name生成器，指定的 `CONFIGURATION_BEAN_NAME_GENERATOR` 在修改首字母大写后无法从IDEA索引到，暂且放置一边。
    > - `ResourceLoader` 和 `ClassLoader`，这些都在前面准备好了
    > - `ConversionService`，用于类型转换的工具，前面也准备好了，并且还做了容器共享

  - 4.9.2 applyInitializers：执行Initializer

    ```java
    //创建 SpringApplication 时准备的那些 ApplicationContextInitializer
    protected void applyInitializers(ConfigurableApplicationContext context) {
        for (ApplicationContextInitializer initializer : getInitializers()) {
            Class<?> requiredType = GenericTypeResolver.resolveTypeArgument(initializer.getClass(),
                    ApplicationContextInitializer.class);
            Assert.isInstanceOf(requiredType, context, "Unable to call initializer.");
            initializer.initialize(context);
        }
    }
    ```

  - 4.9.3 getAllSources

    ```java
     	// prepareContext 的最后几行：
    	// Load the sources
        // 4.9.3 加载主启动类
        Set<Object> sources = getAllSources();
        Assert.notEmpty(sources, "Sources must not be empty");
        // 4.9.4 注册主启动类
        load(context, sources.toArray(new Object[0]));
    
    /************************************************************/
    
    private Set<Class<?>> primarySources;
    private Set<String> sources = new LinkedHashSet<>();
    
    //getAllSources 实际上是把主启动类加载进来了
    public Set<Object> getAllSources() {
        Set<Object> allSources = new LinkedHashSet<>();
        //primarySources不为空,就全部放入set中
        //primarySources 已经被设置过了，就是主启动类
        if (!CollectionUtils.isEmpty(this.primarySources)) {
            allSources.addAll(this.primarySources);
        }
        //本地的source不为空,也全部放入set中
        //debug为空
        if (!CollectionUtils.isEmpty(this.sources)) {
            allSources.addAll(this.sources);
        }
        //不允许增删source?
        return Collections.unmodifiableSet(allSources);
    }
    ```

  - 4.9.4 【复杂】load

    ```java
    protected void load(ApplicationContext context, Object[] sources) {
        // 打日志,忽略
        if (logger.isDebugEnabled()) {
            logger.debug("Loading source " + StringUtils.arrayToCommaDelimitedString(sources));
        }
        // 类定义加载器(找到所有的需要加载的类,并加载为BeanDefinition对象)
        BeanDefinitionLoader loader = createBeanDefinitionLoader(getBeanDefinitionRegistry(context), sources);
        // 设置BeanName生成器，通过Debug发现此时它还没有被注册
        if (this.beanNameGenerator != null) {
            loader.setBeanNameGenerator(this.beanNameGenerator);
        }
        // 设置资源加载器
        if (this.resourceLoader != null) {
            loader.setResourceLoader(this.resourceLoader);
        }
        // 设置运行环境
        if (this.environment != null) {
            loader.setEnvironment(this.environment);
        }
        loader.load();
    }
    ```

    - 4.9.4.1 getBeanDefinitionRegistry

      ```java
      private BeanDefinitionRegistry getBeanDefinitionRegistry(ApplicationContext context) {
          if (context instanceof BeanDefinitionRegistry) {
              return (BeanDefinitionRegistry) context;
          }
          //根据context获取,BeanDefinitionRegistry的实现由BeanFactory还是ApplicationContext真正实现
          if (context instanceof AbstractApplicationContext) {
              return (BeanDefinitionRegistry) ((AbstractApplicationContext) context).getBeanFactory();
          }
          throw new IllegalStateException("Could not locate BeanDefinitionRegistry");
      }
      
      public class AnnotationConfigServletWebServerApplicationContext extends ServletWebServerApplicationContext
      		implements AnnotationConfigRegistry
      public class ServletWebServerApplicationContext extends GenericWebApplicationContext
      		implements ConfigurableWebServerApplicationContext
      public class GenericWebApplicationContext extends GenericApplicationContext
      		implements ConfigurableWebApplicationContext, ThemeSource
      public class GenericApplicationContext extends AbstractApplicationContext implements BeanDefinitionRegistry
      ```

    - 4.9.4.2 createBeanDefinitionLoader

      ```java
      protected BeanDefinitionLoader createBeanDefinitionLoader(BeanDefinitionRegistry registry, Object[] sources) {
          return new BeanDefinitionLoader(registry, sources);
      }
      
      //BeanDefinitionLoader构造方法
      BeanDefinitionLoader(BeanDefinitionRegistry registry, Object... sources) {
          //registry就是applicationContext上下文
          Assert.notNull(registry, "Registry must not be null");
          Assert.notEmpty(sources, "Sources must not be empty");
          this.sources = sources;
          // 注册BeanDefinition解析器,分别从注解和xml中读取类定义
          this.annotatedReader = new AnnotatedBeanDefinitionReader(registry);
          this.xmlReader = new XmlBeanDefinitionReader(registry);
          //如果由Groovy类存在,注册Groovy类定义解析器
          if (isGroovyPresent()) {
              this.groovyReader = new GroovyBeanDefinitionReader(registry);
          }
          //增加类扫描配置,忽略source来源类
    this.scanner = new ClassPathBeanDefinitionScanner(registry);
          this.scanner.addExcludeFilter(new ClassExcludeFilter(sources));
      }
      ```
      
      > 几个关键的组件：`AnnotatedBeanDefinitionReader`（注解驱动的Bean定义解析器）、`XmlBeanDefinitionReader`（Xml定义的Bean定义解析器）、`ClassPathBeanDefinitionScanner`（类路径下的Bean定义扫描器），还有一个我们不用的 `GroovyBeanDefinitionReader`（它需要经过isGroovyPresent方法，而这个方法需要判断classpath下是否有 `groovy.lang.MetaClass` 类）。
      
      - 4.9.4.3 load
      
        ```java
        public int load() {
            int count = 0;
            //其实就是标注了@SpringbootApplication的主启动类
            for (Object source : this.sources) {
                count += load(source);
            }
            return count;
        }
        //拿到所有的 sources（其实就主启动类一个），继续调用重载的load方法
        private int load(Object source) {
            Assert.notNull(source, "Source must not be null");
            // 根据传入source的类型，决定如何解析
            // 主启动类是class类型!
            if (source instanceof Class<?>) {
                return load((Class<?>) source);
            }
            if (source instanceof Resource) {
                return load((Resource) source);
            }
            if (source instanceof Package) {
                return load((Package) source);
            }
            if (source instanceof CharSequence) {
                return load((CharSequence) source);
            }
            throw new IllegalArgumentException("Invalid source type " + source.getClass());
        }
        
        // 根据传入的 source 的类型，来决定用哪种方式加载。主启动类属于 Class 类型，于是继续调用重载的方法
        private int load(Class<?> source) {
            //Groovty相关
            if (isGroovyPresent() && GroovyBeanDefinitionSource.class.isAssignableFrom(source)) {
                // Any GroovyLoaders added in beans{} DSL can contribute beans here
                GroovyBeanDefinitionSource loader = BeanUtils.instantiateClass(source, GroovyBeanDefinitionSource.class);
                load(loader);
            }
            // 如果它是一个Component，则用注解解析器来解析它
            if (isComponent(source)) {
                this.annotatedReader.register(source);
                return 1;
            }
            return 0;
        }
        ```
      
        - 4.9.4.4 annotatedReader.register
      
          ```java
          public void registerBean(Class<?> beanClass) {
              doRegisterBean(beanClass, null, null, null);
          }
          ```
      
        - 4.9.4.5 doRegisterBean
      
          ```java
          <T> void doRegisterBean(Class<T> beanClass, @Nullable Supplier<T> instanceSupplier, @Nullable String name,
                  @Nullable Class<? extends Annotation>[] qualifiers, BeanDefinitionCustomizer... definitionCustomizers) {
          
              // 包装为BeanDefinition
              AnnotatedGenericBeanDefinition abd = new AnnotatedGenericBeanDefinition(beanClass);
              // 条件判断器(盲猜与@Condition注解有关)
              // 如果认为不该存在,则跳过
              if (this.conditionEvaluator.shouldSkip(abd.getMetadata())) {
                  return;
              }
          
              abd.setInstanceSupplier(instanceSupplier);
              // 解析Scope信息，决定作用域
              // ProtoType/singleton 默认单例模式
              ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(abd);
              abd.setScope(scopeMetadata.getScopeName());
              // 生成Bean的名称
              String beanName = (name != null ? name : this.beanNameGenerator.generateBeanName(abd, this.registry));
          
              // 解析BeanDefinition的注解
              AnnotationConfigUtils.processCommonDefinitionAnnotations(abd);
              //判断是立即加载还是懒加载
              if (qualifiers != null) {
                  for (Class<? extends Annotation> qualifier : qualifiers) {
                      if (Primary.class == qualifier) {
                          abd.setPrimary(true);
                      }
                      else if (Lazy.class == qualifier) {
                          abd.setLazyInit(true);
                      }
                      else {
                          abd.addQualifier(new AutowireCandidateQualifier(qualifier));
                      }
                  }
              }
              // 使用定制器修改这个BeanDefinition
              for (BeanDefinitionCustomizer customizer : definitionCustomizers) {
                  customizer.customize(abd);
              }
          
              // 使用BeanDefinitionHolder，将BeanDefinition注册到IOC容器中
              // beanName 和 beanDefinition的键值对?
              BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(abd, beanName);
              // 实施作用域代理规则?配置属性
              definitionHolder = AnnotationConfigUtils.applyScopedProxyMode(scopeMetadata, definitionHolder, this.registry);
              // 将BeanDefinition 绑定到BeanDefinitionRegistry 
            BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, this.registry);
          }
          ```
      
          其中 AnnotationConfigUtils.processCommonDefinitionAnnotations 的实现：
      
          ```java
          // 处理通用类定义注解
          public static void processCommonDefinitionAnnotations(AnnotatedBeanDefinition abd) {
              // 根据通用类定义的元数据(注解?)
              processCommonDefinitionAnnotations(abd, abd.getMetadata());
          }
          
          static void processCommonDefinitionAnnotations(AnnotatedBeanDefinition abd, AnnotatedTypeMetadata metadata) {
              // 解析@Lazy
              AnnotationAttributes lazy = attributesFor(metadata, Lazy.class);
              if (lazy != null) {
                  abd.setLazyInit(lazy.getBoolean("value"));
              }
              else if (abd.getMetadata() != metadata) {
                  lazy = attributesFor(abd.getMetadata(), Lazy.class);
                  if (lazy != null) {
                      abd.setLazyInit(lazy.getBoolean("value"));
                  }
              }
          
              // 解析@Primary
              if (metadata.isAnnotated(Primary.class.getName())) {
                  abd.setPrimary(true);
              }
              // 解析@DependsOn
              AnnotationAttributes dependsOn = attributesFor(metadata, DependsOn.class);
              if (dependsOn != null) {
                  abd.setDependsOn(dependsOn.getStringArray("value"));
              }
          
              // 解析@Role
              AnnotationAttributes role = attributesFor(metadata, Role.class);
              if (role != null) {
                  abd.setRole(role.getNumber("value").intValue());
              }
              // 解析@Description
              AnnotationAttributes description = attributesFor(metadata, Description.class);
              if (description != null) {
                  abd.setDescription(description.getString("value"));
              }
          }
          ```
      
        - 4.9.4.6 BeanDefinitionReaderUtils.registerBeanDefinition
      
          ```java
          public static void registerBeanDefinition(
                  BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry)
                  throws BeanDefinitionStoreException {
          
              // Register bean definition under primary name.
              String beanName = definitionHolder.getBeanName();
              registry.registerBeanDefinition(beanName, definitionHolder.getBeanDefinition());
          
              // Register aliases for bean name, if any.
              // 通过别名也可以找到类定义(根据别名找id)
             	// key:alias value:beanName(id)
              String[] aliases = definitionHolder.getAliases();
              if (aliases != null) {
                  for (String alias : aliases) {
                      registry.registerAlias(beanName, alias);
                  }
              }
          }
          ```
    
  - 4.9.5 【重要】BeanDefinition
  
    > A BeanDefinition describes a bean instance, which has property values, constructor argument values, and further information supplied by concrete implementations. This is just a minimal interface: The main intention is to allow a BeanFactoryPostProcessor such as PropertyPlaceholderConfigurer to introspect and modify property values and other bean metadata.
    >
    > <font color="red">`BeanDefinition`</font> 描述了一个bean实例，该实例具有属性值，构造函数参数值以及具体实现所提供的更多信息。
    >
    > 这只是一个最小的接口：主要目的是允许 <font color="red">`BeanFactoryPostProcessor`</font> （例如 <font color="red">`PropertyPlaceholderConfigurer`</font> ）内省和修改属性值和其他bean元数据。
    
    从文档注释中可以看出，它是描述Bean的实例的一个定义信息，但它不是真正的Bean。这个接口还定义了很多方法：
    
    - `String getBeanClassName();`
    - `String getScope();`
    - `String[] getDependsOn();`
    - `String getInitMethodName();`
    - `boolean isSingleton();`
    - .....