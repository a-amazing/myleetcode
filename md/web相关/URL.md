### [URI][1]

URI（Uniform Resource Identifier），即统一资源标识符,作用是标记服务端的资源

URI不完全等同于网址，它包含URL（Uniform Resource Locator，统一资源定位符）和URN（Uniform Resource Name， 统一资源命名）。

> 狭义上,狭义上，我们可以简单地把URI和URL看做是相同的东西

URI的格式如下:

```uri
URI = scheme:[//authority]path[?query][#fragment]
authority = [userinfo@]host[:port]
```

URI的结构如下:

![](./pics/uri结构.png)

1. scheme，可以翻译成协议名，表示资源应该使用哪种协议来访问。最常见的就是http和https了，其它的如：ftp、file等。

2. 在 scheme 之后，必须是三个特定的字符“://”，它把 scheme 和后面的部分分离开

3. 在“://”之后，是被称为“authority”的部分，表示资源所在的主机名，通常的形式是“host:port”，即主机名加端口号。以前authority还会包含身份信息userinfo，即“user:passwd@”的形式，不过现在已经不流行了，可以忽略。(主要是安全问题吧)



参考资料:

[1](https://mp.weixin.qq.com/s/XI4soBp656YOy268EwRnRw) 面试官给我挖坑：URI中的 “//” 有什么用？