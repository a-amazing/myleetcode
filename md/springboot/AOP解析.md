## 1. 注解AOP基础

### 1.1 AOP的通知类型

- `@Before`：前置通知（logStart）
- `@After`：后置通知（logEnd）
- `@AfterReturning`：返回通知（logReturn，方法正常返回）
- `@AfterThrowing`：异常通知（logException，方法抛出异常）
- `@Around`：环绕通知（编程式推进目标方法运行）

前四种注解都属于**声明式AOP**，`@Around` 属于**编程式AOP**。

### 1.2 AOP的重要概念

在开始研究AOP原理之前，小伙伴们咱一起回顾下AOP的重要核心概念术语：

- **JoinPoint**（连接点）：可能被拦截到的点，在Spring中指的是类中的任意方法（SpringFramework 只支持方法类型的连接点）

- **Pointcut**（切入点）：要对哪些 **JoinPoint** 进行拦截的定义（可以简单理解为已经被增强的方法）（哪些切入点要被增强，需要由切入点表达式来描述）

- Advice

  （通知 / 增强）：拦截到

   

  JoinPoint

   

  之后所要做的事情（额外要执行的代码）

  - 通知的类型就是上面所说的5种

- **Target**（目标对象）：需要被代理的目标对象

- **Introduction**（引入）：一种特殊的通知，它能在不修改原有类代码的前提下，在运行期为原始类动态地添加一些属性或方法

- **Weaving**（织入）：把增强应用到目标对象，创建代理对象的过程（SpringAOP用动态代理织入，而 Aspect 可以采用编译期织入和类装载期织入）

- **Proxy**（代理）：一个类被AOP织入增强后，就产生一个结果代理类

- **Aspect**（切面）：**切入点**和**通知**的结合

---

AOP代码示例如下:

```java
@Aspect
@Component
public class LogAspect {
    
    // 切入com.example.demo下面的一级包下面的所有类的所有方法
    @Before("execution(public * com.example.demo.*.*(..))")
    public void doBefore(JoinPoint joinPoint) {
        System.out.println("doBefore run...");
    }
    
    // 切入被@LogBack标注的方法
    @After("@annotation(com.example.demo.LogBack)")
    public void doAfter(JoinPoint joinPoint) {
        System.out.println("doAfter run...");
    }
    
    // 切入com.example.demo.service.DemoService类的所有方法中第一个参数为Serializable类型的方法
    @AfterReturning(value = "execution(public * com.example.demo.service.DemoService.*(java.io.Serializable, ..))", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Object result) {
        System.out.println("doAfterReturning run, result: " + result);
    }
    
    // 切入com.example.demo下所有的controller包下面的所有类的所有方法
    @AfterThrowing(value = "execution(public * com.example.demo..controller.*(..))", throwing = "ex")
    public void doAfterThrowing(JoinPoint joinPoint, Exception ex) {
        System.out.println("doAfterThrowing catch exception: " + ex.getMessage());
    }
    
    // 切入com.example.demo.controller.DemoController的所有返回值为String的方法
    @Around("execution(public String com.example.demo.controller.DemoController.*(..))")
    public Object doAround(ProceedingJoinPoint joinPoint) {
        System.out.println("doAround run...");
        Object result = null;
        try {
            System.out.println("method before invoke...");
            result = joinPoint.proceed();
            System.out.println("method invoked, result: " + result);
        } catch (Throwable throwable) {
            System.out.println("method throws Exception: " + throwable.getMessage());
            throwable.printStackTrace();
        }
        return result;
    }
    
}
```

## 2. @EnableAspectJAutoProxy的作用

```java
@Import(AspectJAutoProxyRegistrar.class)
public @interface EnableAspectJAutoProxy {

    boolean proxyTargetClass() default false;

    boolean exposeProxy() default false;

}
```

注解标注了 `@Import` ，它导入了一个 `AspectJAutoProxyRegistrar` 。

> Registers an AnnotationAwareAspectJAutoProxyCreator against the current BeanDefinitionRegistry as appropriate based on a given @EnableAspectJAutoProxy annotation.
>
> 根据给定的 `@EnableAspectJAutoProxy` 注解，根据当前 `BeanDefinitionRegistry` 在适当的位置注册 `AnnotationAwareAspectJAutoProxyCreator` 。

要在IOC容器中注册一个 `AnnotationAwareAspectJAutoProxyCreator` 。

### 2.1 AspectJAutoProxyRegistrar

```java
class AspectJAutoProxyRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(
            AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AopConfigUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(registry);

        AnnotationAttributes enableAspectJAutoProxy =
                AnnotationConfigUtils.attributesFor(importingClassMetadata, EnableAspectJAutoProxy.class);
        if (enableAspectJAutoProxy != null) {
            if (enableAspectJAutoProxy.getBoolean("proxyTargetClass")) {
                AopConfigUtils.forceAutoProxyCreatorToUseClassProxying(registry);
            }
            // 强制暴露代理
            if (enableAspectJAutoProxy.getBoolean("exposeProxy")) {
                AopConfigUtils.forceAutoProxyCreatorToExposeProxy(registry);
            }
        }
    }

}
```

它实现了 `ImportBeanDefinitionRegistrar` 接口，会编程式的向IOC容器中注册组件。下面的 `registerBeanDefinitions` 方法中分为两个步骤：注册 `AspectJAnnotationAutoProxyCreator` ，解析 `@EnableAspectJAutoProxy` 注解。

#### 2.1.1 AopConfigUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary

```java
@Nullable
public static BeanDefinition registerAspectJAnnotationAutoProxyCreatorIfNecessary(BeanDefinitionRegistry registry) {
    return registerAspectJAnnotationAutoProxyCreatorIfNecessary(registry, null);
}

@Nullable
public static BeanDefinition registerAspectJAnnotationAutoProxyCreatorIfNecessary(
        BeanDefinitionRegistry registry, @Nullable Object source) {
    // 注意在这个方法中已经把AnnotationAwareAspectJAutoProxyCreator的字节码传入方法了
    return registerOrEscalateApcAsRequired(AnnotationAwareAspectJAutoProxyCreator.class, registry, source);
}

public static final String AUTO_PROXY_CREATOR_BEAN_NAME =
        "org.springframework.aop.config.internalAutoProxyCreator";

private static BeanDefinition registerOrEscalateApcAsRequired(
        Class<?> cls, BeanDefinitionRegistry registry, @Nullable Object source) {

    Assert.notNull(registry, "BeanDefinitionRegistry must not be null");

    //方法进入后先判断IOC容器中是否包含一个特定的Bean，如果没有，下面直接用 RootBeanDefinition 创建。
    if (registry.containsBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME)) {
        BeanDefinition apcDefinition = registry.getBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME);
        if (!cls.getName().equals(apcDefinition.getBeanClassName())) {
            int currentPriority = findPriorityForClass(apcDefinition.getBeanClassName());
            int requiredPriority = findPriorityForClass(cls);
            if (currentPriority < requiredPriority) {
                apcDefinition.setBeanClassName(cls.getName());
            }
        }
        return null;
    }

    RootBeanDefinition beanDefinition = new RootBeanDefinition(cls);
    beanDefinition.setSource(source);
    beanDefinition.getPropertyValues().add("order", Ordered.HIGHEST_PRECEDENCE);
    beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
    registry.registerBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME, beanDefinition);
    return beanDefinition;
}
```

#### 2.1.2 解析@EnableAspectJAutoProxy注解

```java
AnnotationAttributes enableAspectJAutoProxy =
            AnnotationConfigUtils.attributesFor(importingClassMetadata, EnableAspectJAutoProxy.class);
    if (enableAspectJAutoProxy != null) {
        if (enableAspectJAutoProxy.getBoolean("proxyTargetClass")) {
            AopConfigUtils.forceAutoProxyCreatorToUseClassProxying(registry);
        }
        if (enableAspectJAutoProxy.getBoolean("exposeProxy")) {
            AopConfigUtils.forceAutoProxyCreatorToExposeProxy(registry);
        }
    }
```

因为默认的 `@EnableAspectJAutoProxy` 中两个属性默认均为false，故这部分不起作用。

至此，可以发现，`@EnableAspectJAutoProxy` 的根本作用是在IOC容器中注册了一个 `AnnotationAwareAspectJAutoProxyCreator` 。

## 3. AnnotationAwareAspectJAutoProxyCreator的作用时机

> AspectJAwareAdvisorAutoProxyCreator subclass that processes all AspectJ annotation aspects in the current application context, as well as Spring Advisors. Any AspectJ annotated classes will automatically be recognized, and their advice applied if Spring AOP's proxy-based model is capable of applying it. This covers method execution joinpoints. If the `<aop:include>` element is used, only @AspectJ beans with names matched by an include pattern will be considered as defining aspects to use for Spring auto-proxying. Processing of Spring Advisors follows the rules established in org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator.
>
> `AspectJAwareAdvisorAutoProxyCreator`子类，用于处理当前应用程序上下文中的所有 `@AspectJ` 注解的切面，以及Spring的Advisor。
>
> 如果Spring AOP的基于代理的模型能够应用任何被 `@AspectJ` 注解标注的类，那么它们的增强方法将被自动识别。这涵盖了方法执行的切入点表达式。
>
> 如果使用`<aop:include>`元素，则只有名称与包含模式匹配的被 @AspectJ 标注的Bean将被视为定义要用于Spring自动代理的方面。
>
> Spring Advisor的处理遵循 `AbstractAdvisorAutoProxyCreator` 中建立的规则。

继承结构如下:

```java
public class AnnotationAwareAspectJAutoProxyCreator 
    extends AspectJAwareAdvisorAutoProxyCreator

public class AspectJAwareAdvisorAutoProxyCreator 
    extends AbstractAdvisorAutoProxyCreator

public abstract class AbstractAdvisorAutoProxyCreator 
    extends AbstractAutoProxyCreator

public abstract class AbstractAutoProxyCreator 
    extends ProxyProcessorSupport 
    implements SmartInstantiationAwareBeanPostProcessor, BeanFactoryAware

public class ProxyProcessorSupport 
    extends ProxyConfig 
    implements Ordered, BeanClassLoaderAware, AopInfrastructureBean
```

- 实现了 **`SmartInstantiationAwareBeanPostProcessor`** ，可以做组件的 **创建前后、初始化前后的后置处理工作** 。
- 实现了 **`BeanFactoryAware`** ，可以将 **`BeanFactory`** 注入到组件中

### 3.0 SmartInstantiationAwareBeanPostProcessor

> Extension of the InstantiationAwareBeanPostProcessor interface, adding a callback for predicting the eventual type of a processed bean. NOTE: This interface is a special purpose interface, mainly for internal use within the framework. In general, application-provided post-processors should simply implement the plain BeanPostProcessor interface or derive from the InstantiationAwareBeanPostProcessorAdapter class.
>
> 扩展 `InstantiationAwareBeanPostProcessor` 接口，添加了用于预测已处理bean的最终类型的回调。 注意：此接口是专用接口，主要供框架内部使用。通常，应用程序提供的后处理器应简单地实现纯 `BeanPostProcessor` 接口或从 `InstantiationAwareBeanPostProcessorAdapter` 类派生。

它扩展了 `InstantiationAwareBeanPostProcessor` 接口，这个接口之前咱在IOC部分介绍过，它用于组件的创建前后做后置处理，恰好AOP的核心是用代理对象代替普通对象，用这种后置处理器刚好能完成需求。

### 3.1 SpringBoot引导创建IOC容器

根据前面的SpringBoot启动IOC容器的原理，最终会在一系列初始化后进入IOC容器的refresh方法。

```java
// AbstractApplicationContext
public void refresh() throws BeansException, IllegalStateException {
    synchronized (this.startupShutdownMonitor) {
        // ......

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

            // ......
    }
}
```

### 3.2 registerBeanPostProcessors

（省略了部分与AOP无关的源码）

```java
protected void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory) {
    PostProcessorRegistrationDelegate.registerBeanPostProcessors(beanFactory, this);
}

public static void registerBeanPostProcessors(
        ConfigurableListableBeanFactory beanFactory, AbstractApplicationContext applicationContext) {

    // ......
    // Separate between BeanPostProcessors that implement PriorityOrdered,
    // Ordered, and the rest.
    List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
    List<BeanPostProcessor> internalPostProcessors = new ArrayList<>();
    List<String> orderedPostProcessorNames = new ArrayList<>();
    List<String> nonOrderedPostProcessorNames = new ArrayList<>();
    // 根据PriorityOrdered、Ordered接口，对这些BeanPostProcessor进行归类
    for (String ppName : postProcessorNames) {
        if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
            BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
            priorityOrderedPostProcessors.add(pp);
            // MergedBeanDefinitionPostProcessor类型的后置处理器被单独放在一个集合中，说明该接口比较特殊
            if (pp instanceof MergedBeanDefinitionPostProcessor) {
                internalPostProcessors.add(pp);
            }
        }
        else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
            orderedPostProcessorNames.add(ppName);
        }
        else {
            nonOrderedPostProcessorNames.add(ppName);
        }
    }

    // ......

    // Next, register the BeanPostProcessors that implement Ordered.
    // 注册实现了Ordered接口的BeanPostProcessor
    // AnnotationAwareAspectJAutoProxyCreator实现了Ordered接口,实际分类时进入 orderedPostProcessorNames 集合中
    List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>();
    for (String ppName : orderedPostProcessorNames) {
        BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
        orderedPostProcessors.add(pp);
        if (pp instanceof MergedBeanDefinitionPostProcessor) {
            internalPostProcessors.add(pp);
        }
    }
    sortPostProcessors(orderedPostProcessors, beanFactory);
    registerBeanPostProcessors(beanFactory, orderedPostProcessors);

    // ......
}
```

### 3.3 getBean → doCreateBean

根据前面的IOC原理，肯定会执行一系列操作：**getBean → doGetBean → createBean → doCreateBean** 。最终创建这个后置处理器，放入IOC容器中

## 小结

1. 注解AOP的使用需要在切面类上标注 `@Aspect` 和 `@Component` 。
2. 启动AOP的核心是向容器中注册了一个 `AnnotationAwareAspectJAutoProxyCreator` 。

## 4. AnnotationAwareAspectJAutoProxyCreator的后置处理功能

### 4.1 refresh → createBean

这部分依次走 **refresh → finishBeanFactoryInitialization → preInstantiateSingletons → getBean → doGetBean → createBean** 方法。

```java
protected Object createBean(String beanName, RootBeanDefinition mbd, @Nullable Object[] args)
        throws BeanCreationException {

    // ......

    try {
        // Give BeanPostProcessors a chance to return a proxy instead of the target bean instance.
        // 给后置处理器一个机会返回一个代理对象
        Object bean = resolveBeforeInstantiation(beanName, mbdToUse);
        if (bean != null) {
            return bean;
        }
    }
    catch (Throwable ex) {
        throw new BeanCreationException(mbdToUse.getResourceDescription(), beanName,
                "BeanPostProcessor before instantiation of bean failed", ex);
    }

    // doCreateBean ......
}
```

### 4.2 resolveBeforeInstantiation

```java
protected Object resolveBeforeInstantiation(String beanName, RootBeanDefinition mbd) {
    Object bean = null;
    if (!Boolean.FALSE.equals(mbd.beforeInstantiationResolved)) {
        // Make sure bean class is actually resolved at this point.
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

这段源码中先检查是否有 `InstantiationAwareBeanPostProcessor` ，如果有，就调用 `applyBeanPostProcessorsBeforeInstantiation` 方法，给这些后置处理器机会，让它创建真正的代理对象。

### 4.3 applyBeanPostProcessorsBeforeInstantiation

```java
protected Object applyBeanPostProcessorsBeforeInstantiation(Class<?> beanClass, String beanName) {
    for (BeanPostProcessor bp : getBeanPostProcessors()) {
        if (bp instanceof InstantiationAwareBeanPostProcessor) {
            InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
            Object result = ibp.postProcessBeforeInstantiation(beanClass, beanName);
            if (result != null) {
                return result;
            }
        }
    }
    return null;
}
```

### 4.4 AbstractAutoProxyCreator#postProcessBeforeInstantiation

```java
public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) {
    // 缓存机制
    Object cacheKey = getCacheKey(beanClass, beanName);

  
    if (!StringUtils.hasLength(beanName) || !this.targetSourcedBeans.contains(beanName)) {
        // 判断该Bean是否已经被增强（advisedBeans为已经增强过的Bean）
        if (this.advisedBeans.containsKey(cacheKey)) {
            return null;
        }
        // 4.4.1, 4.4.2 判断是否为基础类型（isInfrastructureClass），或者需要跳过的Bean
        if (isInfrastructureClass(beanClass) || shouldSkip(beanClass, beanName)) {
            this.advisedBeans.put(cacheKey, Boolean.FALSE);
            return null;
        }
    }

    // Create proxy here if we have a custom TargetSource.
    // Suppresses unnecessary default instantiation of the target bean:
    // The TargetSource will handle target instances in a custom fashion.
    // 如果我们有一个自定义的TargetSource，则在此处创建代理。
    // 这段源码要抑制目标bean的不必要的默认实例化：TargetSource将以自定义方式处理目标实例。
    // 4.4.3 自定义目标资源，对于单实例Bean必定会返回null
    TargetSource targetSource = getCustomTargetSource(beanClass, beanName);
    if (targetSource != null) {
        if (StringUtils.hasLength(beanName)) {
            this.targetSourcedBeans.add(beanName);
        }
        Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(beanClass, beanName, targetSource);
        Object proxy = createProxy(beanClass, beanName, specificInterceptors, targetSource);
        this.proxyTypes.put(cacheKey, proxy.getClass());
        return proxy;
    }

    return null;
}
```

#### 4.4.1 isInfrastructureClass：判断Bean是否为基础类型

```java
// AnnotationAwareAspectJAutoProxyCreator
protected boolean isInfrastructureClass(Class<?> beanClass) {
    // 一大段文档注释
    return (super.isInfrastructureClass(beanClass) ||
            (this.aspectJAdvisorFactory != null && this.aspectJAdvisorFactory.isAspect(beanClass)));
}

// AbstractAutoProxyCreator
protected boolean isInfrastructureClass(Class<?> beanClass) {
    boolean retVal = Advice.class.isAssignableFrom(beanClass) ||
            Pointcut.class.isAssignableFrom(beanClass) ||
            Advisor.class.isAssignableFrom(beanClass) ||
            AopInfrastructureBean.class.isAssignableFrom(beanClass);
    if (retVal && logger.isTraceEnabled()) {
        logger.trace("Did not attempt to auto-proxy infrastructure class [" + beanClass.getName() + "]");
    }
    return retVal;
}
```

在 上有一大串单行注释，单独摘到下面，咱一块来看：

> Previously we setProxyTargetClass(true) in the constructor, but that has too broad an impact. Instead we now override isInfrastructureClass to avoid proxying aspects. I'm not entirely happy with that as there is no good reason not to advise aspects, except that it causes advice invocation to go through a proxy, and if the aspect implements e.g the Ordered interface it will be proxied by that interface and fail at runtime as the advice method is not defined on the interface. We could potentially relax the restriction about not advising aspects in the future.
>
> 以前我们在构造函数中有 `setProxyTargetClass(true)`，但是影响范围太广。相反，我们现在重写 `isInfrastructureClass` 方法，以避免代理切面。我对此并不完全满意，因为没有充分的理由不增强那些切面，只是它会导致增强方法只能通过代理调用，并且如果方面实现了例如 `Ordered` 接口，它将被该接口代理并在以下位置失败运行时，因为未在切面上定义增强方法。我们将来可能会放宽对非增强切面的限制。

这个方法会调用到父类的方法，而父类的方法会判断Bean的class是否为一些指定的类型（`Advice` 、`PointCut` 、`Advisor` 、`AopInfrastructureBean`）的子类。很显然我们在用注解AOP的时候都是打 `@Aspect` 注解，没有继承操作，故这部分返回false。

后面还有一段：`this.aspectJAdvisorFactory.isAspect(beanClass)` ：

```java
public boolean isAspect(Class<?> clazz) {
    return (hasAspectAnnotation(clazz) && !compiledByAjc(clazz));
}

private boolean hasAspectAnnotation(Class<?> clazz) {
    return (AnnotationUtils.findAnnotation(clazz, Aspect.class) != null);
}
```

判断是否标注了@Aspect注解

#### 4.4.2 shouldSkip：Bean是否需要跳过

一个很重要的操作在这里面一起进行了：**创建增强器**。

```java
// AspectJAwareAdvisorAutoProxyCreator
protected boolean shouldSkip(Class<?> beanClass, String beanName) {
    // TODO: Consider optimization by caching the list of the aspect names
    List<Advisor> candidateAdvisors = findCandidateAdvisors();
    for (Advisor advisor : candidateAdvisors) {
        if (advisor instanceof AspectJPointcutAdvisor &&
                ((AspectJPointcutAdvisor) advisor).getAspectName().equals(beanName)) {
            return true;
        }
    }
    return super.shouldSkip(beanClass, beanName);
}

// AbstractAutoProxyCreator
protected boolean shouldSkip(Class<?> beanClass, String beanName) {
    return AutoProxyUtils.isOriginalInstance(beanName, beanClass);
}
```

上面的方法是扩展了下面父类的方法。父类的方法很简单，它就是判断**目标对象是不是原始对象**（没有经过代理）

##### 4.4.2.1 findCandidateAdvisors：加载增强器

```java
// AnnotationAwareAspectJAutoProxyCreator
protected List<Advisor> findCandidateAdvisors() {
    // Add all the Spring advisors found according to superclass rules.
    // 添加所有根据父类的规则找到的Spring的增强器
    List<Advisor> advisors = super.findCandidateAdvisors();
    // Build Advisors for all AspectJ aspects in the bean factory.
    // 给所有BeanFactory中的AspectJ切面构建增强器
    if (this.aspectJAdvisorsBuilder != null) {
        advisors.addAll(this.aspectJAdvisorsBuilder.buildAspectJAdvisors());
    }
    return advisors;
}

// AbstractAdvisorAutoProxyCreator
protected List<Advisor> findCandidateAdvisors() {
    Assert.state(this.advisorRetrievalHelper != null, "No BeanFactoryAdvisorRetrievalHelper available");
    // 4.4.2.2 获取、创建增强器的Bean
    return this.advisorRetrievalHelper.findAdvisorBeans();
}
```

先调父类的方法取出一组增强器，再从IOC容器中找出所有标注 `@Aspect` 的组件一起添加上。

##### 4.4.2.2 advisorRetrievalHelper.findAdvisorBeans：获取、创建增强器的Bean

父类获取所有增强器的方法

```java
public List<Advisor> findAdvisorBeans() {
    // Determine list of advisor bean names, if not cached already.
    // 确定增强器bean名称的列表（如果尚未缓存）
    String[] advisorNames = this.cachedAdvisorBeanNames;
    if (advisorNames == null) {
        // Do not initialize FactoryBeans here: We need to leave all regular beans
        // uninitialized to let the auto-proxy creator apply to them!
        // 不要在这里初始化FactoryBeans：我们需要保留所有未初始化的常规bean，以使自动代理创建者对其应用
        // 其实这里只是把那些类型为Advisor的Bean都找出来而已
        advisorNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
                this.beanFactory, Advisor.class, true, false);
        this.cachedAdvisorBeanNames = advisorNames;
    }
    // 如果当前IOC容器中没有任何增强器类Bean，直接返回
    if (advisorNames.length == 0) {
        return new ArrayList<>();
    }

    List<Advisor> advisors = new ArrayList<>();
    // 有增强器类Bean，循环它们
    for (String name : advisorNames) {
        if (isEligibleBean(name)) {
            if (this.beanFactory.isCurrentlyInCreation(name)) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Skipping currently created advisor '" + name + "'");
                }
            }
            else {
                try {
                    // 利用getBean把这些增强器先创建出来
                    advisors.add(this.beanFactory.getBean(name, Advisor.class));
                }
                catch (BeanCreationException ex) {
                    Throwable rootCause = ex.getMostSpecificCause();
                    if (rootCause instanceof BeanCurrentlyInCreationException) {
                        BeanCreationException bce = (BeanCreationException) rootCause;
                        String bceBeanName = bce.getBeanName();
                        // 这里可能也会引发循环依赖，如果这里正在创建这个增强器了，直接continue
                        if (bceBeanName != null && this.beanFactory.isCurrentlyInCreation(bceBeanName)) {
                            if (logger.isTraceEnabled()) {
                                logger.trace("Skipping advisor '" + name +
                                        "' with dependency on currently created bean: " + ex.getMessage());
                            }
                            // Ignore: indicates a reference back to the bean we're trying to advise.
                            // We want to find advisors other than the currently created bean itself.
                            continue;
                        }
                    }
                    throw ex;
                }
            }
        }
    }
    return advisors;
}
```

把那些增强器先创建、初始化出来，放入IOC容器中就完事了。

---

准备构建增强器了，它要调 `aspectJAdvisorsBuilder.buildAspectJAdvisors` 方法：

```java
protected List<Advisor> findCandidateAdvisors() {
    // 添加所有根据父类的规则找到的Spring的增强器
    List<Advisor> advisors = super.findCandidateAdvisors();
    // Build Advisors for all AspectJ aspects in the bean factory.
    // 给所有BeanFactory中的AspectJ切面构建增强器
    if (this.aspectJAdvisorsBuilder != null) {
        advisors.addAll(this.aspectJAdvisorsBuilder.buildAspectJAdvisors());
    }
    return advisors;
}
```

##### 4.4.2.3 【创建增强器】aspectJAdvisorsBuilder.buildAspectJAdvisors

```java
// 在当前的BeanFactory中查找带有@AspectJ注解的切面类Bean，然后返回代表它们的增强器列表。为每个AspectJ通知方法创建一个增强器
public List<Advisor> buildAspectJAdvisors() {
    List<String> aspectNames = this.aspectBeanNames;

    // 提取增强通知
    if (aspectNames == null) {
        synchronized (this) {
            aspectNames = this.aspectBeanNames;
            if (aspectNames == null) {
                List<Advisor> advisors = new ArrayList<>();
                aspectNames = new ArrayList<>();
                // 获取IOC容器中的所有Bean
                String[] beanNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
                        this.beanFactory, Object.class, true, false);
                for (String beanName : beanNames) {
                    if (!isEligibleBean(beanName)) {
                        continue;
                    }
                    // We must be careful not to instantiate beans eagerly as in this case they
                    // would be cached by the Spring container but would not have been weaved.
                    // 我们必须小心，不要急于实例化bean，因为在这种情况下，IOC容器会缓存它们，但不会被织入增强器
                    // 这一部分的功能是在不创建Bean的情况下获取Bean的类型，防止因为增强器还没有创建，导致对象没有被成功代理
                    Class<?> beanType = this.beanFactory.getType(beanName);
                    if (beanType == null) {
                        continue;
                    }
                    // 如果当前循环的Bean是一个切面类
                    if (this.advisorFactory.isAspect(beanType)) {
                        aspectNames.add(beanName);
                        // 包装@Aspect注解的元数据
                        AspectMetadata amd = new AspectMetadata(beanType, beanName);
                        // 默认使用单实例创建切面类
                        if (amd.getAjType().getPerClause().getKind() == PerClauseKind.SINGLETON) {
                            MetadataAwareAspectInstanceFactory factory =
                                    new BeanFactoryAspectInstanceFactory(this.beanFactory, beanName);
                            // 4.4.2.4 如果切面类是一个单实例Bean，则会缓存所有增强器
                            List<Advisor> classAdvisors = this.advisorFactory.getAdvisors(factory);
                            if (this.beanFactory.isSingleton(beanName)) {
                                this.advisorsCache.put(beanName, classAdvisors);
                            }
                            // 否则只会缓存增强器创建工厂，由增强器工厂来创建增强器
                            else {
                                this.aspectFactoryCache.put(beanName, factory);
                            }
                            advisors.addAll(classAdvisors);
                        }
                        else {
                            // Per target or per this.
                            if (this.beanFactory.isSingleton(beanName)) {
                                throw new IllegalArgumentException("Bean with name '" + beanName +
                                        "' is a singleton, but aspect instantiation model is not singleton");
                            }
                            MetadataAwareAspectInstanceFactory factory =
                                    new PrototypeAspectInstanceFactory(this.beanFactory, beanName);
                            this.aspectFactoryCache.put(beanName, factory);
                            advisors.addAll(this.advisorFactory.getAdvisors(factory));
                        }
                    }
                }
                this.aspectBeanNames = aspectNames;
                return advisors;
            }
        }
    }

    // 如果aspectNames不为null，证明之前已经创建过了，直接读缓存即可
    if (aspectNames.isEmpty()) {
        return Collections.emptyList();
    }
    List<Advisor> advisors = new ArrayList<>();
    for (String aspectName : aspectNames) {
        List<Advisor> cachedAdvisors = this.advisorsCache.get(aspectName);
        if (cachedAdvisors != null) {
            advisors.addAll(cachedAdvisors);
        }
        else {
            MetadataAwareAspectInstanceFactory factory = this.aspectFactoryCache.get(aspectName);
            advisors.addAll(this.advisorFactory.getAdvisors(factory));
        }
    }
    return advisors;
}
```

概括一下上面的增强器创建思路：

1. 获取IOC容器中的所有Bean
2. 从所有的Bean中找带有 `@Aspect` 注解的Bean
3. 根据Bean中定义的通知（Advice，即被五种通知类型标注的方法），创建增强器
4. 将增强器放入缓存，以备后续加载

注意源码中有一步我标注了序号：`advisorFactory.getAdvisors` ：

##### 4.4.2.4 advisorFactory.getAdvisors：缓存Bean中的所有增强器

```java
public List<Advisor> getAdvisors(MetadataAwareAspectInstanceFactory aspectInstanceFactory) {
    // 目标Aspect类
    Class<?> aspectClass = aspectInstanceFactory.getAspectMetadata().getAspectClass();
    // 代理对象Bean的name
    String aspectName = aspectInstanceFactory.getAspectMetadata().getAspectName();
    // 校验Aspect类上是不是标注了@Aspect注解
    validate(aspectClass);

    // We need to wrap the MetadataAwareAspectInstanceFactory with a decorator
    // so that it will only instantiate once.
    // 我们需要用装饰器包装MetadataAwareAspectInstanceFactory，使其仅实例化一次
    // 这部分是使用了装饰者模式，把aspectInstanceFactory包装起来，保证增强器不会多次实例化
    MetadataAwareAspectInstanceFactory lazySingletonAspectInstanceFactory =
            new LazySingletonAspectInstanceFactoryDecorator(aspectInstanceFactory);

    // 筛选没有标注@Pointcut注解的方法，并创建增强器
    List<Advisor> advisors = new ArrayList<>();
    for (Method method : getAdvisorMethods(aspectClass)) {
        // 4.4.2.5 真正创建增强器
        Advisor advisor = getAdvisor(method, lazySingletonAspectInstanceFactory, advisors.size(), aspectName);
        if (advisor != null) {
            advisors.add(advisor);
        }
    }

    // If it's a per target aspect, emit the dummy instantiating aspect.
    // 通过在装饰者内部的开始加入SyntheticInstantiationAdvisor增强器，达到延迟初始化切面bean的目的
    if (!advisors.isEmpty() && lazySingletonAspectInstanceFactory.getAspectMetadata().isLazilyInstantiated()) {
        Advisor instantiationAdvisor = new SyntheticInstantiationAdvisor(lazySingletonAspectInstanceFactory);
        advisors.add(0, instantiationAdvisor);
    }

    // Find introduction fields.
    // 对@DeclareParent注解功能的支持（引入）
    for (Field field : aspectClass.getDeclaredFields()) {
        Advisor advisor = getDeclareParentsAdvisor(field);
        if (advisor != null) {
            advisors.add(advisor);
        }
    }

    return advisors;
}
```

核心就是中间部分的 `getAdvisor`：获取/创建增强器（可以类比**getBean**）。

##### 4.4.2.5 getAdvisor：真正创建增强器

```java
public Advisor getAdvisor(Method candidateAdviceMethod, MetadataAwareAspectInstanceFactory aspectInstanceFactory,
        int declarationOrderInAspect, String aspectName) {
    // 校验@Aspect注解等
    validate(aspectInstanceFactory.getAspectMetadata().getAspectClass());

    // 4.4.2.6 解析切入点
    AspectJExpressionPointcut expressionPointcut = getPointcut(
            candidateAdviceMethod, aspectInstanceFactory.getAspectMetadata().getAspectClass());
    if (expressionPointcut == null) {
        return null;
    }

    // 将切入点和通知包装成一个切面
    return new InstantiationModelAwarePointcutAdvisorImpl(expressionPointcut, candidateAdviceMethod,
            this, aspectInstanceFactory, declarationOrderInAspect, aspectName);
}
```

先是解析切入点，之后把**切入点**和**通知**包装成一个**切面**（回想AOP的术语）。

即将怎么过滤类和方法以及需要怎么处理封装为一个切面对象

【切入点】`AspectJExpressionPointcut` 的类结构与实现

```java
public class AspectJExpressionPointcut extends AbstractExpressionPointcut
		implements ClassFilter, IntroductionAwareMethodMatcher, BeanFactoryAware
    //即需要类型匹配,也需要方法匹配
```

