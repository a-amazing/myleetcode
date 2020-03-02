```java
public void refresh() throws BeansException, IllegalStateException {
    synchronized (this.startupShutdownMonitor) {
        // ...
        try {
            // ...
            // Initialize other special beans in specific context subclasses.
            // 9. 子类的多态onRefresh
            onRefresh();
            // ...
            // Last step: publish corresponding event.
            // 12. 完成容器的创建工作
            finishRefresh();
        }
        catch (BeansException ex) {
            // ...
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

## 12. finishRefresh：完成容器的创建工作

```java
protected void finishRefresh() {
    // Clear context-level resource caches (such as ASM metadata from scanning).
    // 清除资源缓存(如扫描的ASM元数据)
    clearResourceCaches();

    // Initialize lifecycle processor for this context.
    // 初始化生命周期处理器
    initLifecycleProcessor();

    // Propagate refresh to lifecycle processor first.
    // 将刷新传播到生命周期处理器
    getLifecycleProcessor().onRefresh();

    // Publish the final event.
    // 发布容器刷新完成的事件，让监听器去回调各自的方法
    publishEvent(new ContextRefreshedEvent(this));

    // Participate in LiveBeansView MBean, if active.
    LiveBeansView.registerApplicationContext(this);
}
```

### 12.1 clearResourceCaches：清除资源缓存

```java
public void clearResourceCaches() {
    this.resourceCaches.clear();
}
```

### 12.2 initLifecycleProcessor：初始化生命周期处理器

```java
public static final String LIFECYCLE_PROCESSOR_BEAN_NAME = "lifecycleProcessor";

// 默认使用DefaultLifecycleProcessor
protected void initLifecycleProcessor() {
    ConfigurableListableBeanFactory beanFactory = getBeanFactory();
    if (beanFactory.containsLocalBean(LIFECYCLE_PROCESSOR_BEAN_NAME)) {
        this.lifecycleProcessor =
                beanFactory.getBean(LIFECYCLE_PROCESSOR_BEAN_NAME, LifecycleProcessor.class);
        if (logger.isTraceEnabled()) {
            logger.trace("Using LifecycleProcessor [" + this.lifecycleProcessor + "]");
        }
    }
    else {
        // 默认走这条分支,说明没有配置生命周期处理器
        DefaultLifecycleProcessor defaultProcessor = new DefaultLifecycleProcessor();
        defaultProcessor.setBeanFactory(beanFactory);
        this.lifecycleProcessor = defaultProcessor;
        beanFactory.registerSingleton(LIFECYCLE_PROCESSOR_BEAN_NAME, this.lifecycleProcessor);
        if (logger.isTraceEnabled()) {
            logger.trace("No '" + LIFECYCLE_PROCESSOR_BEAN_NAME + "' bean, using " +
                    "[" + this.lifecycleProcessor.getClass().getSimpleName() + "]");
        }
    }
}
```

> Default implementation of the LifecycleProcessor strategy.
>
> LifecycleProcessor: Strategy interface for processing Lifecycle beans within the ApplicationContext.
>
> 用于在 `ApplicationContext` 中处理 `Lifecycle` 类型的Bean的策略接口。

注意LifeCycle(生命周期)的概念

#### 12.2.1 LifeCycle

`Lifecycle` 是一个接口，它的文档注释原文翻译：

> A common interface defining methods for start/stop lifecycle control. The typical use case for this is to control asynchronous processing. NOTE: This interface does not imply specific auto-startup semantics. Consider implementing SmartLifecycle for that purpose. Can be implemented by both components (typically a Spring bean defined in a Spring context) and containers (typically a Spring ApplicationContext itself). Containers will propagate start/stop signals to all components that apply within each container, e.g. for a stop/restart scenario at runtime. Can be used for direct invocations or for management operations via JMX. In the latter case, the org.springframework.jmx.export.MBeanExporter will typically be defined with an org.springframework.jmx.export.assembler.InterfaceBasedMBeanInfoAssembler, restricting the visibility of activity-controlled components to the Lifecycle interface. Note that the present Lifecycle interface is only supported on top-level singleton beans. On any other component, the Lifecycle interface will remain undetected and hence ignored. Also, note that the extended SmartLifecycle interface provides sophisticated integration with the application context's startup and shutdown phases.
>
> 定义启动/停止生命周期控制方法的通用接口。典型的用例是控制异步处理。注意：此接口并不意味着特定的自动启动语义。考虑为此目的实施 SmartLifecycle。
>
> 可以通过组件（通常是在Spring上下文中定义的 `Spring` bean）和容器（通常是Spring `ApplicationContext` 本身）实现。容器会将开始/停止信号传播到每个容器中应用的所有组件，例如在运行时停止/重新启动的情况。
>
> 可以用于直接调用或通过JMX进行管理操作。在后一种情况下，通常将使用 `InterfaceBasedMBeanInfoAssembler` 定义 `MBeanExporter`，从而将活动控制的组件的可见性限制为 `Lifecycle` 接口。
>
> 请注意，当前的 `Lifecycle` 接口仅在顶级 `Singleton Bean` 上受支持。在任何其他组件上，`Lifecycle` 接口将保持未被检测到并因此被忽略。另外，请注意，扩展的 `SmartLifecycle` 接口提供了与应用程序上下文的启动和关闭阶段的复杂集成。

#### 12.2.2 【扩展】SmartLifeCycle

`Lifecycle` 还有一个扩展的接口：`SmartLifecycle` ，它的文档注释关键部分：

> An extension of the Lifecycle interface for those objects that require to be started upon ApplicationContext refresh and/or shutdown in a particular order. The isAutoStartup() return value indicates whether this object should be started at the time of a context refresh. The callback-accepting stop(Runnable) method is useful for objects that have an asynchronous shutdown process. Any implementation of this interface must invoke the callback's run() method upon shutdown completion to avoid unnecessary delays in the overall ApplicationContext shutdown.
>
> `Lifecycle` 接口的扩展，用于那些需要按特定顺序刷新和/或关闭IOC容器时启动的对象。 `isAutoStartup()` 返回值指示是否应在刷新上下文时启动此对象。接受回调的 `stop(Runnable)` 方法对于具有异步关闭过程的对象很有用。此接口的任何实现都必须在关闭完成时调用回调的 `run()` 方法，以避免在整个IOC容器关闭中不必要的延迟。

`stop(Runnable)`方法 ，这就意味着可以在 `stop` 动作中再注入一些自定义逻辑。从它的方法定义中，可以看到它还扩展了几个方法：

- `getPhase` - Bean的排序（类似于 `@Order` 或 `Ordered` 接口）
- `isAutoStartup` - 如果该方法返回 false ，则不执行 start 方法。

### 12.3 getLifecycleProcessor().onRefresh()

```java
public void onRefresh() {
    startBeans(true);
    this.running = true;
}

private void startBeans(boolean autoStartupOnly) {
    Map<String, Lifecycle> lifecycleBeans = getLifecycleBeans();
    Map<Integer, LifecycleGroup> phases = new HashMap<>();
    lifecycleBeans.forEach((beanName, bean) -> {
        if (!autoStartupOnly || (bean instanceof SmartLifecycle && ((SmartLifecycle) bean).isAutoStartup())) {
            int phase = getPhase(bean);
            LifecycleGroup group = phases.get(phase);
            if (group == null) {
                group = new LifecycleGroup(phase, this.timeoutPerShutdownPhase, lifecycleBeans, autoStartupOnly);
                phases.put(phase, group);
            }
            group.add(beanName, bean);
        }
    });
    if (!phases.isEmpty()) {
        List<Integer> keys = new ArrayList<>(phases.keySet());
        Collections.sort(keys);
        // 执行所有bean的start方法
        for (Integer key : keys) {
            phases.get(key).start();
        }
    }
}
```

### 12.4 publishEvent(new ContextRefreshedEvent(this))

发布了 `ContextRefreshedEvent` 事件，代表IOC容器已经刷新完成。

## 13. resetCommonCaches：清除缓存

```java
protected void resetCommonCaches() {
    ReflectionUtils.clearCache();
    AnnotationUtils.clearCache();
    ResolvableType.clearCache();
    CachedIntrospectionResults.clearClassLoader(getClassLoader());
}
```

## 9. ServletWebServerApplicationContext.onRefresh

SpringBoot 扩展的IOC容器中对这个方法进行了真正地实现

```java
protected void onRefresh() {
    super.onRefresh();
    try {
        createWebServer();
    }
    catch (Throwable ex) {
        throw new ApplicationContextException("Unable to start web server", ex);
    }
}
```

创建一个web服务器(内嵌的web服务器)

```java
private void createWebServer() {
    WebServer webServer = this.webServer;
    ServletContext servletContext = getServletContext();
    if (webServer == null && servletContext == null) {
        // 9.1 这一步创建了嵌入式Servlet容器的工厂
        ServletWebServerFactory factory = getWebServerFactory();
        // 9.2 创建嵌入式Servlet容器
        this.webServer = factory.getWebServer(getSelfInitializer());
    }
    else if (servletContext != null) {
        try {
            getSelfInitializer().onStartup(servletContext);
        }
        catch (ServletException ex) {
            throw new ApplicationContextException("Cannot initialize servlet context", ex);
        }
    }
    initPropertySources();
}
```

### 9.1 getWebServerFactory：获取嵌入式Servlet容器工厂Bean

```java
protected ServletWebServerFactory getWebServerFactory() {
    // Use bean names so that we don't consider the hierarchy
    //获取IOC容器中类型为ServletWebServerFactory的Bean
    //必须确保容器中只有一个类型为ServletWebServerFactory的Bean
    String[] beanNames = getBeanFactory().getBeanNamesForType(ServletWebServerFactory.class);
    if (beanNames.length == 0) {
        throw new ApplicationContextException("Unable to start ServletWebServerApplicationContext due to missing "
                + "ServletWebServerFactory bean.");
    }
    if (beanNames.length > 1) {
        throw new ApplicationContextException("Unable to start ServletWebServerApplicationContext due to multiple "
                + "ServletWebServerFactory beans : " + StringUtils.arrayToCommaDelimitedString(beanNames));
    }
    return getBeanFactory().getBean(beanNames[0], ServletWebServerFactory.class);
}
```

