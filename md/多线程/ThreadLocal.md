### ThreadLocal

---

##### 从一个例子引入

SimpleDateFormat对象是线程不安全的,在多线程环境下,为了保证每个线程使用自己的对象,可以通过以下代码实现:

```java
private ThreadLocal<SimpleDateFormat> sdf;

public void method(){
    SimpleDateFormat sdf = localSdf.get();
    if(sdf == null){
    	//初始化
        sdf = new SimpleDateFormat()...
        localSdf.set(sdf);
    }
    ...
}
```

---

##### 作用

顾名思义,ThreadLocal中文翻译就是线程本地对象

当使用ThreadLocal维护变量时，ThreadLocal为每个使用该变量的线程提供独立的变量副本，所以每一个线程都可以独立地改变自己的副本，而不会影响其它线程所对应的副本。从线程的角度看，目标变量就象是线程的本地变量，这也是类名中“Local”所要表达的意思。

如何实现以上功能呢?

首先,ThreadLocal有一个静态内部类ThreadLocalMap

从ThreadLocalMap的Entry类的构造方法可以看到,map的key就是ThreadLocal对象本身

```java
static class Entry extends WeakReference<ThreadLocal<?>> {
            /** The value associated with this ThreadLocal. */
            Object value;
			/**
			注意,Entry类继承自WeakReference<ThreadLocal<?>>
			再看它的构造方法,将ThreadLocal对象传入父类的构造方法
			所以Entry中的key是虚引用,会在下次GC时被清除
			但只有Key是弱引用类型的，Value并非弱引用
			*/
            Entry(ThreadLocal<?> k, Object v) {
                super(k);
                value = v;
            }
        }
```

ThreadLocal本身并不持有ThreadLocalMap对象,那么,ThreadLocalMap由谁持有呢?

从ThreadLocal的get()方法可知ThreadLocalMap由线程对象Thread持有

```java
/**
1.获取当前线程的ThreadLocalMap对象threadLocals
2.从map中获取线程存储的K-V Entry节点。
3.map不为空的话,从Entry节点获取存储的Value副本值返回。
4.map为空的话返回初始值null，即线程变量副本为null，在使用时需要注意判断NullPointerException。
*/
public T get() {
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null) {
            ThreadLocalMap.Entry e = map.getEntry(this);
            if (e != null) {
                @SuppressWarnings("unchecked")
                T result = (T)e.value;
                return result;
            }
        }
        return setInitialValue();
}

ThreadLocalMap getMap(Thread t) {
        return t.threadLocals;
}
```

在Thread类中的成员变量

```java
public class Thread implements Runnable {
	ThreadLocal.ThreadLocalMap threadLocals = null;
	...
}
```

