#### 三元表达式
```java
boolean condition = false;
Double value1 = 1.0D;
Double value2 = 2.0D;
Double value3 = null;
Double result = condition ? value1 * value2 : value3; // 抛出空指针异常
```

为什么这边会因为赋null值抛出空指针异常?

三元表达式的类型转化规则：

- 若两个表达式类型相同，返回值类型为该类型；
- 若两个表达式类型不同，但类型不可转换，返回值类型为 Object 类型；
- 若两个表达式类型不同，但类型可以转化，先把包装数据类型转化为基本数据类型，然后按照基本数据类型的转换规则 （byte < short(char)< int < long < float < double） 来转化，返回值类型为优先级最高的基本数据类型。

根据规则分析，表达式 1（value1 * value2）的类型为基础数据类型 double，表达式 2（value 3）的类型为包装数据类型 Double，根据三元表达式的类型转化规则判断，最终的表达式类型为基础数据类型 double。所以，当条件表达式 condition 为 false 时，需要把空 Double 对象 value 3 转化为基础数据类型 double，于是就调用了 value 3 的 doubleValue 方法进行拆包，当然会抛出空指针异常。

避坑指南:

1. **尽量避免使用三元表达式，可以采用 if-else 语句代替**

   如果三元表达式中有包装数据类型的算术计算，可以考虑利用 if-else 语句代替

   ```java
   if (condition) {
     result = value1 * value2;
   } else {
     result = value3;
   }
   ```

2. **尽量使用基本数据类型，避免包装数据类型的拆装包**

   如果在三元表达式中有算术计算，尽量使用基本数据类型，避免包装数据类型的拆装包

   ```java
   boolean condition = false;
   double value1 = 1.0D;
   double value2 = 2.0D;
   double value3 = 3.0D;
   double result = condition ? value1 * value2 : value3;
   ```

---

#### **泛型对象赋值**