| 区别项             | innodb | myisam   |
| ------------------ | ------ | -------- |
| 事务               | 支持   | 不支持   |
| 行锁               | 支持   | 不支持   |
| count(*)(不带条件) | 遍历表 | 直接取值 |
|                    |        |          |