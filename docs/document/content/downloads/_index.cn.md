+++
pre = "<b>9. </b>"
title = "下载"
weight = 9
chapter = true
extracss = true
+++

## 最新版本

Apache ShardingSphere 的发布版包括源码包及其对应的二进制包。
由于下载内容分布在镜像服务器上，所以下载后应该进行 GPG 或 SHA-512 校验，以此来保证内容没有被篡改。

##### Apache ShardingSphere - 版本: 5.5.2 ( 发布日期: January 16th, 2025 )

- 源码: [<u>SRC</u>](https://www.apache.org/dyn/closer.lua/shardingsphere/5.5.2/apache-shardingsphere-5.5.2-src.zip) ( [<u>ASC</u>](https://downloads.apache.org/shardingsphere/5.5.2/apache-shardingsphere-5.5.2-src.zip.asc), [<u>SHA512</u>](https://downloads.apache.org/shardingsphere/5.5.2/apache-shardingsphere-5.5.2-src.zip.sha512) )
- ShardingSphere-JDBC 二进制包: [<u>TAR</u>](https://www.apache.org/dyn/closer.lua/shardingsphere/5.5.2/apache-shardingsphere-5.5.2-shardingsphere-jdbc-bin.tar.gz) ( [<u>ASC</u>](https://downloads.apache.org/shardingsphere/5.5.2/apache-shardingsphere-5.5.2-shardingsphere-jdbc-bin.tar.gz.asc), [<u>SHA512</u>](https://downloads.apache.org/shardingsphere/5.5.2/apache-shardingsphere-5.5.2-shardingsphere-jdbc-bin.tar.gz.sha512) )
- ShardingSphere-Proxy 二进制包: [<u>TAR</u>](https://www.apache.org/dyn/closer.lua/shardingsphere/5.5.2/apache-shardingsphere-5.5.2-shardingsphere-proxy-bin.tar.gz) ( [<u>ASC</u>](https://downloads.apache.org/shardingsphere/5.5.2/apache-shardingsphere-5.5.2-shardingsphere-proxy-bin.tar.gz.asc), [<u>SHA512</u>](https://downloads.apache.org/shardingsphere/5.5.2/apache-shardingsphere-5.5.2-shardingsphere-proxy-bin.tar.gz.sha512) )
- ShardingSphere-Agent 二进制包: [<u>TAR</u>](https://www.apache.org/dyn/closer.lua/shardingsphere/5.5.2/apache-shardingsphere-5.5.2-shardingsphere-agent-bin.tar.gz) ( [<u>ASC</u>](https://downloads.apache.org/shardingsphere/5.5.2/apache-shardingsphere-5.5.2-shardingsphere-agent-bin.tar.gz.asc), [<u>SHA512</u>](https://downloads.apache.org/shardingsphere/5.5.2/apache-shardingsphere-5.5.2-shardingsphere-agent-bin.tar.gz.sha512) )

## 全部版本

全部版本请到 [Archive repository](https://archive.apache.org/dist/shardingsphere/) 查看。</br>
全部孵化器版本请到 [Archive incubator repository](https://archive.apache.org/dist/incubator/shardingsphere/) 查看。

## 校验版本

[PGP签名文件](https://downloads.apache.org/shardingsphere/KEYS)

使用 PGP 或 SHA 签名验证下载文件的完整性至关重要。
可以使用 GPG 或 PGP 验证 PGP 签名。
请下载 KEYS 以及发布的 asc 签名文件。
建议从主发布目录而不是镜像中获取这些文件。

```shell
gpg -i KEYS
```

或者

```shell
pgpk -a KEYS
```

或者

```shell
pgp -ka KEYS
```

要验证二进制文件或源代码，您可以从主发布目录下载相关的 asc 文件，并按照以下指南进行操作。

```shell
gpg --verify apache-shardingsphere-********.asc apache-shardingsphere-*********
```

或者

```shell
pgpv apache-shardingsphere-********.asc
```

或者

```shell
pgp apache-shardingsphere-********.asc
```
