# Wandering Earth Plan

## plugin-framework

基于pf4j-3.11.0开发, 支持spring-boot-2.7.9

## usage:

### 使spring-boot支持插件

in your spring-boot pom

```xml

<dependencyManagement>
    <dependencies>
        <groupId>org.wep</groupId>
        <artifactId>bom</artifactId>
        <version>1.0</version>
    </dependencies>
</dependencyManagement>

<dependencies>
<dependency>
    <groupId>org.wep</groupId>
    <artifactId>plugin-framework-spring-boot-starter</artifactId>
</dependency>
</dependencies>

```

in your application.yml:

```yaml
plugin:
  # 插件目录
  plugin-path: xxx
  # 插件配置文件目录
  plugin-config-file-path: xxx
  # 插件rest 路径默认以插件id开始
  enable-plugin-id-rest-path-prefix: default true
```

### 插件开发

使用archetype创建插件

```shell
mvn archetype:generate \
  -DarchetypeGroupId=org.wep 
  -DarchetypeVersion=1.0 
  -DarchetypeArtifactId=plugin-archetype
```

插件配置

```java
@org.wep.Config
public class Config {
    private String host;
    //getter and setter
}
```

```yaml
host: xx
```
