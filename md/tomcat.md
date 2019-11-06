### tomcat启动优化

---

~~未做任何优化情况下,启动时间为12139ms~~

---

只删除了无用的项目情况下,启动时间为12139ms

---

在不使用jsp的情况下,找到 Tomcat 的conf/目录下的context.xml文件，在这个文件里 Context 标签下，加上 JarScanner 和 JarScanFilter 子标签

```xml
<Context>
	<JarScanner>
    	<JarScannerFilter defaultTldScan="false" />
    </JarScanner>
</Context>
```

启动时间未12038ms

---

关闭webSocket支持

如果不需要使用 WebSockets 就可以关闭它。具体方法是，找到 Tomcat 的conf/目录下的context.xml文件，给 Context 标签加一个 containerSciFilter 的属性

```xml
<Context containerSciFilter="org.apache.tomcat.websocket.server.WsSci">
...
</Context>
```

同理,关闭JSP支持

```xml
<Context containerSciFilter="org.apache.tomcat.websocket.server.WsSci |
          org.apache.jasper.servlet.JasperInitializer">
...
</Context>
```

关闭两个功能后的启动时间为11961ms

---

禁止servlet注解扫描

具体配置方法是，在你的 Web 应用的web.xml文件中，设置<web-app>元素的属性metadata-complete="true"

```xml
<web-app metadata-complete="true">
...
</web-app>
```

启动时间11955ms

---

### 结论

综上,没啥用处...