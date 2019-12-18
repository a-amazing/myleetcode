### @RequestBody注解浅谈

---

1. 使用注解

```java
@PostMapping("/product")
public ResponseEntity addProduct(@RequestBody ProductDo product){}
```

---

2. 