#### 什么是DAF

DAF的中文名称是捐助者建议基金,

#### 技术列表

eclipse,spring,springmvc,mybatis,tomcat,swagger,xxl-job

#### 主要功能

对基金会接口,包括

- 保存捐赠者信息(add/update)
- 新增捐助指令
- 新增资助指令
- 查询资产信息
- 定时任务
  + 获取基金会token并缓存 使用ConcurrentHashMap,在获取到后对比生成时间与当前时间,判断是否生效
  + 推送项目信息(信托项目)
  + 计算每日收益,推送资产信息

后台管理页面

	#### 亮点技术?

springEvent 监听者模式 --> 引申至MQ相关 MessageStore接口,面向接口编程,message存储位置实现,如Redis,Mysql,DB

interceptor统一权限拦截器 --> Validator接口,由自己实现,面向接口编程

XXL-JOB统一调度平台 --> 源码解析

#### 思考

