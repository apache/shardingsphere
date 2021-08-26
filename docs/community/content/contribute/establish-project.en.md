+++
title = "Establish project and environmental guidelines"
weight = 2
chapter = true
+++


# Establish the ShardingSphere software environment locally and obtain the latest installation package

## Software environment



| **Software** | **JDK** | **Idea** |
|  :--:  | :--:  |:--:  |
| **Version** |  >=1.8  |  The latest version  |

## Installation Procedure (For example, Mac)



## 1.The JDK installation


- The following link is to obtain the installation package suitable for your environment (MAC select.dmg format).
	https://www.oracle.com/java

![image20210818115720737](https://note.youdao.com/yws/public/resource/ae044dae27b19d4d2f191c76e6df0a10/xmlnote/WEBRESOURCE98df8ecd49565fed4be4dfe594d04521/17060)

- Install it directly after downloading



## 2.Set the environment variable


```shell
vim ~/.zprofile
```

- Add the environment variable below:

```shell
#The following path is the default for the JDK environment installed in.dmg mode

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



## 3.Idea Download and install


- The following link provides an installation package suitable for your environment

	https://www.jetbrains.com/idea/download/#section=mac
- Install it directly after downloading



## 4.Idea clone code


- Enter Idea
- Toolbar-->Git-->Clone-->Url(https://github.com/apache/shardingsphere.git)

	![image-20210818110310185](https://note.youdao.com/yws/public/resource/d2c90a133be6216fadf355a5693af603/xmlnote/WEBRESOURCE714e7e0a63d84257f20230dbcc92fbbe/17045)
- At the end of the wait, there is the latest code that has just been cloned



## 5.Compile the project


```shell
#Suppose the project path is /Users/hanmeimei/IdeaProjects/shardingsphere/

cd /Users/hanmeimei/IdeaProjects/shardingsphere/
```

### Compile environment


```shell
./mvnw -Dmaven.javadoc.skip=true -Djacoco.skip=true -DskipITs -DskipTests clean install
```

### Get the latest projects


```shell
./mvnw -Dmaven.javadoc.skip=true -Djacoco.skip=true -DskipITs -DskipTests clean install  -Prelease

#Go to the directory below
cd /Users/hanmeimei/shardingsphere/shardingsphere/shardingsphere-distribution/shardingsphere-proxy-distribution/target

#You can see the latest software packaged

apache-shardingsphere-5.0.0-RC1-SNAPSHOT-shardingsphere-proxy-bin.tar.gz
```



## Conclusion:

After completing the above operations, you have a ShardingSphere environment and can participate in the community building of ShardingSphere according to the [Contributors' Guide](https://shardingsphere.apache.org/community/en/contribute/contributor/)

