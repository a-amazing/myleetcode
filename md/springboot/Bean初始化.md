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

11.2.1 transformedBeanName：别名-BeanName的映射

```java
protected <T> T doGetBean(final String name, @Nullable final Class<T> requiredType,
        @Nullable final Object[] args, boolean typeCheckOnly) throws BeansException {

    final String beanName = transformedBeanName(name);
    Object bean;
}

protected String transformedBeanName(String name) {
    return canonicalName(BeanFactoryUtils.transformedBeanName(name));
}

public String canonicalName(String name) {
    String canonicalName = name;
    // Handle aliasing...
    String resolvedName;
    do {
        //根据原名寻找别名,直到找不到别名(最后的名称,就是真正的BeanName吗?)
        resolvedName = this.aliasMap.get(canonicalName);
        if (resolvedName != null) {
            canonicalName = resolvedName;
        }
    }
    while (resolvedName != null);
    return canonicalName;
}
  
```

11.2.2 getSingleton：尝试获取单实例Bean（解决循环依赖）

```java
    // Eagerly check singleton cache for manually registered singletons.
    // 先尝试从之前实例化好的Bean中找有没有这个Bean，如果能找到，说明已经被实例化了，可以直接返回
    Object sharedInstance = getSingleton(beanName);
    if (sharedInstance != null && args == null) {
        if (logger.isDebugEnabled()) {
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
```

11.2.3 创建前的检查

```java
    //上面get不到bean
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
```

检查循环依赖的方法： `isPrototypeCurrentlyInCreation`

```java
// 返回指定的原型bean是否当前正在创建（在当前线程内）
protected boolean isPrototypeCurrentlyInCreation(String beanName) {
    Object curVal = this.prototypesCurrentlyInCreation.get();
    //当前线程正在创建的对象不为空,并且当前值与beanName相同或者当前值中包含beanName
    return (curVal != null &&
            (curVal.equals(beanName) || (curVal instanceof Set && ((Set<?>) curVal).contains(beanName))));
}
```

11.2.4 标记准备创建的Bean

```java
        // 走到这个地方，证明Bean确实要被创建了，标记Bean被创建
        // 该设计是防止多线程同时到这里，引发多次创建的问题
        if (!typeCheckOnly) {
            markBeanAsCreated(beanName);
        }

		// 真正的标记方法如下:
		protected void markBeanAsCreated(String beanName) {
            // double-check,防止线程安全问题出现
            if (!this.alreadyCreated.contains(beanName)) {
                synchronized (this.mergedBeanDefinitions) {
                    if (!this.alreadyCreated.contains(beanName)) {
            // Let the bean definition get re-merged now that we're actually creating
            // the bean... just in case some of its metadata changed in the meantime.
                        clearMergedBeanDefinition(beanName);
                        //IOC容器会把所有创建过的Bean的name都存起来
                        this.alreadyCreated.add(beanName);
                    }
                }
            }
        }
```

11.2.5 合并BeanDefinition，处理显式依赖

```java
        try {
            // 合并BeanDefinition
            final RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
            checkMergedBeanDefinition(mbd, beanName, args);

            // Guarantee initialization of beans that the current bean depends on.
            // 处理当前bean的bean依赖（@DependsOn注解的依赖）
            // 在创建一个Bean之前，可能这个Bean需要依赖其他的Bean。
            // 通过这个步骤，可以先递归的将这个Bean显式声明的需要的其他Bean先创建出来。
            // 通过bean标签的depends-on属性或@DependsOn注解进行显式声明。
            String[] dependsOn = mbd.getDependsOn();
            if (dependsOn != null) {
                for (String dep : dependsOn) {
                    if (isDependent(beanName, dep)) {
                        throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                                "Circular depends-on relationship between '" + beanName + "' and '" + dep + "'");
                    }
                    registerDependentBean(dep, beanName);
                    getBean(dep);
                }
            }
```

11.2.6 准备创建Bean

```java
            // Create bean instance.
            // 作用域为singleton，单实例Bean，创建
            if (mbd.isSingleton()) {
                // 匿名内部类执行完成后的getSingleton调用
                sharedInstance = getSingleton(beanName, () -> {
                    try {
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
```

11.3 getSingleton

```java
public Object getSingleton(String beanName, ObjectFactory<?> singletonFactory) {
    Assert.notNull(beanName, "Bean name must not be null");
    synchronized (this.singletonObjects) {
        // 先试着从已经加载好的单实例Bean缓存区中获取是否有当前BeanName的Bean，显然没有
        Object singletonObject = this.singletonObjects.get(beanName);
        if (singletonObject == null) {
            if (this.singletonsCurrentlyInDestruction) {
                throw new BeanCreationNotAllowedException(beanName,
                        "Singleton bean creation not allowed while singletons of this factory are in destruction " +
                        "(Do not request a bean from a BeanFactory in a destroy method implementation!)");
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Creating shared instance of singleton bean '" + beanName + "'");
            }
            // 11.3.1 标记当前bean
            // 如果当前准备创建的Bean还没有在IOC容器中，就标记一下它
            beforeSingletonCreation(beanName);
            boolean newSingleton = false;
            boolean recordSuppressedExceptions = (this.suppressedExceptions == null);
            if (recordSuppressedExceptions) {
                this.suppressedExceptions = new LinkedHashSet<>();
            }
            try {
                // 11.4 创建Bean
                singletonObject = singletonFactory.getObject();
                newSingleton = true;
            }
            catch (IllegalStateException ex) {
                // Has the singleton object implicitly appeared in the meantime ->
                // if yes, proceed with it since the exception indicates that state.
                singletonObject = this.singletonObjects.get(beanName);
                if (singletonObject == null) {
                    throw ex;
                }
            }
            catch (BeanCreationException ex) {
                if (recordSuppressedExceptions) {
                    for (Exception suppressedException : this.suppressedExceptions) {
                        ex.addRelatedCause(suppressedException);
                    }
                }
                throw ex;
            }
            finally {
                if (recordSuppressedExceptions) {
                    this.suppressedExceptions = null;
                }
                afterSingletonCreation(beanName);
            }
            if (newSingleton) {
                // 将这个创建好的Bean放到单实例Bean缓存区中
                addSingleton(beanName, singletonObject);
            }
        }
        return singletonObject;
    }
}
```

11.3.1 beforeSingletonCreation

```java
protected void beforeSingletonCreation(String beanName) {
    // 排除的bean中不包含并且无法放入循环创建依赖中,抛出异常
    // 完成bean创建前校验工作
    if (!this.inCreationCheckExclusions.contains(beanName) && !this.singletonsCurrentlyInCreation.add(beanName)) {
        throw new BeanCurrentlyInCreationException(beanName);
    }
}
```

11.4 createBean

注意跳转到的类：**`AbstractAutowireCapableBeanFactory`**

```java
protected Object createBean(String beanName, RootBeanDefinition mbd, @Nullable Object[] args)
        throws BeanCreationException {

    // 打日志,真正开始创建一个bean
    if (logger.isDebugEnabled()) {
        logger.debug("Creating instance of bean '" + beanName + "'");
    }
    RootBeanDefinition mbdToUse = mbd;

    // Make sure bean class is actually resolved at this point, and
    // clone the bean definition in case of a dynamically resolved Class
    // which cannot be stored in the shared merged bean definition.
    // 先拿到这个Bean的定义信息，获取Bean的类型
    Class<?> resolvedClass = resolveBeanClass(mbd, beanName);
    if (resolvedClass != null && !mbd.hasBeanClass() && mbd.getBeanClassName() != null) {
        mbdToUse = new RootBeanDefinition(mbd);
        mbdToUse.setBeanClass(resolvedClass);
    }

    // Prepare method overrides.
    // 方法重写的准备工作
    // 利用反射，对该Bean对应类及其父类的方法定义进行获取和加载，确保能够正确实例化出该对象
    try {
        // 准备方法重写?
        mbdToUse.prepareMethodOverrides();
    }
    catch (BeanDefinitionValidationException ex) {
        throw new BeanDefinitionStoreException(mbdToUse.getResourceDescription(),
                beanName, "Validation of method overrides failed", ex);
    }

    try {
        // Give BeanPostProcessors a chance to return a proxy instead of the target bean instance.
        // 11.5 给BeanPostProcessors一个机会，来返回代理而不是目标bean实例
        // 这个步骤是确保可以创建的是被增强的代理对象而不是原始对象（AOP）
        // AOP的入口
        Object bean = resolveBeforeInstantiation(beanName, mbdToUse);
        if (bean != null) {
            //如果动态代理创建完毕，将直接返回该Bean
            return bean;
        }
    }
    catch (Throwable ex) {
        throw new BeanCreationException(mbdToUse.getResourceDescription(), beanName,
                "BeanPostProcessor before instantiation of bean failed", ex);
    }

    // 如果不需要创建动态代理对象，则执行下面的doCreateBean
    try {
        // 11.6 doCreateBean
        // 真正创建Bean的入口
        Object beanInstance = doCreateBean(beanName, mbdToUse, args);
        if (logger.isDebugEnabled()) {
            logger.debug("Finished creating instance of bean '" + beanName + "'");
        }
        return beanInstance;
    }
    catch (BeanCreationException ex) {
        // A previously detected exception with proper bean creation context already...
        throw ex;
    }
    catch (ImplicitlyAppearedSingletonException ex) {
        // An IllegalStateException to be communicated up to DefaultSingletonBeanRegistry...
        throw ex;
    }
    catch (Throwable ex) {
        throw new BeanCreationException(
                mbdToUse.getResourceDescription(), beanName, "Unexpected exception during bean creation", ex);
    }
}
```

11.5 resolveBeforeInstantiation：AOP

```java
protected Object resolveBeforeInstantiation(String beanName, RootBeanDefinition mbd) {
    Object bean = null;
    // 保证bean在实例化前的操作已完成
    if (!Boolean.FALSE.equals(mbd.beforeInstantiationResolved)) {
        // Make sure bean class is actually resolved at this point.
        //
        if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
            Class<?> targetType = determineTargetType(beanName, mbd);
            if (targetType != null) {
                bean = applyBeanPostProcessorsBeforeInstantiation(targetType, beanName);
                if (bean != null) {
                    bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
                }
            }
        }
        mbd.beforeInstantiationResolved = (bean != null);
    }
    return bean;
}
```

**`InstantiationAwareBeanPostProcessor`** 是 `BeanPostProcessor` 的子接口，它的文档注释：

> Subinterface of BeanPostProcessor that adds a before-instantiation callback, and a callback after instantiation but before explicit properties are set or autowiring occurs. Typically used to suppress default instantiation for specific target beans, for example to create proxies with special TargetSources (pooling targets, lazily initializing targets, etc), or to implement additional injection strategies such as field injection. NOTE: This interface is a special purpose interface, mainly for internal use within the framework. It is recommended to implement the plain BeanPostProcessor interface as far as possible, or to derive from InstantiationAwareBeanPostProcessorAdapter in order to be shielded from extensions to this interface.
>
> `BeanPostProcessor` 的子接口，它添加实例化之前的回调，以及在实例化之后但在设置显式属性或发生自动装配之前的回调。
>
> 通常用于抑制特定目标Bean的默认实例化，例如创建具有特殊 `TargetSource` 的代理（池目标，延迟初始化目标等），或实现其他注入策略，例如字段注入。
>
> 注意：此接口是专用接口，主要供框架内部使用。建议尽可能实现普通的 `BeanPostProcessor` 接口，或从 `InstantiationAwareBeanPostProcessorAdapter` 派生，以免对该接口进行扩展。

11.6 doCreateBean

```java
protected Object doCreateBean(final String beanName, final RootBeanDefinition mbd, final @Nullable Object[] args)
        throws BeanCreationException {

    // Instantiate the bean.
    // 实例化Bean，并创建一个BeanWrapper，对Bean进行包装
    BeanWrapper instanceWrapper = null;
    if (mbd.isSingleton()) {
        instanceWrapper = this.factoryBeanInstanceCache.remove(beanName);
    }
    // 确保已经实例化的Bean中没有当前要创建的bean，而且不是工厂Bean，才可以开始创建
    if (instanceWrapper == null) {
        // 11.6.1 createBeanInstance
        instanceWrapper = createBeanInstance(beanName, mbd, args);
    }
    final Object bean = instanceWrapper.getWrappedInstance();
    Class<?> beanType = instanceWrapper.getWrappedClass();
    if (beanType != NullBean.class) {
        mbd.resolvedTargetType = beanType;
    }

    // Allow post-processors to modify the merged bean definition.
    // MergedBeanDefinitionPostProcessor可以修改Bean的定义
    synchronized (mbd.postProcessingLock) {
        if (!mbd.postProcessed) {
            try {
                // 这个方法只允许MergedBeanDefinitionPostProcessor执行
                // MergedBeanDefinitionPostProcessor也是BeanPostProcessor的子接口，之前介绍过了
                applyMergedBeanDefinitionPostProcessors(mbd, beanType, beanName);
            }
            catch (Throwable ex) {
                throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                        "Post-processing of merged bean definition failed", ex);
            }
            mbd.postProcessed = true;
        }
    }

    // Eagerly cache singletons to be able to resolve circular references
    // even when triggered by lifecycle interfaces like BeanFactoryAware.
    // 缓存单例对象，以便能够解析循环引用，甚至在生命周期接口(如BeanFactoryAware)触发时也是如此
    boolean earlySingletonExposure = (mbd.isSingleton() && this.allowCircularReferences &&
            isSingletonCurrentlyInCreation(beanName));
    if (earlySingletonExposure) {
        if (logger.isDebugEnabled()) {
            logger.debug("Eagerly caching bean '" + beanName +
                    "' to allow for resolving potential circular references");
        }
        addSingletonFactory(beanName, () -> getEarlyBeanReference(beanName, mbd, bean));
    }

    // Initialize the bean instance.
    // 给Bean赋值
    Object exposedObject = bean;
    try {
        // 11.6.2 populateBean：属性赋值和自动注入
        populateBean(beanName, mbd, instanceWrapper);
        // 11.6.3 initializeBean：初始化Bean
        // (调用init方法?)
        exposedObject = initializeBean(beanName, exposedObject, mbd);
    }
    catch (Throwable ex) {
        if (ex instanceof BeanCreationException && beanName.equals(((BeanCreationException) ex).getBeanName())) {
            throw (BeanCreationException) ex;
        }
        else {
            throw new BeanCreationException(
                    mbd.getResourceDescription(), beanName, "Initialization of bean failed", ex);
        }
    }

    // 又获取那个单例Bean，前面已经创建好了，但还没有缓存到IOC容器中，所以这里仍然返回null，故这部分是会跳过的
    if (earlySingletonExposure) {
        Object earlySingletonReference = getSingleton(beanName, false);
        if (earlySingletonReference != null) {
            if (exposedObject == bean) {
                exposedObject = earlySingletonReference;
            }
            else if (!this.allowRawInjectionDespiteWrapping && hasDependentBean(beanName)) {
                String[] dependentBeans = getDependentBeans(beanName);
                Set<String> actualDependentBeans = new LinkedHashSet<>(dependentBeans.length);
                for (String dependentBean : dependentBeans) {
                    if (!removeSingletonIfCreatedForTypeCheckOnly(dependentBean)) {
                        actualDependentBeans.add(dependentBean);
                    }
                }
                if (!actualDependentBeans.isEmpty()) {
                    throw new BeanCurrentlyInCreationException(beanName,
                            "Bean with name '" + beanName + "' has been injected into other beans [" +
                            StringUtils.collectionToCommaDelimitedString(actualDependentBeans) +
                            "] in its raw version as part of a circular reference, but has eventually been " +
                            "wrapped. This means that said other beans do not use the final version of the " +
                            "bean. This is often the result of over-eager type matching - consider using " +
                            "'getBeanNamesOfType' with the 'allowEagerInit' flag turned off, for example.");
                }
            }
        }
    }

    // Register bean as disposable.
    // 注册Bean的销毁方法，销毁方法在IOC容器关闭后再销毁
    try {
        registerDisposableBeanIfNecessary(beanName, bean, mbd);
    }
    catch (BeanDefinitionValidationException ex) {
        throw new BeanCreationException(
                mbd.getResourceDescription(), beanName, "Invalid destruction signature", ex);
    }

    return exposedObject;
}
```

Bean的创建需要几个重要的步骤：

1. createBeanInstance：创建Bean对象
2. addSingletonFactory：Bean放入缓存（涉及到循环依赖，下一篇详细介绍）
3. populateBean：属性复制和自动注入
4. initializeBean：初始化后处理

11.6.1 【真正实例化】createBeanInstance

```java
protected BeanWrapper createBeanInstance(String beanName, RootBeanDefinition mbd, @Nullable Object[] args) {
    // Make sure bean class is actually resolved at this point.
    // 解析Bean的类型
    Class<?> beanClass = resolveBeanClass(mbd, beanName);

    if (beanClass != null && !Modifier.isPublic(beanClass.getModifiers()) && !mbd.isNonPublicAccessAllowed()) {
        throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                "Bean class isn't public, and non-public access not allowed: " + beanClass.getName());
    }

    // 11.6.1.1 判断是否有用于创建bean实例的特殊的回调方法
    // 如果存在，会使用特殊的callback回调方法，通过这个callback创建bean
    Supplier<?> instanceSupplier = mbd.getInstanceSupplier();
    if (instanceSupplier != null) {
        return obtainFromSupplier(instanceSupplier, beanName);
    }

    // 11.6.1.2 判断是否有工厂方法，如果存在，会尝试调用该Bean定义信息中的工厂方法来获取实例
    // 如果使用注解方式注册的Bean，会跳到该Bean的注册方法中（配置类中定义的那些Bean）
    if (mbd.getFactoryMethodName() != null)  {
        return instantiateUsingFactoryMethod(beanName, mbd, args);
    }

    // Shortcut when re-creating the same bean...
    // 一个类可能有多个构造器，所以Spring得根据参数个数、类型确定需要调用的构造器
    boolean resolved = false;
    boolean autowireNecessary = false;
    if (args == null) {
        synchronized (mbd.constructorArgumentLock) {
            if (mbd.resolvedConstructorOrFactoryMethod != null) {
                // 在使用构造器创建实例后，会将解析过后确定下来的构造器或工厂方法保存在缓存中，避免再次创建相同bean时再次解析，导致循环依赖
                resolved = true;
                autowireNecessary = mbd.constructorArgumentsResolved;
            }
        }
    }
    
    if (resolved) {
        // 构造器注入创建Bean
        if (autowireNecessary) {
            return autowireConstructor(beanName, mbd, null, null);
        }
        else {
            // 普通创建
            return instantiateBean(beanName, mbd);
        }
    }

    // Need to determine the constructor...
    Constructor<?>[] ctors = determineConstructorsFromBeanPostProcessors(beanClass, beanName);
    if (ctors != null ||
            mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_CONSTRUCTOR ||
            mbd.hasConstructorArgumentValues() || !ObjectUtils.isEmpty(args))  {
        return autowireConstructor(beanName, mbd, ctors, args);
    }

    // No special handling: simply use no-arg constructor.
    return instantiateBean(beanName, mbd);
}
```

11.6.1.1 getInstanceSupplier：特殊的callback回调方法

```java
public class AbstractBeanDefinition{

public void setInstanceSupplier(@Nullable Supplier<?> instanceSupplier) {
    this.instanceSupplier = instanceSupplier;
}

@Nullable
public Supplier<?> getInstanceSupplier() {
    return this.instanceSupplier;
}
    
}
```

它只是简单地get和set而已，那它这个 `Supplier` 又是从哪里来的呢？借助IDEA，发现在 `GenericApplicationContext` 中有一个调用：

```java
public <T> void registerBean(@Nullable String beanName, Class<T> beanClass,
        @Nullable Supplier<T> supplier, BeanDefinitionCustomizer... customizers) {

    ClassDerivedBeanDefinition beanDefinition = new ClassDerivedBeanDefinition(beanClass);
    if (supplier != null) {
        beanDefinition.setInstanceSupplier(supplier);
    }
    for (BeanDefinitionCustomizer customizer : customizers) {
        customizer.customize(beanDefinition);
    }

    String nameToUse = (beanName != null ? beanName : beanClass.getName());
    registerBeanDefinition(nameToUse, beanDefinition);
}
```

11.6.1.2 instantiateUsingFactoryMethod：工厂方法

`@Bean` 注解标注的方法的解析过程，这里面就有一个工厂方法的设置

```java
/**
对这种被 @Bean 注解标注的Bean进行创建
*/
protected BeanWrapper instantiateUsingFactoryMethod(
        String beanName, RootBeanDefinition mbd, @Nullable Object[] explicitArgs) {

    //借助一个构造器处理器，来执行这个工厂方法中定义的Bean
    return new ConstructorResolver(this).instantiateUsingFactoryMethod(beanName, mbd, explicitArgs);
}
```

```java

public BeanWrapper instantiateUsingFactoryMethod(
        String beanName, RootBeanDefinition mbd, @Nullable Object[] explicitArgs) {

    // 构造BeanWrapper
    BeanWrapperImpl bw = new BeanWrapperImpl();
    this.beanFactory.initBeanWrapper(bw);

    Object factoryBean;
    Class<?> factoryClass;
    boolean isStatic;

    // 获取工厂方法名称
    String factoryBeanName = mbd.getFactoryBeanName();
    if (factoryBeanName != null) {
        if (factoryBeanName.equals(beanName)) {
            throw new BeanDefinitionStoreException(mbd.getResourceDescription(), beanName,
                    "factory-bean reference points back to the same bean definition");
        }
        // 如果工厂方法不为空，则获取工厂实例，并标记该工厂方法不是静态方法
        factoryBean = this.beanFactory.getBean(factoryBeanName);
        if (mbd.isSingleton() && this.beanFactory.containsSingleton(beanName)) {
            throw new ImplicitlyAppearedSingletonException();
        }
        factoryClass = factoryBean.getClass();
        isStatic = false;
    }
    else {
        // It's a static factory method on the bean class.
        // 如果获取不到工厂方法名，则这应该是一个静态工厂，需要提供完整的工厂全限定类名，否则会抛出异常
        if (!mbd.hasBeanClass()) {
            throw new BeanDefinitionStoreException(mbd.getResourceDescription(), beanName,
                    "bean definition declares neither a bean class nor a factory-bean reference");
        }
        factoryBean = null;
        factoryClass = mbd.getBeanClass();
        isStatic = true;
    }

    Method factoryMethodToUse = null;
    ArgumentsHolder argsHolderToUse = null;
    Object[] argsToUse = null;

    // 这个explicitArgs是从这个方法的参数中传过来的，它是从getBean方法中传过来的
    // 默认情况下getBean只有BeanName（AbstractBeanFactory的getBean(String name)方法），故这里为null
    if (explicitArgs != null) {
        argsToUse = explicitArgs;
    }
    else {
        Object[] argsToResolve = null;
        synchronized (mbd.constructorArgumentLock) {
            factoryMethodToUse = (Method) mbd.resolvedConstructorOrFactoryMethod;
            if (factoryMethodToUse != null && mbd.constructorArgumentsResolved) {
                // Found a cached factory method...
                argsToUse = mbd.resolvedConstructorArguments;
                if (argsToUse == null) {
                    argsToResolve = mbd.preparedConstructorArguments;
                }
            }
        }
        if (argsToResolve != null) {
            argsToUse = resolvePreparedArguments(beanName, mbd, bw, factoryMethodToUse, argsToResolve, true);
        }
    }

    // 上面的东西统统没有，进入下面的结构体中
    if (factoryMethodToUse == null || argsToUse == null) {
        // Need to determine the factory method...
        // Try all methods with this name to see if they match the given arguments.
        factoryClass = ClassUtils.getUserClass(factoryClass);

        // 获取配置类中所有的方法（包括父类），称为候选方法
        Method[] rawCandidates = getCandidateMethods(factoryClass, mbd);
        List<Method> candidateList = new ArrayList<>();
        for (Method candidate : rawCandidates) {
            if (Modifier.isStatic(candidate.getModifiers()) == isStatic && mbd.isFactoryMethod(candidate)) {
                candidateList.add(candidate);
            }
        }

        // 因为@Bean只对当前要创建的Bean标注了一次，所以这里candidateList的大小必为1
        if (candidateList.size() == 1 && explicitArgs == null && !mbd.hasConstructorArgumentValues()) {
            Method uniqueCandidate = candidateList.get(0);
            if (uniqueCandidate.getParameterCount() == 0) {
                mbd.factoryMethodToIntrospect = uniqueCandidate;
                synchronized (mbd.constructorArgumentLock) {
                    mbd.resolvedConstructorOrFactoryMethod = uniqueCandidate;
                    mbd.constructorArgumentsResolved = true;
                    mbd.resolvedConstructorArguments = EMPTY_ARGS;
                }
                bw.setBeanInstance(instantiate(beanName, mbd, factoryBean, uniqueCandidate, EMPTY_ARGS));
                return bw;
            }
        }

        // 按照构造方法参数的数量降序排序
        Method[] candidates = candidateList.toArray(new Method[0]);
        AutowireUtils.sortFactoryMethods(candidates);

        ConstructorArgumentValues resolvedValues = null;
        boolean autowiring = (mbd.getResolvedAutowireMode() == AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR);
        int minTypeDiffWeight = Integer.MAX_VALUE;
        Set<Method> ambiguousFactoryMethods = null;

        int minNrOfArgs;
        if (explicitArgs != null) {
            minNrOfArgs = explicitArgs.length;
        }
        else {
            // We don't have arguments passed in programmatically, so we need to resolve the
            // arguments specified in the constructor arguments held in the bean definition.
            // 没有以编程方式在getBean方法中传递参数，因此需要解析在bean定义中保存的构造函数参数中指定的参数
            if (mbd.hasConstructorArgumentValues()) {
                ConstructorArgumentValues cargs = mbd.getConstructorArgumentValues();
                resolvedValues = new ConstructorArgumentValues();
                minNrOfArgs = resolveConstructorArguments(beanName, mbd, bw, cargs, resolvedValues);
            }
            else {
                minNrOfArgs = 0;
            }
        }

        LinkedList<UnsatisfiedDependencyException> causes = null;

        for (Method candidate : candidates) {
            // 解析被@Bean标注的方法的参数
            Class<?>[] paramTypes = candidate.getParameterTypes();

            if (paramTypes.length >= minNrOfArgs) {
                ArgumentsHolder argsHolder;

                if (explicitArgs != null) {
                    // Explicit arguments given -> arguments length must match exactly.
                    if (paramTypes.length != explicitArgs.length) {
                        continue;
                    }
                    argsHolder = new ArgumentsHolder(explicitArgs);
                }
                // getBean中没有传入参数，这里需要解析构造方法中的参数
                else {
                    // Resolved constructor arguments: type conversion and/or autowiring necessary.
                    // 解决的构造函数参数：类型转换、自动装配是必需的
                    try {
                        String[] paramNames = null;
                        ParameterNameDiscoverer pnd = this.beanFactory.getParameterNameDiscoverer();
                        if (pnd != null) {
                            paramNames = pnd.getParameterNames(candidate);
                        }
                        // 在已经解析的构造函数参数值的情况下，创建一个参数持有者对象
                        argsHolder = createArgumentArray(beanName, mbd, resolvedValues, bw,
                                paramTypes, paramNames, candidate, autowiring, candidates.length == 1);
                    }
                    catch (UnsatisfiedDependencyException ex) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("Ignoring factory method [" + candidate + "] of bean '" + beanName + "': " + ex);
                        }
                        // Swallow and try next overloaded factory method.
                        if (causes == null) {
                            causes = new LinkedList<>();
                        }
                        causes.add(ex);
                        continue;
                    }
                }

                // 【扩展】解析构造方法的参数时使用严格模式还是宽松模式
                int typeDiffWeight = (mbd.isLenientConstructorResolution() ?
                        argsHolder.getTypeDifferenceWeight(paramTypes) : argsHolder.getAssignabilityWeight(paramTypes));
                // Choose this factory method if it represents the closest match.
                if (typeDiffWeight < minTypeDiffWeight) {
                    factoryMethodToUse = candidate;
                    argsHolderToUse = argsHolder;
                    argsToUse = argsHolder.arguments;
                    minTypeDiffWeight = typeDiffWeight;
                    ambiguousFactoryMethods = null;
                }
                // Find out about ambiguity: In case of the same type difference weight
                // for methods with the same number of parameters, collect such candidates
                // and eventually raise an ambiguity exception.
                // However, only perform that check in non-lenient constructor resolution mode,
                // and explicitly ignore overridden methods (with the same parameter signature).
                else if (factoryMethodToUse != null && typeDiffWeight == minTypeDiffWeight &&
                        !mbd.isLenientConstructorResolution() &&
                        paramTypes.length == factoryMethodToUse.getParameterCount() &&
                        !Arrays.equals(paramTypes, factoryMethodToUse.getParameterTypes())) {
                    if (ambiguousFactoryMethods == null) {
                        ambiguousFactoryMethods = new LinkedHashSet<>();
                        ambiguousFactoryMethods.add(factoryMethodToUse);
                    }
                    ambiguousFactoryMethods.add(candidate);
                }
            }
        }

        // 如果发现没有可执行的工厂方法，进行一系列检查后可能会抛出异常
        if (factoryMethodToUse == null) {
            if (causes != null) {
                UnsatisfiedDependencyException ex = causes.removeLast();
                for (Exception cause : causes) {
                    this.beanFactory.onSuppressedException(cause);
                }
                throw ex;
            }
            List<String> argTypes = new ArrayList<>(minNrOfArgs);
            if (explicitArgs != null) {
                for (Object arg : explicitArgs) {
                    argTypes.add(arg != null ? arg.getClass().getSimpleName() : "null");
                }
            }
            else if (resolvedValues != null) {
                Set<ValueHolder> valueHolders = new LinkedHashSet<>(resolvedValues.getArgumentCount());
                valueHolders.addAll(resolvedValues.getIndexedArgumentValues().values());
                valueHolders.addAll(resolvedValues.getGenericArgumentValues());
                for (ValueHolder value : valueHolders) {
                    String argType = (value.getType() != null ? ClassUtils.getShortName(value.getType()) :
                            (value.getValue() != null ? value.getValue().getClass().getSimpleName() : "null"));
                    argTypes.add(argType);
                }
            }
            String argDesc = StringUtils.collectionToCommaDelimitedString(argTypes);
            throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                    "No matching factory method found: " +
                    (mbd.getFactoryBeanName() != null ?
                        "factory bean '" + mbd.getFactoryBeanName() + "'; " : "") +
                    "factory method '" + mbd.getFactoryMethodName() + "(" + argDesc + ")'. " +
                    "Check that a method with the specified name " +
                    (minNrOfArgs > 0 ? "and arguments " : "") +
                    "exists and that it is " +
                    (isStatic ? "static" : "non-static") + ".");
        }
        else if (void.class == factoryMethodToUse.getReturnType()) {
            throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                    "Invalid factory method '" + mbd.getFactoryMethodName() +
                    "': needs to have a non-void return type!");
        }
        else if (ambiguousFactoryMethods != null) {
            throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                    "Ambiguous factory method matches found in bean '" + beanName + "' " +
                    "(hint: specify index/type/name arguments for simple parameters to avoid type ambiguities): " +
                    ambiguousFactoryMethods);
        }

        if (explicitArgs == null && argsHolderToUse != null) {
            mbd.factoryMethodToIntrospect = factoryMethodToUse;
            argsHolderToUse.storeCache(mbd, factoryMethodToUse);
        }
    }

    Assert.state(argsToUse != null, "Unresolved factory method arguments");
    // 实例化Bean，包装BeanWraper
    bw.setBeanInstance(instantiate(beanName, mbd, factoryBean, factoryMethodToUse, argsToUse));
    return bw;
}
```

总结下来就干了一件事：确定工厂方法 + 实例化、包装 `BeanWrapper` 。

**解析构造方法参数的严格模式/宽松模式**。

11.6.1.3 【扩展】严格模式/宽松模式

```java
// true 严格模式/ false 宽松模式
int typeDiffWeight = (mbd.isLenientConstructorResolution() ?
            argsHolder.getTypeDifferenceWeight(paramTypes) : argsHolder.getAssignabilityWeight(paramTypes));
    // Choose this factory method if it represents the closest match.
    if (typeDiffWeight < minTypeDiffWeight) {
        factoryMethodToUse = candidate;
        argsHolderToUse = argsHolder;
        argsToUse = argsHolder.arguments;
        minTypeDiffWeight = typeDiffWeight;
        ambiguousFactoryMethods = null;
    }
```

严格模式代码如下:

```java
// 严格模式
public int getTypeDifferenceWeight(Class<?>[] paramTypes) {
    // If valid arguments found, determine type difference weight.
    // Try type difference weight on both the converted arguments and
    // the raw arguments. If the raw weight is better, use it.
    // Decrease raw weight by 1024 to prefer it over equal converted weight.
    // 如果找到有效的参数，请确定类型差异权重。尝试对转换后的参数和原始参数都使用类型差异权重。
    // 如果原始权重更好，请使用它。将原始权重减少1024，以使其优于相等的转换权重。
    
    // 先拿转换之后的参数对比
    int typeDiffWeight = MethodInvoker.getTypeDifferenceWeight(paramTypes, this.arguments);
    // 再拿原始参数对比
    int rawTypeDiffWeight = MethodInvoker.getTypeDifferenceWeight(paramTypes, this.rawArguments) - 1024;
    // 由值确定选哪一个，值越小越接近参数声明类型
    return (rawTypeDiffWeight < typeDiffWeight ? rawTypeDiffWeight : typeDiffWeight);
}

// 宽松模式
public int getAssignabilityWeight(Class<?>[] paramTypes) {
    for (int i = 0; i < paramTypes.length; i++) {
        if (!ClassUtils.isAssignableValue(paramTypes[i], this.arguments[i])) {
            return Integer.MAX_VALUE;
        }
    }
    for (int i = 0; i < paramTypes.length; i++) {
        if (!ClassUtils.isAssignableValue(paramTypes[i], this.rawArguments[i])) {
            return Integer.MAX_VALUE - 512;
        }
    }
    return Integer.MAX_VALUE - 1024;
}
```

- 严格模式下，必须要求参数类型完全一致
  - 这个方法的实现涉及到算法，不作细致研究，小伙伴们了解即可，感兴趣的小伙伴可以Debug运行看一下机制。
- 宽松模式，只要参数是声明类型或子类型即可
  - 如果使用宽松模式，会出现一个问题：如果构造方法中传入两个接口，而这两个接口分别有两个实现类，此时IOC容器会觉得这两个对象都可以放到这两个参数中，造成权重一致，出现**构造方法歧义**。

11.6.2 populateBean：属性赋值和自动注入

```java
protected void populateBean(String beanName, RootBeanDefinition mbd, @Nullable BeanWrapper bw) {
    if (bw == null) {
        if (mbd.hasPropertyValues()) {
            throw new BeanCreationException(
                    mbd.getResourceDescription(), beanName, "Cannot apply property values to null instance");
        }
        else {
            // Skip property population phase for null instance.
            return;
        }
    }

    // Give any InstantiationAwareBeanPostProcessors the opportunity to modify the
    // state of the bean before properties are set. This can be used, for example,
    // to support styles of field injection.
    boolean continueWithPropertyPopulation = true;

    // 执行所有InstantiationAwareBeanPostProcessor的postProcessAfterInstantiation方法
    if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
        for (BeanPostProcessor bp : getBeanPostProcessors()) {
            if (bp instanceof InstantiationAwareBeanPostProcessor) {
                InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
                if (!ibp.postProcessAfterInstantiation(bw.getWrappedInstance(), beanName)) {
                    continueWithPropertyPopulation = false;
                    break;
                }
            }
        }
    }

    if (!continueWithPropertyPopulation) {
        return;
    }

    PropertyValues pvs = (mbd.hasPropertyValues() ? mbd.getPropertyValues() : null);

    if (mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_NAME ||
            mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_TYPE) {
        MutablePropertyValues newPvs = new MutablePropertyValues(pvs);

        // Add property values based on autowire by name if applicable.
        if (mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_NAME) {
            autowireByName(beanName, mbd, bw, newPvs);
        }

        // Add property values based on autowire by type if applicable.
        if (mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_TYPE) {
            autowireByType(beanName, mbd, bw, newPvs);
        }

        pvs = newPvs;
    }

    boolean hasInstAwareBpps = hasInstantiationAwareBeanPostProcessors();
    boolean needsDepCheck = (mbd.getDependencyCheck() != RootBeanDefinition.DEPENDENCY_CHECK_NONE);

    // 11.6.2.1 又拿了那些InstantiationAwareBeanPostProcessor，不过这次执行的方法不同：postProcessPropertyValues
    // 这些InstantiationAwareBeanPostProcessor其中有一个能实现 @Autowired、@Value 等注入
    if (hasInstAwareBpps || needsDepCheck) {
        if (pvs == null) {
            pvs = mbd.getPropertyValues();
        }
        PropertyDescriptor[] filteredPds = filterPropertyDescriptorsForDependencyCheck(bw, mbd.allowCaching);
        if (hasInstAwareBpps) {
            for (BeanPostProcessor bp : getBeanPostProcessors()) {
                if (bp instanceof InstantiationAwareBeanPostProcessor) {
                    InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
                    pvs = ibp.postProcessPropertyValues(pvs, filteredPds, bw.getWrappedInstance(), beanName);
                    if (pvs == null) {
                        return;
                    }
                }
            }
        }
        if (needsDepCheck) {
            checkDependencies(beanName, mbd, filteredPds, pvs);
        }
    }

    // 11.6.2. 使用setter方式，给Bean赋值和自动注入
    if (pvs != null) {
        applyPropertyValues(beanName, mbd, bw, pvs);
    }
}
```

`InstantiationAwareBeanPostProcessor` 就可以实现 `@Autowired` 、`@Value` 等自动注入。

通过 `setter` 的方式进行自动注入，会走最后的一个if结构，调用 `applyPropertyValues` 方法。

11.6.2.1 @Autowired 的自动注入

```java
// 通过下面这个类实现@Autowired自动注入
public class AutowiredAnnotationBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter
		implements MergedBeanDefinitionPostProcessor, PriorityOrdered, BeanFactoryAware
public abstract class InstantiationAwareBeanPostProcessorAdapter implements SmartInstantiationAwareBeanPostProcessor
public interface SmartInstantiationAwareBeanPostProcessor extends InstantiationAwareBeanPostProcessor
```

核心回调方法就是 `postProcessProperties` ：

```java
public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) {
    // 构建自动注入的元数据
    InjectionMetadata metadata = findAutowiringMetadata(beanName, bean.getClass(), pvs);
    try {
        // 真正进行参数注入
        metadata.inject(bean, beanName, pvs);
    }
    catch (BeanCreationException ex) {
        throw ex;
    }
    catch (Throwable ex) {
        throw new BeanCreationException(beanName, "Injection of autowired dependencies failed", ex);
    }
    return pvs;
}
```

11.6.2.2 [Autowired] inject

```java
public void inject(Object target, @Nullable String beanName, @Nullable PropertyValues pvs) throws Throwable {
    Collection<InjectedElement> checkedElements = this.checkedElements;
    Collection<InjectedElement> elementsToIterate =
            (checkedElements != null ? checkedElements : this.injectedElements);
    if (!elementsToIterate.isEmpty()) {
        for (InjectedElement element : elementsToIterate) {
            if (logger.isTraceEnabled()) {
                logger.trace("Processing injected element of bean '" + beanName + "': " + element);
            }
            element.inject(target, beanName, pvs);
        }
    }
}
```

最终调 了 `element.inject` 方法进行参数注入

这个方法有两个子类重写了这个方法，分别是 `AutowiredFieldElement` 和 `AutowiredMethodElement` 。很明显它们是给属性注入和方法注入的

```java
protected void inject(Object bean, @Nullable String beanName, @Nullable PropertyValues pvs) throws Throwable {
    Field field = (Field) this.member;
    Object value;
    // 如果这个值在前面的注入中有缓存过，直接取缓存
    if (this.cached) {
        value = resolvedCachedArgument(beanName, this.cachedFieldValue);
    }
    else {
        // 没有缓存，要在下面的try块中利用BeanFactory处理依赖关系
        DependencyDescriptor desc = new DependencyDescriptor(field, this.required);
        desc.setContainingClass(bean.getClass());
        Set<String> autowiredBeanNames = new LinkedHashSet<>(1);
        Assert.state(beanFactory != null, "No BeanFactory available");
        TypeConverter typeConverter = beanFactory.getTypeConverter();
        try {
            // 【关联创建】value应该被找出 / 创建出
            value = beanFactory.resolveDependency(desc, beanName, autowiredBeanNames, typeConverter);
        }
        catch (BeansException ex) {
            throw new UnsatisfiedDependencyException(null, beanName, new InjectionPoint(field), ex);
        }
        synchronized (this) {
            // 处理完成后要对这个属性进行缓存
            if (!this.cached) {
                if (value != null || this.required) {
                    this.cachedFieldValue = desc;
                    // 把这个依赖的Bean添加到BeanFactory的依赖关系映射上缓存起来
                    registerDependentBeans(beanName, autowiredBeanNames);
                    if (autowiredBeanNames.size() == 1) {
                        String autowiredBeanName = autowiredBeanNames.iterator().next();
                        if (beanFactory.containsBean(autowiredBeanName) &&
                                beanFactory.isTypeMatch(autowiredBeanName, field.getType())) {
                            this.cachedFieldValue = new ShortcutDependencyDescriptor(
                                    desc, autowiredBeanName, field.getType());
                        }
                    }
                }
                else {
                    this.cachedFieldValue = null;
                }
                this.cached = true;
            }
        }
    }
    // 如果找到 / 创建好了value，就给它注入
    if (value != null) {
        ReflectionUtils.makeAccessible(field);
        field.set(bean, value);
    }
}
```

在try块中的核心方法可以用来关联创建被依赖的Bean：`beanFactory.resolveDependency` 。

11.6.2.3 [Autowired] beanFactory.resolveDependency

```java
public Object resolveDependency(DependencyDescriptor descriptor, @Nullable String requestingBeanName,
        @Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter) throws BeansException {

    descriptor.initParameterNameDiscovery(getParameterNameDiscoverer());
    if (Optional.class == descriptor.getDependencyType()) {
        return createOptionalDependency(descriptor, requestingBeanName);
    }
    else if (ObjectFactory.class == descriptor.getDependencyType() ||
            ObjectProvider.class == descriptor.getDependencyType()) {
        return new DependencyObjectProvider(descriptor, requestingBeanName);
    }
    else if (javaxInjectProviderClass == descriptor.getDependencyType()) {
        return new Jsr330Factory().createDependencyProvider(descriptor, requestingBeanName);
    }
    else {
        Object result = getAutowireCandidateResolver().getLazyResolutionProxyIfNecessary(
                descriptor, requestingBeanName);
        if (result == null) {
            result = doResolveDependency(descriptor, requestingBeanName, autowiredBeanNames, typeConverter);
        }
        return result;
    }
}
```

11.6.2.4 [Autowired] doResolveDependency

```java
//真正处理依赖的方法(?)
public Object doResolveDependency(DependencyDescriptor descriptor, @Nullable String beanName,
        @Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter) throws BeansException {

    InjectionPoint previousInjectionPoint = ConstructorResolver.setCurrentInjectionPoint(descriptor);
    try {
        // 该方法默认是调用DependencyDescriptor的方法，没有子类，默认实现是返回null
        Object shortcut = descriptor.resolveShortcut(this);
        if (shortcut != null) {
            return shortcut;
        }

        // 获取依赖的class类型
        Class<?> type = descriptor.getDependencyType();
        // 处理@Value注解
        Object value = getAutowireCandidateResolver().getSuggestedValue(descriptor);
        if (value != null) {
            if (value instanceof String) {
                String strVal = resolveEmbeddedValue((String) value);
                BeanDefinition bd = (beanName != null && containsBean(beanName) ?
                        getMergedBeanDefinition(beanName) : null);
                value = evaluateBeanDefinitionString(strVal, bd);
            }
            TypeConverter converter = (typeConverter != null ? typeConverter : getTypeConverter());
            try {
                return converter.convertIfNecessary(value, type, descriptor.getTypeDescriptor());
            }
            catch (UnsupportedOperationException ex) {
                // A custom TypeConverter which does not support TypeDescriptor resolution...
                return (descriptor.getField() != null ?
                        converter.convertIfNecessary(value, type, descriptor.getField()) :
                        converter.convertIfNecessary(value, type, descriptor.getMethodParameter()));
            }
        }

        // 处理数组、集合、Map等
        Object multipleBeans = resolveMultipleBeans(descriptor, beanName, autowiredBeanNames, typeConverter);
        if (multipleBeans != null) {
            return multipleBeans;
        }

        // 从现有的已经创建好的Bean实例中找可以匹配到该自动注入的字段上的Bean
        Map<String, Object> matchingBeans = findAutowireCandidates(beanName, type, descriptor);
        if (matchingBeans.isEmpty()) {
            if (isRequired(descriptor)) {
                raiseNoMatchingBeanFound(type, descriptor.getResolvableType(), descriptor);
            }
            return null;
        }

        String autowiredBeanName;
        Object instanceCandidate;

        // 如果找到了，超过1个，会决定使用哪个Bean更合适，如果真的分辨不出来，则会抛出异常
        if (matchingBeans.size() > 1) {
            autowiredBeanName = determineAutowireCandidate(matchingBeans, descriptor);
            if (autowiredBeanName == null) {
                if (isRequired(descriptor) || !indicatesMultipleBeans(type)) {
                    return descriptor.resolveNotUnique(descriptor.getResolvableType(), matchingBeans);
                }
                else {
                    // In case of an optional Collection/Map, silently ignore a non-unique case:
                    // possibly it was meant to be an empty collection of multiple regular beans
                    // (before 4.3 in particular when we didn't even look for collection beans).
                    return null;
                }
            }
            instanceCandidate = matchingBeans.get(autowiredBeanName);
        }
        else {
            // We have exactly one match.
            // 匹配不到，要走下面的创建流程
            Map.Entry<String, Object> entry = matchingBeans.entrySet().iterator().next();
            autowiredBeanName = entry.getKey();
            instanceCandidate = entry.getValue();
        }

        if (autowiredBeanNames != null) {
            autowiredBeanNames.add(autowiredBeanName);
        }
        // 关联创建
        if (instanceCandidate instanceof Class) {
            instanceCandidate = descriptor.resolveCandidate(autowiredBeanName, type, this);
        }
        Object result = instanceCandidate;
        if (result instanceof NullBean) {
            if (isRequired(descriptor)) {
                raiseNoMatchingBeanFound(type, descriptor.getResolvableType(), descriptor);
            }
            result = null;
        }
        if (!ClassUtils.isAssignableValue(type, result)) {
            throw new BeanNotOfRequiredTypeException(autowiredBeanName, type, instanceCandidate.getClass());
        }
        return result;
    }
    finally {
        ConstructorResolver.setCurrentInjectionPoint(previousInjectionPoint);
    }
}
```

`descriptor.resolveCandidate(autowiredBeanName, type, this);` ，在一开始什么Bean都匹配不到的情况下，Debug发现会来到这里，而这个方法的实现：

```java
public Object resolveCandidate(String beanName, Class<?> requiredType, BeanFactory beanFactory)
        throws BeansException {
    // 从容器中根据beanName获取对象
    return beanFactory.getBean(beanName);
}
```

创建好后，回到 inject 方法：

```java
    // 如果找到 / 创建好了value，就给它注入
    if (value != null) {
        ReflectionUtils.makeAccessible(field);
        field.set(bean, value);
    }
```

11.6.2.5 [setter] applyPropertyValues

使用setter方法，前面的一大段都不走了，直接来到最后的 `applyPropertyValues` 方法。

进入到 `applyPropertyValues` 方法中

```java
protected void applyPropertyValues(String beanName, BeanDefinition mbd, BeanWrapper bw, PropertyValues pvs) {
    // 没有任何要属性赋值/自动注入，直接返回
    if (pvs.isEmpty()) {
        return;
    }

    if (System.getSecurityManager() != null && bw instanceof BeanWrapperImpl) {
        ((BeanWrapperImpl) bw).setSecurityContext(getAccessControlContext());
    }

    MutablePropertyValues mpvs = null;
    // 需要转换的属性
    List<PropertyValue> original;

    if (pvs instanceof MutablePropertyValues) {
        mpvs = (MutablePropertyValues) pvs;
        if (mpvs.isConverted()) {
            // Shortcut: use the pre-converted values as-is.
            try {
                bw.setPropertyValues(mpvs);
                return;
            }
            catch (BeansException ex) {
                throw new BeanCreationException(
                        mbd.getResourceDescription(), beanName, "Error setting property values", ex);
            }
        }
        original = mpvs.getPropertyValueList();
    }
    else {
        original = Arrays.asList(pvs.getPropertyValues());
    }

    // 类型转换器是可以自定义的
    TypeConverter converter = getCustomTypeConverter();
    if (converter == null) {
        converter = bw;
    }
    // BeanDefinitionValueResolver是真正实现属性赋值和自动注入的
    BeanDefinitionValueResolver valueResolver = new BeanDefinitionValueResolver(this, beanName, mbd, converter);

    // Create a deep copy, resolving any references for values.
    List<PropertyValue> deepCopy = new ArrayList<>(original.size());
    boolean resolveNecessary = false;
    for (PropertyValue pv : original) {
        if (pv.isConverted()) {
            deepCopy.add(pv);
        } else {
            String propertyName = pv.getName();
            Object originalValue = pv.getValue();
            // 11.6.2.6 【核心】解析、注入值
            Object resolvedValue = valueResolver.resolveValueIfNecessary(pv, originalValue);
            Object convertedValue = resolvedValue;
            boolean convertible = bw.isWritableProperty(propertyName) &&
                    !PropertyAccessorUtils.isNestedOrIndexedProperty(propertyName);
            if (convertible) {
                convertedValue = convertForProperty(resolvedValue, propertyName, bw, converter);
            }
            // Possibly store converted value in merged bean definition,
            // in order to avoid re-conversion for every created bean instance.
            // 将已经转换过的值放入缓存，避免重复解析降低效率
            if (resolvedValue == originalValue) {
                if (convertible) {
                    pv.setConvertedValue(convertedValue);
                }
                deepCopy.add(pv);
            }
            else if (convertible && originalValue instanceof TypedStringValue &&
                    !((TypedStringValue) originalValue).isDynamic() &&
                    !(convertedValue instanceof Collection || ObjectUtils.isArray(convertedValue))) {
                pv.setConvertedValue(convertedValue);
                deepCopy.add(pv);
            }
            else {
                resolveNecessary = true;
                deepCopy.add(new PropertyValue(pv, convertedValue));
            }
        }
    }
    // 标记已经转换完毕
    if (mpvs != null && !resolveNecessary) {
        mpvs.setConverted();
    }

    // Set our (possibly massaged) deep copy.
    try {
        bw.setPropertyValues(new MutablePropertyValues(deepCopy));
    }
    catch (BeansException ex) {
        throw new BeanCreationException(
                mbd.getResourceDescription(), beanName, "Error setting property values", ex);
    }
}
```

核心方法：**`valueResolver.resolveValueIfNecessary`**

11.6.2.6 [setter] resolveValueIfNecessary

```java
// 通过参数名和参数值进行解析
public Object resolveValueIfNecessary(Object argName, @Nullable Object value) {
    // We must check each value to see whether it requires a runtime reference
    // to another bean to be resolved.
    // 11.6.2.7 如果依赖了另外一个Bean时，进入下面的分支
    if (value instanceof RuntimeBeanReference) {
        RuntimeBeanReference ref = (RuntimeBeanReference) value;
        return resolveReference(argName, ref);
    }
    // 如果根据另一个Bean的name进行依赖，进入下面的分支
    else if (value instanceof RuntimeBeanNameReference) {
        String refName = ((RuntimeBeanNameReference) value).getBeanName();
        refName = String.valueOf(doEvaluate(refName));
        if (!this.beanFactory.containsBean(refName)) {
            throw new BeanDefinitionStoreException(
                    "Invalid bean name '" + refName + "' in bean reference for " + argName);
        }
        return refName;
    }
    // 解析BeanDefinitionHolder
    else if (value instanceof BeanDefinitionHolder) {
        // Resolve BeanDefinitionHolder: contains BeanDefinition with name and aliases.
        BeanDefinitionHolder bdHolder = (BeanDefinitionHolder) value;
        return resolveInnerBean(argName, bdHolder.getBeanName(), bdHolder.getBeanDefinition());
    }
    // 解析纯BeanDefinition
    else if (value instanceof BeanDefinition) {
        // Resolve plain BeanDefinition, without contained name: use dummy name.
        BeanDefinition bd = (BeanDefinition) value;
        String innerBeanName = "(inner bean)" + BeanFactoryUtils.GENERATED_BEAN_NAME_SEPARATOR +
                ObjectUtils.getIdentityHexString(bd);
        return resolveInnerBean(argName, innerBeanName, bd);
    }
    // 解析数组
    else if (value instanceof ManagedArray) {
        // May need to resolve contained runtime references.
        ManagedArray array = (ManagedArray) value;
        Class<?> elementType = array.resolvedElementType;
        if (elementType == null) {
            String elementTypeName = array.getElementTypeName();
            if (StringUtils.hasText(elementTypeName)) {
                try {
                    elementType = ClassUtils.forName(elementTypeName, this.beanFactory.getBeanClassLoader());
                    array.resolvedElementType = elementType;
                }
                catch (Throwable ex) {
                    // Improve the message by showing the context.
                    throw new BeanCreationException(
                            this.beanDefinition.getResourceDescription(), this.beanName,
                            "Error resolving array type for " + argName, ex);
                }
            }
            else {
                elementType = Object.class;
            }
        }
        return resolveManagedArray(argName, (List<?>) value, elementType);
    }
    // 11.6.2.8 解析List
    else if (value instanceof ManagedList) {
        // May need to resolve contained runtime references.
        return resolveManagedList(argName, (List<?>) value);
    }
    // 解析Set
    else if (value instanceof ManagedSet) {
        // May need to resolve contained runtime references.
        return resolveManagedSet(argName, (Set<?>) value);
    }
    // 解析Map
    else if (value instanceof ManagedMap) {
        // May need to resolve contained runtime references.
        return resolveManagedMap(argName, (Map<?, ?>) value);
    }
    // 解析Properties
    else if (value instanceof ManagedProperties) {
        Properties original = (Properties) value;
        Properties copy = new Properties();
        original.forEach((propKey, propValue) -> {
            if (propKey instanceof TypedStringValue) {
                propKey = evaluate((TypedStringValue) propKey);
            }
            if (propValue instanceof TypedStringValue) {
                propValue = evaluate((TypedStringValue) propValue);
            }
            if (propKey == null || propValue == null) {
                throw new BeanCreationException(
                        this.beanDefinition.getResourceDescription(), this.beanName,
                        "Error converting Properties key/value pair for " + argName + ": resolved to null");
            }
            copy.put(propKey, propValue);
        });
        return copy;
    }
    // 解析String
    else if (value instanceof TypedStringValue) {
        // Convert value to target type here.
        TypedStringValue typedStringValue = (TypedStringValue) value;
        Object valueObject = evaluate(typedStringValue);
        try {
            Class<?> resolvedTargetType = resolveTargetType(typedStringValue);
            if (resolvedTargetType != null) {
                return this.typeConverter.convertIfNecessary(valueObject, resolvedTargetType);
            }
            else {
                return valueObject;
            }
        }
        catch (Throwable ex) {
            // Improve the message by showing the context.
            throw new BeanCreationException(
                    this.beanDefinition.getResourceDescription(), this.beanName,
                    "Error converting typed String value for " + argName, ex);
        }
    }
    else if (value instanceof NullBean) {
        return null;
    }
    else {
        return evaluate(value);
    }
}
```

11.6.2.7 [setter] RuntimeBeanReference

```java
 	if (value instanceof RuntimeBeanReference) {
        RuntimeBeanReference ref = (RuntimeBeanReference) value;
        return resolveReference(argName, ref);
    }

// 跳转到了 resolveReference 方法：
private Object resolveReference(Object argName, RuntimeBeanReference ref) {
    try {
        Object bean;
        // 获取BeanName
        String refName = ref.getBeanName();
        refName = String.valueOf(doEvaluate(refName));
        // 如果Bean在父容器，则去父容器取
        if (ref.isToParent()) {
            if (this.beanFactory.getParentBeanFactory() == null) {
                throw new BeanCreationException(
                        this.beanDefinition.getResourceDescription(), this.beanName,
                        "Can't resolve reference to bean '" + refName +
                                "' in parent factory: no parent factory available");
            }
            bean = this.beanFactory.getParentBeanFactory().getBean(refName);
        }
        else {
            // 在本容器，调用getBean
            bean = this.beanFactory.getBean(refName);
            this.beanFactory.registerDependentBean(refName, this.beanName);
        }
        if (bean instanceof NullBean) {
            bean = null;
        }
        return bean;
    }
    catch (BeansException ex) {
        throw new BeanCreationException(
                this.beanDefinition.getResourceDescription(), this.beanName,
                "Cannot resolve reference to bean '" + ref.getBeanName() + "' while setting " + argName, ex);
    }
}
```

11.6.2.8 [setter] 解析List

```java
    else if (value instanceof ManagedList) {
        // May need to resolve contained runtime references.
        return resolveManagedList(argName, (List<?>) value);
    }

// 跳转到 resolveManagedList 方法：
private List<?> resolveManagedList(Object argName, List<?> ml) {
    List<Object> resolved = new ArrayList<>(ml.size());
    for (int i = 0; i < ml.size(); i++) {
        resolved.add(resolveValueIfNecessary(new KeyedArgName(argName, i), ml.get(i)));
    }
    return resolved;
}
```

11.6.3 initializeBean：初始化Bean

```java
protected Object initializeBean(final String beanName, final Object bean, @Nullable RootBeanDefinition mbd) {
    if (System.getSecurityManager() != null) {
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            invokeAwareMethods(beanName, bean);
            return null;
        }, getAccessControlContext());
    }
    else {
        // 11.6.3.1 将那些实现xxxAware接口的类，注入一些属性（beanName、ClassLoader、BeanFactory）
        invokeAwareMethods(beanName, bean);
    }

    Object wrappedBean = bean;
    if (mbd == null || !mbd.isSynthetic()) {
        // 11.6.3.2 后置处理器在做初始化之前的处理
        wrappedBean = applyBeanPostProcessorsBeforeInitialization(wrappedBean, beanName);
    }

    try {
        // 11.6.3.3 初始化Bean，执行@PostConstruct，InitializingBean接口的方法
        invokeInitMethods(beanName, wrappedBean, mbd);
    }
    catch (Throwable ex) {
        throw new BeanCreationException(
                (mbd != null ? mbd.getResourceDescription() : null),
                beanName, "Invocation of init method failed", ex);
    }
    if (mbd == null || !mbd.isSynthetic()) {
        // 后置处理器在做初始化之后的处理
        wrappedBean = applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);
    }

    return wrappedBean;
}
```

11.6.3.1 invokeAwareMethods：执行注入的功能

如果是Aware接口类型,分别强制类型转换为该类型接口,并调用接口方法

```java
private void invokeAwareMethods(final String beanName, final Object bean) {
    if (bean instanceof Aware) {
        if (bean instanceof BeanNameAware) {
            ((BeanNameAware) bean).setBeanName(beanName);
        }
        if (bean instanceof BeanClassLoaderAware) {
            ClassLoader bcl = getBeanClassLoader();
            if (bcl != null) {
                ((BeanClassLoaderAware) bean).setBeanClassLoader(bcl);
            }
        }
        if (bean instanceof BeanFactoryAware) {
            ((BeanFactoryAware) bean).setBeanFactory(AbstractAutowireCapableBeanFactory.this);
        }
    }
}
```

11.6.3.2 applyBeanPostProcessorsBeforeInitialization：执行后置处理器

```java
public Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName)
        throws BeansException {

    Object result = existingBean;
    for (BeanPostProcessor processor : getBeanPostProcessors()) {
        Object current = processor.postProcessBeforeInitialization(result, beanName);
        if (current == null) {
            return result;
        }
        result = current;
    }
    return result;
}
```

11.6.3.3 invokeInitMethods：执行初始化Bean的操作

```java
protected void invokeInitMethods(String beanName, final Object bean, @Nullable RootBeanDefinition mbd)
        throws Throwable {

    // 不是InitializiingBean，而且也没声明afterPropertiesSet方法，则不执行下面的逻辑
    boolean isInitializingBean = (bean instanceof InitializingBean);
    if (isInitializingBean && (mbd == null || !mbd.isExternallyManagedInitMethod("afterPropertiesSet"))) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking afterPropertiesSet() on bean with name '" + beanName + "'");
        }
        if (System.getSecurityManager() != null) {
            try {
                AccessController.doPrivileged((PrivilegedExceptionAction<Object>) () -> {
                    ((InitializingBean) bean).afterPropertiesSet();
                    return null;
                }, getAccessControlContext());
            }
            catch (PrivilegedActionException pae) {
                throw pae.getException();
            }
        }
        else {
            ((InitializingBean) bean).afterPropertiesSet();
        }
    }

    if (mbd != null && bean.getClass() != NullBean.class) {
        String initMethodName = mbd.getInitMethodName();
        if (StringUtils.hasLength(initMethodName) &&
                !(isInitializingBean && "afterPropertiesSet".equals(initMethodName)) &&
                !mbd.isExternallyManagedInitMethod(initMethodName)) {
            invokeCustomInitMethod(beanName, bean, mbd);
        }
    }
}
```

类上标注 `PostConstruct` ，发现它的调用栈里有一个 `InitDestroyAnnotationBeanPostProcessor` ，它的执行方法 `postProcessBeforeInitilization` 方法如下：

```java
public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    LifecycleMetadata metadata = findLifecycleMetadata(bean.getClass());
    try {
        // 执行了 @PostConstruct 标注的方法。
        metadata.invokeInitMethods(bean, beanName);
    }
    catch (InvocationTargetException ex) {
        throw new BeanCreationException(beanName, "Invocation of init method failed", ex.getTargetException());
    }
    catch (Throwable ex) {
        throw new BeanCreationException(beanName, "Failed to invoke init method", ex);
    }
    return bean;
}
```

11.7 匿名内部类执行完成后的getSingleton调用

```java
public Object getSingleton(String beanName, ObjectFactory<?> singletonFactory) {
    Assert.notNull(beanName, "Bean name must not be null");
    synchronized (this.singletonObjects) {
        Object singletonObject = this.singletonObjects.get(beanName);
        if (singletonObject == null) {
            // ......
            try {
                // createBean方法在getObject中
                singletonObject = singletonFactory.getObject();
                newSingleton = true;
            }
            catch (IllegalStateException ex) {
                // Has the singleton object implicitly appeared in the meantime ->
                // if yes, proceed with it since the exception indicates that state.
                singletonObject = this.singletonObjects.get(beanName);
                if (singletonObject == null) {
                    throw ex;
                }
            }
            catch (BeanCreationException ex) {
                if (recordSuppressedExceptions) {
                    for (Exception suppressedException : this.suppressedExceptions) {
                        ex.addRelatedCause(suppressedException);
                    }
                }
                throw ex;
            }
            finally {
                if (recordSuppressedExceptions) {
                    this.suppressedExceptions = null;
                }
                afterSingletonCreation(beanName);
            }
            if (newSingleton) {
                // 将这个创建好的Bean放到IOC容器的单实例Bean缓存区中
                addSingleton(beanName, singletonObject);
            }
        }
        return singletonObject;
    }
}


/**
要把当前Bean的name从 singletonsCurrentlyInCreation （正在被创建的Bean）中清除。
将这个Bean添加到 singletonObjects （一级缓存）中，createBean 方法彻底完成。
*/
protected void afterSingletonCreation(String beanName) {
    if (!this.inCreationCheckExclusions.contains(beanName) && !this.singletonsCurrentlyInCreation.remove(beanName)) {
        throw new IllegalStateException("Singleton '" + beanName + "' isn't currently in creation");
    }
}
```

11.8 最后的工作

```java
    // Trigger post-initialization callback for all applicable beans...
	// 又回调了一组类型为 SmartInitializingSingleton 的组件，来回调它们的 afterSingletonsInstantiated 方法。
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
```

11.8.1 SmartInitializingSingleton

> Callback interface triggered at the end of the singleton pre-instantiation phase during BeanFactory bootstrap. This interface can be implemented by singleton beans in order to perform some initialization after the regular singleton instantiation algorithm, avoiding side effects with accidental early initialization (e.g. from ListableBeanFactory.getBeansOfType calls). In that sense, it is an alternative to InitializingBean which gets triggered right at the end of a bean's local construction phase. This callback variant is somewhat similar to org.springframework.context.event.ContextRefreshedEvent but doesn't require an implementation of org.springframework.context.ApplicationListener, with no need to filter context references across a context hierarchy etc. It also implies a more minimal dependency on just the beans package and is being honored by standalone ListableBeanFactory implementations, not just in an org.springframework.context.ApplicationContext environment. NOTE: If you intend to start/manage asynchronous tasks, preferably implement org.springframework.context.Lifecycle instead which offers a richer model for runtime management and allows for phased startup/shutdown.
>
> 在 `BeanFactory` 引导期间的单实例bean的初始化阶段结束时触发的回调接口。该接口可以由单例bean实现，以便在常规的单例实例化算法之后执行一些初始化，避免意外的早期初始化带来的副作用（例如，来自 `ListableBeanFactory.getBeansOfType` 调用）。从这个意义上讲，它是 `InitializingBean` 的替代方法，后者在bean的本地构造阶段结束时立即触发。 这个回调变体有点类似于 `org.springframework.context.event.ContextRefreshedEvent`，但是不需要 `org.springframework.context.ApplicationListener` 的实现，不需要在整个上下文层次结构中过滤上下文引用。这也意味着更多对bean包的依赖性最小，并且由独立的ListableBeanFactory实现兑现，而不仅仅是在 `ApplicationContext` 环境中。 注意：如果要启动/管理异步任务，则最好实现 `org.springframework.context.Lifecycle`，它为运行时管理提供了更丰富的模型，并允许分阶段启动/关闭。