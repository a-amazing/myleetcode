### spring事务机制

#### 事务定义



#### 踩坑记录

##### 事务不生效

使用 Spring 注解方式进行事务管理，即在 Action 类的数据库操作方法上加 @Transactional 注解，却发现在实践中不起作用。经过排查后发现， Spring 的事务注解是靠 AOP 切面实现的。在对象内部的方法中调用该对象其他使用 AOP 注解的方法，被调用方法的 AOP 注解会失效。因为同一个类的内部代码调用中，不会走代理类。

**解决方案**

- 通过ThreadLocal暴露aop代理对象

  1. 开启暴露op代理到ThreadLocal支持（如下配置方式从spring3开始支持）

     ```java
     <aop:aspectj-autoproxy expose-proxy="true"/><!—注解风格支持-->  
     <aop:config expose-proxy="true"><!—xml风格支持--> 
     ```

  2. 修改业务实现类(ServiceImpl)

     ~~this.b();~~

     //修改为

     ((AService) AopContext.currentProxy()).b();

     

- 通过初始化方法在目标对象中注入代理对象

```java
@Service  
public class AServiceImpl3 implements AService{  
    @Autowired  //①  注入上下文  
    private ApplicationContext context;  
      
    private AService proxySelf; //②  表示代理对象，不是目标对象  
    @PostConstruct  //③ 初始化方法  
    private void setSelf() {  
        //从上下文获取代理对象（如果通过proxtSelf=this是不对的，this是目标对象）  
        //此种方法不适合于prototype Bean，因为每次getBean返回一个新的Bean  
        proxySelf = context.getBean(AService.class);   
    }  
    @Transactional(propagation = Propagation.REQUIRED)  
    public void a() {  
       proxySelf.b(); //④ 调用代理对象的方法 这样可以执行事务切面  
    }  
    @Transactional(propagation = Propagation.REQUIRES_NEW)  
    public void b() {  
    }  
}  
```

- 通过BeanPostProcessor在目标对象中注入代理对象

  1. 定义一个BeanSelfAware接口，实现了此接口的程序表明需要注入代理后的对象到自身

  ```java
  public class SomeServiceImpl implements SomeService,BeanSelfAware  {  
    
      private SomeService self;//AOP增强后的代理对象
    
      //实现BeanSelfAware接口
      public void setSelf(Object proxyBean)  {  
          this.self = (SomeService)proxyBean  
      }  
    
      public void someMethod()  {  
          someInnerMethod();//注意这句，通过self这个对象，而不是直接调用的  
          //foo...  
      }  
    
      public void someInnerMethod()  {  
          //bar...  
      }  
  }  
  ```

  2. 定义一个BeanPostProcessor，beanFactory中的每个Bean初始化完毕后，调用所有BeanSelfAware的setSelf方法，把自身的代理对象注入自身

  ```java
  public class InjectBeanSelfProcessor implements BeanPostProcessor  {  
     
      public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException  {
          if(bean instanceof BeanSelfAware)  
          {
              System.out.println("inject proxy：" + bean.getClass());  
              BeanSelfAware myBean = (BeanSelfAware)bean;  
              myBean.setSelf(bean);  
              return myBean;  
          }  
          return bean;  
      }  
     
      public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException  {  
          return bean; 
      }  
  }  
  ```

  3. 在BeanFactory配置中组合起来，只需要把BeanPostProcesser加进去就可以了

  ```xml
  <!-- 注入代理后的bean到bean自身的BeanPostProcessor... -->  
  <bean class=" org.mypackage.InjectBeanSelfProcessor"></bean>  
    
  <bean id="someServiceTarget" class="org.mypackage.SomeServiceImpl" />   
    
  <bean id="someService" class="org.springframework.aop.framework.ProxyFactoryBean">  
      <property name="target">  
          <ref local="someServiceTarget" />  
      </property>  
      <property name="interceptorNames">  
          <list>  
              <value>someAdvisor</value>  
          </list>  
      </property>  
  </bean>  
    
  <!-- 调用spring的DebugInterceptor记录日志,以确定方法是否被AOP增强 -->  
  <bean id="debugInterceptor" class="org.springframework.aop.interceptor.DebugInterceptor" />  
    
  <bean id="someAdvisor" class="org.springframework.aop.support.RegexpMethodPointcutAdvisor">  
      <property name="advice">  
          <ref local="debugInterceptor" />  
      </property>  
      <property name="patterns">  
          <list>  
              <value>.*someMethod</value>  
              <value>.*someInnerMethod</value>  
          </list>  
      </property>  
  </bean>  
  ```

  **用XmlBeanFactory进行测试需要注意，所有的BeanPostProcessor并不会自动生效，需要执行以下代码：** 

  ```java
  XmlBeanFactory factory = new XmlBeanFactory(...);  
  InjectBeanSelfProcessor postProcessor = new InjectBeanSelfProcessor();  
  factory.addBeanPostProcessor(postProcessor);  
  ```