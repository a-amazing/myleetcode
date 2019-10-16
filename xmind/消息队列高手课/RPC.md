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

   - 服务接口,以HelloService为例

     ```java
     public interface HelloService {
         String hello(String name);
     }
     ```

4. 代码实现

   - 序列化与反序列化

     ```java
     public interface Serializer<T> {
         /**
          * 计算对象序列化后的长度，主要用于申请存放序列化数据的字节数组
          * @param entry 待序列化的对象
          * @return 对象序列化后的长度
          */
         int size(T entry);
     
         /**
          * 序列化对象。将给定的对象序列化成字节数组
          * @param entry 待序列化的对象
          * @param bytes 存放序列化数据的字节数组
          * @param offset 数组的偏移量，从这个位置开始写入序列化数据
          * @param length 对象序列化后的长度，也就是{@link Serializer#size(java.lang.Object)}方法的返回值。
          */
         void serialize(T entry, byte[] bytes, int offset, int length);
     
         /**
          * 反序列化对象
          * @param bytes 存放序列化数据的字节数组
          * @param offset 数组的偏移量，从这个位置开始写入序列化数据
          * @param length 对象序列化后的长度
          * @return 反序列化之后生成的对象
          */
         T parse(byte[] bytes, int offset, int length);
     
         /**
          * 用一个字节标识对象类型，每种类型的数据应该具有不同的类型值
          */
         byte type();
     
         /**
          * 返回序列化对象类型的 Class 对象。
          */
         Class<T> getSerializeClass();
     }
     ```

     对应不同Class的序列化实现可以通过继承上面的接口实现,如对应String序列化的StringSerializer

     ```java
     public class StringSerializer implements Serializer<String> {
         @Override
         public int size(String entry) {
             return entry.getBytes(StandardCharsets.UTF_8).length;
         }
     
         @Override
         public void serialize(String entry, byte[] bytes, int offset, int length) {
             byte [] strBytes = entry.getBytes(StandardCharsets.UTF_8);
             System.arraycopy(strBytes, 0, bytes, offset, strBytes.length);
         }
     
         @Override
         public String parse(byte[] bytes, int offset, int length) {
             return new String(bytes, offset, length, StandardCharsets.UTF_8);
         }
     
         @Override
         public byte type() {
             return Types.TYPE_STRING;
         }
     
         @Override
         public Class<String> getSerializeClass() {
             return String.class;
         }
     }
     ```

   - 网络通信

     CompletableFuture

     可以直接通过get方法同步获取响应结果,也可以使用then开头的一系列异步方法进行异步调用 

     ```java
     public interface Transport {
         /**
          * 发送请求命令
          * @param request 请求命令
          * @return 返回值是一个 Future，Future
          */
         CompletableFuture<Command> send(Command request);
     }
     ```

     请求和响应数据都抽象成了一个 Command 类

     ```java
     public class Command {
         protected Header header;
         //要传输的数据,已经通过序列化为byte[]
         private byte [] payload;
         //...
     }
     
     public class Header {
         //唯一标识一次请求
         private int requestId;
         //传输协议对应的版本,由服务端确认是否支持
         private int version;
         //序列化对应Serializer
         private int type;
         // ...
     }
     public class ResponseHeader extends Header {
         //响应码
         private int code;
         //错误信息
         private String error;
         // ...
     }
     ```

     Netty实现的Transport类

     ```java
     @Override
     public  CompletableFuture<Command> send(Command request) {
         // 构建返回值
         CompletableFuture<Command> completableFuture = new CompletableFuture<>();
         try {
             // 将在途请求放到 inFlightRequests 中
             inFlightRequests.put(new ResponseFuture(request.getHeader().getRequestId(), completableFuture));
             // 发送命令
             channel.writeAndFlush(request).addListener((ChannelFutureListener) channelFuture -> {
                 // 处理发送失败的情况
                 if (!channelFuture.isSuccess()) {
                     completableFuture.completeExceptionally(channelFuture.cause());
                     channel.close();
                 }
             });
         } catch (Throwable t) {
             // 处理发送异常
             inFlightRequests.remove(request.getHeader().getRequestId());
             completableFuture.completeExceptionally(t);
         }
         return completableFuture;
     }
     ```

     

