# DEFAULT_GROUP

## auth.yaml

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3307/smart_todo?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver

secure:
  jwt:
    # （密钥与gateway模块相同）
    secret-key: "4JfTqMBa8cOyFfjCDzOGvQ13WC4v+Z2rmSVQlj1kAiM="
    expiration: 86400
```

## gateway.yaml

```yaml
secure:
  jwt:
    # 请确保密钥长度至少为 32 位以满足 HS256 算法要求（密钥与auth模块相同）
    secret-key: "4JfTqMBa8cOyFfjCDzOGvQ13WC4v+Z2rmSVQlj1kAiM="
  ignore-urls:
    - /api/auth/login
    - /api/auth/register
```

## task.yaml

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3307/smart_todo?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
```