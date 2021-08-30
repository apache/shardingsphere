+++
title = "搭建项目及环境指南"
weight = 2
chapter = true
+++
## 软件环境
| **软件** | **JDK** | **Idea**     |
| :------- | :------ | :----------- |
| **版本** | >=1.8   | 最新版本即可 |
## 安装步骤（ Mac 为例）
## 1.JDK 安装
- 下方链接获取适合自己环境的安装包（ mac 选取 .dmg 格式）
-	https://www.oracle.com/java
	![JDK.png](https://shardingsphere.apache.org/community/image/download_source/JDK.png)
- 下载完成后直接安装即可
## 2.设置环境变量
```shell
vim ~/.zprofile
```
- 在下方添加环境变量：
```shell
#jdk环境通过 .dmg 方式安装后默认为下方路径
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_301.jdk/Contents/Home
PATH=$JAVA_HOME/bin:$PATH:.
CLASSPATH=$JAVA_HOME/lib/tools.jar:$JAVA_HOME/lib/dt.jar:.
export JAVA_HOME
export PATH
export CLASSPATH
```
```shell
source ~/.zprofile
```
## 3.Idea 下载安装
- 下方链接获取适合自己环境的安装包
- https://www.jetbrains.com/idea/download/#section=mac
- 下载后直接安装
## 4.Idea clone 代码
- 进入 Idea
- 工具栏-->Git-->Clone-->Url(https://github.com/apache/shardingsphere.git)
	![Idea.png](https://shardingsphere.apache.org/community/image/download_source/Idea.png)
- 等待结束就有刚刚克隆的最新的代码了
## 5.项目编译
```shell
#假设项目路径为 /Users/hanmeimei/IdeaProjects/shardingsphere/
cd /Users/hanmeimei/IdeaProjects/shardingsphere/
```
## 编译环境
```shell
./mvnw -Dmaven.javadoc.skip=true -Djacoco.skip=true -DskipITs -DskipTests clean install
```
## 获取最新的项目
```shell
./mvnw -Dmaven.javadoc.skip=true -Djacoco.skip=true -DskipITs -DskipTests clean install  -Prelease
#进入下方目录位置
cd /Users/hanmeimei/shardingsphere/shardingsphere/shardingsphere-distribution/shardingsphere-proxy-distribution/target
#即可看到打包好最新的软件
apache-shardingsphere-5.0.0-RC1-SNAPSHOT-shardingsphere-proxy-bin.tar.gz
```
## 结语
完成以上操作后，您已经有了 ShardingSphere 的软件环境，可以根据[《贡献者指南》](https://shardingsphere.apache.org/community/cn/contribute/contributor/) 参与到 ShardingSphere 的社区建设
