### RPC(远程过程调用)

---

1. 怎么调用远程服务

   ![](https://static001.geekbang.org/resource/image/94/ea/946841b09cab0b11ce349a5a1eeea0ea.jpg)

   - 客户端(以dubbo为例)

     ```java
     @Component
     public class HelloClient {
     
         @Reference // dubbo 注解
         private HelloService helloService;
     
         public String hello() {
           return helloService.hello("World");
         }
     }
     ```

     > 桩(Stub):由RPC框架提供的一个代理类的实例
     >
     > 主要作用是构建一个请求,请求中包含两个重要信息:
     >
     > 1. 请求的服务名
     > 2. 请求的所有参数
     >
     > 

   - 服务端(以dubbo为例)

     ```java
     @Service // dubbo 注解
     @Component
     public class HelloServiceImpl implements HelloService {
     
         @Override
         public String hello(String name) {
             return "Hello " + name;
         }
     }
     ```

     > RPC框架收到请求后,可以通过请求中的服务名找到真正的实现类,调用这个实例的方法,使用的参数值就是客户端发送过来的参数值.服务端的RPC框架在获得返回的结果后,在将结果封装成响应,返回给客户端

2. 需要哪些技术

   1. 高性能网络传输

      > 以netty为代表

   

   2. 序列化与反序列化

      > 以hessian为例

   3. 服务路由的发现

      > 注册中心以zookeeper为例

3. 代码结构

   - RpcAccessPoint接口

     ```java
     /**
      * RPC 框架对外提供的服务接口
      */
     public interface RpcAccessPoint extends Closeable{
         /**
          * 客户端获取远程服务的引用
          * @param uri 远程服务地址
          * @param serviceClass 服务的接口类的 Class
          * @param <T> 服务接口的类型
          * @return 远程服务引用
          */
         <T> T getRemoteService(URI uri, Class<T> serviceClass);
     
         /**
          * 服务端注册服务的实现实例
          * @param service 实现实例
          * @param serviceClass 服务的接口类的 Class
          * @param <T> 服务接口的类型
          * @return 服务地址
          */
         <T> URI addServiceProvider(T service, Class<T> serviceClass);
     
         /**
          * 服务端启动 RPC 框架，监听接口，开始提供远程服务。
          * 方法 startServer 和 close（在父接口 Closeable中定义)
          * @return 服务实例，用于程序停止的时候安全关闭服务。
          */
         Closeable startServer() throws Exception;
     }
     ```

     - NameService接口

     ```java
     /**
      * 注册中心
      */
     public interface NameService {
         /**
          * 注册服务
          * @param serviceName 服务名称
          * @param uri 服务地址
          */
         void registerService(String serviceName, URI uri) throws IOException;
     
         /**
          * 查询服务地址
          * @param serviceName 服务名称
          * @return 服务地址
          */
         URI lookupService(String serviceName) throws IOException;
     }
     ```

     -  服务接口,以HelloService为例

     ```java
     public interface HelloService {
         String hello(String name);
     }
     ```

4. 