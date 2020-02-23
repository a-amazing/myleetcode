```java
    // Instantiate all remaining (non-lazy-init) singletons.
	// 实例化所有现存的(不是懒加载)的单例对象
    finishBeanFactoryInitialization(beanFactory);
```

11. finishBeanFactoryInitialization：初始化单实例Bean

```java
protected void finishBeanFactoryInitialization(ConfigurableListableBeanFactory beanFactory) {
    // Initialize conversion service for this context.
    // 初始化ConversionService，这个ConversionService是用于类型转换的服务接口。
    // 它的工作，是将配置文件/properties中的数据，进行类型转换，得到Spring真正想要的数据类型。
    if (beanFactory.containsBean(CONVERSION_SERVICE_BEAN_NAME) &&
            beanFactory.isTypeMatch(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class)) {
        beanFactory.setConversionService(
                beanFactory.getBean(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class));
    }

    // Register a default embedded value resolver if no bean post-processor
    // (such as a PropertyPlaceholderConfigurer bean) registered any before:
    // at this point, primarily for resolution in annotation attribute values.
    // 嵌入式值解析器EmbeddedValueResolver的组件注册，它负责解析占位符和表达式
    if (!beanFactory.hasEmbeddedValueResolver()) {
        beanFactory.addEmbeddedValueResolver(strVal -> getEnvironment().resolvePlaceholders(strVal));
    }

    // Initialize LoadTimeWeaverAware beans early to allow for registering their transformers early.
    // 尽早初始化LoadTimeWeaverAware类型的bean，以允许尽早注册其变换器。
    // 这部分与LoadTimeWeaverAware有关部分，它实际上是与AspectJ有关
    String[] weaverAwareNames = beanFactory.getBeanNamesForType(LoadTimeWeaverAware.class, false, false);
    for (String weaverAwareName : weaverAwareNames) {
        getBean(weaverAwareName);
    }

    // Stop using the temporary ClassLoader for type matching.
    // 停用临时类加载器（单行注释解释的很清楚）
    beanFactory.setTempClassLoader(null);

    // Allow for caching all bean definition metadata, not expecting further changes.
    // 允许缓存所有bean定义元数据,即不再期望有后期的变化
    beanFactory.freezeConfiguration();

    // Instantiate all remaining (non-lazy-init) singletons.
    // 【初始化】实例化所有非延迟加载的单例Bean
    beanFactory.preInstantiateSingletons();
}
```

11.1 preInstantiateSingletons

它跳转到了 `DefaultListableBeanFactory` 的 `preInstantiateSingletons` 方法：

```java
// 对bean实例化进行预处理
public void preInstantiateSingletons() throws BeansException {
    if (this.logger.isDebugEnabled()) {
        this.logger.debug("Pre-instantiating singletons in " + this);
    }

    // Iterate over a copy to allow for init methods which in turn register new bean definitions.
    // While this may not be part of the regular factory bootstrap, it does otherwise work fine.
    // 拿到了所有的Bean定义信息，这些信息已经在前面的步骤中都准备完毕了
    List<String> beanNames = new ArrayList<>(this.beanDefinitionNames);

    // Trigger initialization of all non-lazy singleton beans...
    // 这里面有一些Bean已经在之前的步骤中已经创建过了，这里只创建剩余的那些非延迟加载的单例Bean
    for (String beanName : beanNames) {
        // 合并父BeanFactory中同名的BeanDefinition，
        RootBeanDefinition bd = getMergedLocalBeanDefinition(beanName);
        // 这个Bean不是抽象Bean、是单例Bean、是非延迟加载的Bean
        if (!bd.isAbstract() && bd.isSingleton() && !bd.isLazyInit()) {
            // 是否为工厂Bean（如果是工厂Bean，还需要实现FactoryBean接口）
            if (isFactoryBean(beanName)) {
                // 如果是工厂Bean：判断该工厂Bean是否需要被迫切加载，如果需要，则直接实例化该工厂Bean
                Object bean = getBean(FACTORY_BEAN_PREFIX + beanName);
                if (bean instanceof FactoryBean) {
                    final FactoryBean<?> factory = (FactoryBean<?>) bean;
                    boolean isEagerInit;
                    if (System.getSecurityManager() != null && factory instanceof SmartFactoryBean) {
                        isEagerInit = AccessController.doPrivileged((PrivilegedAction<Boolean>)
                                        ((SmartFactoryBean<?>) factory)::isEagerInit,
                                getAccessControlContext());
                    }
                    else {
                        isEagerInit = (factory instanceof SmartFactoryBean &&
                                ((SmartFactoryBean<?>) factory).isEagerInit());
                    }
                    // 迫切加载,直接实例化该工厂Bean
                    if (isEagerInit) {
                        getBean(beanName);
                    }
                }
            }
            // 如果不是工厂Bean，直接调用getBean方法
            else {
                // 11.2 getBean
                getBean(beanName);
            }
        }
    }

    // Trigger post-initialization callback for all applicable beans...
    // 到这里，所有非延迟加载的单实例Bean都已经创建好。
    // 如果有Bean实现了SmartInitializingSingleton接口，还会去回调afterSingletonsInstantiated方法
    for (String beanName : beanNames) {
        Object singletonInstance = getSingleton(beanName);
        if (singletonInstance instanceof SmartInitializingSingleton) {
            final SmartInitializingSingleton smartSingleton = (SmartInitializingSingleton) singletonInstance;
            if (System.getSecurityManager() != null) {
                AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                    smartSingleton.afterSingletonsInstantiated();
                    return null;
                }, getAccessControlContext());
            }
            else {
                smartSingleton.afterSingletonsInstantiated();
            }
        }
    }
}
```

### 11.2 【核心】getBean

```java
public Object getBean(String name) throws BeansException {
    return doGetBean(name, null, null, false);
}

// beanName,class,参数[],是否类型确认
protected <T> T doGetBean(final String name, @Nullable final Class<T> requiredType,
        @Nullable final Object[] args, boolean typeCheckOnly) throws BeansException {

    // 11.2.1 此处是解决别名 -> BeanName的映射，getBean时可以传入bean的别名，此处可以根据别名找到BeanName
    final String beanName = transformedBeanName(name);
    Object bean;

    // Eagerly check singleton cache for manually registered singletons.
    // 先尝试从之前实例化好的Bean中找有没有这个Bean，如果能找到，说明已经被实例化了，可以直接返回
    // 11.2.2 getSingleton
    Object sharedInstance = getSingleton(beanName);
    if (sharedInstance != null && args == null) {
        if (logger.isDebugEnabled()) {
            // 找到缓存的bean,但还在创建ing
            if (isSingletonCurrentlyInCreation(beanName)) {
                logger.debug("Returning eagerly cached instance of singleton bean '" + beanName +
                        "' that is not fully initialized yet - a consequence of a circular reference");
            }
            else {
                logger.debug("Returning cached instance of singleton bean '" + beanName + "'");
            }
        }
        bean = getObjectForBeanInstance(sharedInstance, name, beanName, null);
    }

    // 11.2.3 上面get不到bean
    else {
        // Fail if we're already creating this bean instance:
        // We're assumably within a circular reference.
        // 如果搜不到，但该Bean正在被创建，说明产生了循环引用且无法处理，只能抛出异常
        if (isPrototypeCurrentlyInCreation(beanName)) {
            throw new BeanCurrentlyInCreationException(beanName);
        }

        // Check if bean definition exists in this factory.
        // 检查这个Bean对应的BeanDefinition在IOC容器中是否存在
        BeanFactory parentBeanFactory = getParentBeanFactory();
        // 父容器不为空,并且当前容器不存在该名称的类定义
        if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
            // Not found -> check parent.
            // 如果检查不存在，看看父容器有没有（Web环境会存在父子容器现象）
            String nameToLookup = originalBeanName(name);
            if (parentBeanFactory instanceof AbstractBeanFactory) {
                return ((AbstractBeanFactory) parentBeanFactory).doGetBean(
                        nameToLookup, requiredType, args, typeCheckOnly);
            }
            else if (args != null) {
                // Delegation to parent with explicit args.
                return (T) parentBeanFactory.getBean(nameToLookup, args);
            }
            else {
                // No args -> delegate to standard getBean method.
                return parentBeanFactory.getBean(nameToLookup, requiredType);
            }
        }

        // 11.2.4 走到这个地方，证明Bean确实要被创建了，标记Bean被创建
        // 该设计是防止多线程同时到这里，引发多次创建的问题
        if (!typeCheckOnly) {
            markBeanAsCreated(beanName);
        }

        try {
            // 11.2.5 合并BeanDefinition
            final RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
            checkMergedBeanDefinition(mbd, beanName, args);

            // Guarantee initialization of beans that the current bean depends on.
            // 处理当前bean的bean依赖（@DependsOn注解的依赖）
            // 在创建一个Bean之前，可能这个Bean需要依赖其他的Bean。
            // 通过这个步骤，可以先递归的将这个Bean显式声明的需要的其他Bean先创建出来。
            // 通过bean标签的depends-on属性或@DependsOn注解进行显式声明。
            // 实际操作时,我们一般只进行@Autowired
            String[] dependsOn = mbd.getDependsOn();
            if (dependsOn != null) {
                for (String dep : dependsOn) {
                    if (isDependent(beanName, dep)) {
                        throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                                "Circular depends-on relationship between '" + beanName + "' and '" + dep + "'");
                    }
                    // 分别注册依赖Bean并获取依赖Bean实例
                    registerDependentBean(dep, beanName);
                    getBean(dep);
                }
            }

            // Create bean instance.
            // 作用域为singleton，单实例Bean，创建
            if (mbd.isSingleton()) {
                // 11.3,7 匿名内部类执行完成后的getSingleton调用
                sharedInstance = getSingleton(beanName, () -> {
                    try {
                        // 11.4 createBean
                        return createBean(beanName, mbd, args);
                    }
                    catch (BeansException ex) {
                        // Explicitly remove instance from singleton cache: It might have been put there
                        // eagerly by the creation process, to allow for circular reference resolution.
                        // Also remove any beans that received a temporary reference to the bean.
                        destroySingleton(beanName);
                        throw ex;
                    }
                });
                bean = getObjectForBeanInstance(sharedInstance, name, beanName, mbd);
            }

            // 作用域为prototype类型
            else if (mbd.isPrototype()) {
                // It's a prototype -> create a new instance.
                Object prototypeInstance = null;
                try {
                    beforePrototypeCreation(beanName);
                    prototypeInstance = createBean(beanName, mbd, args);
                }
                finally {
                    afterPrototypeCreation(beanName);
                }
                bean = getObjectForBeanInstance(prototypeInstance, name, beanName, mbd);
            }

            // 作用域既不是singleton，又不是prototype，那就按照实际情况来创建吧。
            else {
                String scopeName = mbd.getScope();
                final Scope scope = this.scopes.get(scopeName);
                if (scope == null) {
                    throw new IllegalStateException("No Scope registered for scope name '" + scopeName + "'");
                }
                try {
                    Object scopedInstance = scope.get(beanName, () -> {
                        beforePrototypeCreation(beanName);
                        try {
                            return createBean(beanName, mbd, args);
                        }
                        finally {
                            afterPrototypeCreation(beanName);
                        }
                    });
                    bean = getObjectForBeanInstance(scopedInstance, name, beanName, mbd);
                }
                catch (IllegalStateException ex) {
                    throw new BeanCreationException(beanName,
                            "Scope '" + scopeName + "' is not active for the current thread; consider " +
                            "defining a scoped proxy for this bean if you intend to refer to it from a singleton",
                            ex);
                }
            }
        }
        catch (BeansException ex) {
            cleanupAfterBeanCreationFailure(beanName);
            throw ex;
        }
    }

    // Check if required type matches the type of the actual bean instance.
    // 检查所需的类型是否与实际bean实例的类型匹配，类型不匹配则抛出异常
    if (requiredType != null && !requiredType.isInstance(bean)) {
        //类型不匹配时,先尝试类型转换,如果转换失败,抛出异常
        try {
            T convertedBean = getTypeConverter().convertIfNecessary(bean, requiredType);
            if (convertedBean == null) {
                throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
            }
            return convertedBean;
        }
        catch (TypeMismatchException ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to convert bean '" + name + "' to required type '" +
                        ClassUtils.getQualifiedName(requiredType) + "'", ex);
            }
            throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
        }
    }
    return (T) bean;
}
```

