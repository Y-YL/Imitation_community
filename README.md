# Imitation_community
  ## 仿牛客社交网站
  - 类贴吧社区
  
 - 主要功能：
  
    - 使用 ThreadLocal 保存用户状态，通过拦截器拦截请求，根据自定义注解判断用户登录状态。
    - 使用 Ajax 异步发帖、发送私信、评论，通过字典树过滤敏感词。
    - 使用 Redis 实现点赞、关注功能，优化登录模块——存储登录凭证、缓存用户信息。
  
  - 使用技术：
    - redis
    - Kafka
    - MySql
    - SpringBoot
    - Elasticsearch
    - Caffeine
    
 ### 欢迎 Star 
 
