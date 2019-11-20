在Servlet中调用其他类的静态方法时,报IllegalAccessException

---

异常原因:

```
java.lang.IllegalAccessException

安全权限异常，一般来说，是由于java在反射时调用了private方法所导致的。
```

反编译检查代码后发现,调用一个被改成public的private方法未更新部署,服务器中的代码还是private版本

---

异常思考:

遇到IllegalAccessException,第一时间考虑是否为访问级别的问题

引申到反射过程中,可以通过setAccessible(true)方法无视访问级别的问题,直接暴力反射!