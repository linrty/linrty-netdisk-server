# 资源共享平台
这是一个基于SpringCloud框架的项目
### 技术栈
- 微服务注册中心：Nacos 
- 微服务网关：Spring Cloud Gateway  版本 
- 微服务调用：OpenFeign 
- 微服务熔断：Sentinel 
- 微服务配置中心：Nacos 
- 分布式事务：Seata  
- 内容搜索：Elasticsearch  版本：8.1.2
- 消息队列：RabbitMQ 
- 数据库：MySQL 版本：8.0.23
- 缓存：Redis 

SpringCloudAlibaba 版本：2021.0.4.0
SpringBoot 版本：2.7.12

### 项目结构

- netdisk-service-common (通用工具以及配置模块)
- netdisk-service-config (应用内配置模块)
- netdisk-service-gateway (网关模块)
- netdisk-service-user (用户模块)
- netdisk-service-file (文件模块，文件的一些基础操作比如搜索、分享、重命名、复制、移动等)
- netdisk-service-transfer (文件传输模块，文件的上传、下载、断点续传等)
- netdisk-service-notice (系统公告模块)
