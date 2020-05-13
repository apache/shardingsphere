+++
pre = "<b>5. </b>"
title = "Downloads"
weight = 5
chapter = true
+++

## Latest releases

ShardingSphere is released as source code tarballs with corresponding binary tarballs for convenience. The downloads are distributed via mirror sites and should be checked for tampering using GPG or SHA-512.

**ShardingSphere**

| Version | Release date | Description | Downloads |
| - | - | - | - |
| 4.1.0     | Apr 30, 2020 | Source codes | [[src]](https://www.apache.org/dyn/closer.cgi/shardingsphere/4.1.0/apache-shardingsphere-4.1.0-src.zip) [[asc]](https://downloads.apache.org/shardingsphere/4.1.0/apache-shardingsphere-4.1.0-src.zip.asc) [[sha512]](https://downloads.apache.org/shardingsphere/4.1.0/apache-shardingsphere-4.1.0-src.zip.sha512) |
|           |              | Sharding-JDBC Binary Distribution | [[tar]](https://www.apache.org/dyn/closer.cgi/shardingsphere/4.1.0/apache-shardingsphere-4.1.0-sharding-jdbc-bin.tar.gz) [[asc]](https://downloads.apache.org/shardingsphere/4.1.0/apache-shardingsphere-4.1.0-sharding-jdbc-bin.tar.gz.asc) [[sha512]](https://downloads.apache.org/shardingsphere/4.1.0/apache-shardingsphere-4.1.0-sharding-jdbc-bin.tar.gz.sha512) |
|           |              | Sharding-Proxy Binary Distribution | [[tar]](https://www.apache.org/dyn/closer.cgi/shardingsphere/4.1.0/apache-shardingsphere-4.1.0-sharding-proxy-bin.tar.gz) [[asc]](https://downloads.apache.org/shardingsphere/4.1.0/apache-shardingsphere-4.1.0-sharding-proxy-bin.tar.gz.asc) [[sha512]](https://downloads.apache.org/shardingsphere/4.1.0/apache-shardingsphere-4.1.0-sharding-proxy-bin.tar.gz.sha512) |
|           |              | Sharding-Scaling Binary Distribution | [[tar]](https://www.apache.org/dyn/closer.cgi/shardingsphere/4.1.0/apache-shardingsphere-4.1.0-sharding-scaling-bin.tar.gz) [[asc]](https://downloads.apache.org/shardingsphere/4.1.0/apache-shardingsphere-4.1.0-sharding-scaling-bin.tar.gz.asc) [[sha512]](https://downloads.apache.org/shardingsphere/4.1.0/apache-shardingsphere-4.1.0-sharding-scaling-bin.tar.gz.sha512) |

**ShardingSphere UI**

| Version | Release date | Description | Downloads |
| - | - | - | - |
| 4.1.0     | Apr 30, 2020 | Source codes | [[src]](https://www.apache.org/dyn/closer.cgi/shardingsphere/shardingsphere-ui-4.1.0/apache-shardingsphere-4.1.0-shardingsphere-ui-src.zip ) [[asc]](https://downloads.apache.org/shardingsphere/shardingsphere-ui-4.1.0/apache-shardingsphere-4.1.0-shardingsphere-ui-src.zip.asc) [[sha512]](https://downloads.apache.org/shardingsphere/shardingsphere-ui-4.1.0/apache-shardingsphere-4.1.0-shardingsphere-ui-src.zip.sha512) |
|           |              | ShardingSphere-UI Binary Distribution | [[tar]](https://www.apache.org/dyn/closer.cgi/shardingsphere/shardingsphere-ui-4.1.0/apache-shardingsphere-4.1.0-shardingsphere-ui-bin.tar.gz) [[asc]](https://downloads.apache.org/shardingsphere/shardingsphere-ui-4.1.0/apache-shardingsphere-4.1.0-shardingsphere-ui-bin.tar.gz.asc) [[sha512]](https://downloads.apache.org/shardingsphere/shardingsphere-ui-4.1.0/apache-shardingsphere-4.1.0-shardingsphere-ui-bin.tar.gz.sha512) |

## All releases

Find all releases in the [Archive repository](https://archive.apache.org/dist/shardingsphere/).

Find all incubator releases in the [Archive incubator repository](https://archive.apache.org/dist/incubator/shardingsphere/).

## Verify the releases

[PGP signatures KEYS](https://downloads.apache.org/shardingsphere/KEYS)

It is essential that you verify the integrity of the downloaded files using the PGP or SHA signatures. The PGP signatures can be verified using GPG or PGP. Please download the KEYS as well as the asc signature files for relevant distribution. It is recommended to get these files from the main distribution directory and not from the mirrors.

```shell
gpg -i KEYS
```

or

```shell
pgpk -a KEYS
```

or

```shell
pgp -ka KEYS
```

To verify the binaries/sources you can download the relevant asc files for it from main distribution directory and follow the below guide.

```shell
gpg --verify apache-shardingsphere-********.asc apache-shardingsphere-*********
```

or

```shell
pgpv apache-shardingsphere-********.asc
```

or

```shell
pgp apache-shardingsphere-********.asc
```
