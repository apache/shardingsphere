+++
pre = "<b>5. </b>"
title = "Downloads"
weight = 5
chapter = true
+++

## Latest releases

ShardingSphere is released as source code tarballs with corresponding binary tarballs for convenience. The downloads are distributed via mirror sites and should be checked for tampering using GPG or SHA-512.

| Version   | Release date | Source download             | Sharding-JDBC download | Sharding-Proxy download | Sharding-UI download | Sharding-Scaling download |
| --------- | ------------ | --------------------------- | ----------------------------- | ------------------------------ | ------------------------------ | ------------------------------ |
| 4.0.1     | 2020 Mar 9   | [source](https://www.apache.org/dyn/closer.cgi?path=incubator/shardingsphere/4.0.1/apache-shardingsphere-incubating-4.0.1-src.zip) ([asc](https://downloads.apache.org/incubator/shardingsphere/4.0.1/apache-shardingsphere-incubating-4.0.1-src.zip.asc) [sha512](https://downloads.apache.org/incubator/shardingsphere/4.0.1/apache-shardingsphere-incubating-4.0.1-src.zip.sha512))                         | [binary](https://www.apache.org/dyn/closer.cgi?path=incubator/shardingsphere/4.0.1/apache-shardingsphere-incubating-4.0.1-sharding-jdbc-bin.tar.gz) ([asc](https://downloads.apache.org/incubator/shardingsphere/4.0.1/apache-shardingsphere-incubating-4.0.1-sharding-jdbc-bin.tar.gz.asc) [sha512](https://downloads.apache.org/incubator/shardingsphere/4.0.1/apache-shardingsphere-incubating-4.0.1-sharding-jdbc-bin.tar.gz.sha512))                           | [binary](https://www.apache.org/dyn/closer.cgi?path=incubator/shardingsphere/4.0.1/apache-shardingsphere-incubating-4.0.1-sharding-proxy-bin.tar.gz) ([asc](https://downloads.apache.org/incubator/shardingsphere/4.0.1/apache-shardingsphere-incubating-4.0.1-sharding-proxy-bin.tar.gz.asc) [sha512](https://downloads.apache.org/incubator/shardingsphere/4.0.1/apache-shardingsphere-incubating-4.0.1-sharding-proxy-bin.tar.gz.sha512))                            | [binary](https://www.apache.org/dyn/closer.cgi?path=incubator/shardingsphere/4.0.1/apache-shardingsphere-incubating-4.0.1-sharding-ui-bin.tar.gz) ([asc](https://downloads.apache.org/incubator/shardingsphere/4.0.1/apache-shardingsphere-incubating-4.0.1-sharding-ui-bin.tar.gz.asc) [sha512](https://downloads.apache.org/incubator/shardingsphere/4.0.1/apache-shardingsphere-incubating-4.0.1-sharding-ui-bin.tar.gz.sha512))                         |
| 4.1.0     | 2020 Apr 30  | [source](https://www.apache.org/dyn/closer.cgi?path=shardingsphere/4.1.0/apache-shardingsphere-4.1.0-src.zip) ([asc](https://downloads.apache.org/shardingsphere/4.1.0/apache-shardingsphere-4.1.0-src.zip.asc) [sha512](https://downloads.apache.org/shardingsphere/4.1.0/apache-shardingsphere-4.1.0-src.zip.sha512))                         | [binary](https://www.apache.org/dyn/closer.cgi?path=shardingsphere/4.1.0/apache-shardingsphere-4.1.0-sharding-jdbc-bin.tar.gz) ([asc](https://downloads.apache.org/shardingsphere/4.1.0/apache-shardingsphere-4.1.0-sharding-jdbc-bin.tar.gz.asc) [sha512](https://downloads.apache.org/shardingsphere/4.1.0/apache-shardingsphere-4.1.0-sharding-jdbc-bin.tar.gz.sha512))                           | [binary](https://www.apache.org/dyn/closer.cgi?path=shardingsphere/4.1.0/apache-shardingsphere-4.1.0-sharding-proxy-bin.tar.gz) ([asc](https://downloads.apache.org/shardingsphere/4.1.0/apache-shardingsphere-4.1.0-sharding-proxy-bin.tar.gz.asc) [sha512](https://downloads.apache.org/shardingsphere/4.1.0/apache-shardingsphere-4.1.0-sharding-proxy-bin.tar.gz.sha512))                            |                         | [binary](https://www.apache.org/dyn/closer.cgi?path=shardingsphere/4.1.0/apache-shardingsphere-4.1.0-sharding-scaling-bin.tar.gz) ([asc](https://downloads.apache.org/shardingsphere/4.1.0/apache-shardingsphere-4.1.0-sharding-scaling-bin.tar.gz.asc) [sha512](https://downloads.apache.org/shardingsphere/4.1.0/apache-shardingsphere-4.1.0-sharding-scaling-bin.tar.gz.sha512))                       |                        |

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
