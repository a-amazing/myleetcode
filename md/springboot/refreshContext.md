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
      protected abstract void refreshBeanFactory() throws BeansException, IllegalStateException;
      ```

      