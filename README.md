# deploywar-maven-plugin


## 插件配置
```xml
<plugin>
	<groupId>cn.tony</groupId>
	<artifactId>deploywar-maven-plugin</artifactId>
	<version>0.0.2</version>
	<configuration>
		<server-host>${server-host}</server-host>
		<password>${password}</password>
		<tomcatHome>${tomcatHome}</tomcatHome>
		<finalName>${project_war_name}</finalName>
	</configuration>
</plugin>
```

## mvn 命令
```shell
mvn cn.tony:deploywar-maven-plugin:deploy 
```
