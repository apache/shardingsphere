+++
title = "Downloads"
weight = 1
chapter = true
+++

## Releases

ShardingSphere is released as source code tarballs with corresponding binary tarballs for convenience. The downloads are distributed via mirror sites and should be checked for tampering using GPG or SHA-512.

| Version   | Release date | Source download             | Sharding-JDBC binary download | Sharding-Proxy binary download |
| --------- | ------------ | --------------------------- | ----------------------------- | ------------------------------ |
| 4.0.0-RC1 | 2019 Apr 21  | [source](https://www.apache.org/dyn/closer.cgi?path=incubator/shardingsphere/4.0.0-RC1/apache-shardingsphere-incubating-4.0.0-RC1-src.zip) ([asc](https://www.apache.org/dist/incubator/shardingsphere/4.0.0-RC1/apache-shardingsphere-incubating-4.0.0-RC1-src.zip.asc) [sha512](https://www.apache.org/dist/incubator/shardingsphere/4.0.0-RC1/apache-shardingsphere-incubating-4.0.0-RC1-src.zip.sha512)) | [binary](https://www.apache.org/dyn/closer.cgi?path=incubator/shardingsphere/4.0.0-RC1/apache-shardingsphere-incubating-4.0.0-RC1-sharding-jdbc-bin.tar.gz) ([asc](https://www.apache.org/dist/incubator/shardingsphere/4.0.0-RC1/apache-shardingsphere-incubating-4.0.0-RC1-sharding-jdbc-bin.tar.gz.asc) [sha512](https://www.apache.org/dist/incubator/shardingsphere/4.0.0-RC1/apache-shardingsphere-incubating-4.0.0-RC1-sharding-jdbc-bin.tar.gz.sha512))   | [binary](https://www.apache.org/dyn/closer.cgi?path=incubator/shardingsphere/4.0.0-RC1/apache-shardingsphere-incubating-4.0.0-RC1-sharding-proxy-bin.tar.gz) ([asc](https://www.apache.org/dist/incubator/shardingsphere/4.0.0-RC1/apache-shardingsphere-incubating-4.0.0-RC1-sharding-proxy-bin.tar.gz.asc) [sha512](https://www.apache.org/dist/incubator/shardingsphere/4.0.0-RC1/apache-shardingsphere-incubating-4.0.0-RC1-sharding-proxy-bin.tar.gz.sha512))    |

## Verify the releases

[PGP signatures KEYS](https://www.apache.org/dist/incubator/shardingsphere/KEYS)

It is essential that you verify the integrity of the downloaded files using the PGP or SHA signatures. The PGP signatures can be verified using GPG or PGP. Please download the KEYS as well as the asc signature files for relevant distribution. It is recommended to get these files from the main distribution directory and not from the mirrors.

```
gpg -i KEYS

or

pgpk -a KEYS

or

pgp -ka KEYS
```

To verify the binaries/sources you can download the relevant asc files for it from main distribution directory and follow the below guide.

```
gpg --verify  apache-shardingsphere-incubating********.asc  apache-shardingsphere-incubating*********

or

pgpv  apache-shardingsphere-incubating********.asc

or

pgp  apache-shardingsphere-incubating********.asc
```

## Disclaimer

Apache ShardingSphere (incubating) is an effort undergoing incubation at The Apache Software Foundation (ASF), sponsored by the Apache Incubator PMC.
Incubation is required of all newly accepted projects until a further review indicates that the infrastructure, 
communications, and decision making process have stabilized in a manner consistent with other successful ASF projects. 
While incubation status is not necessarily a reflection of the completeness or stability of the code, 
it does indicate that the project has yet to be fully endorsed by the ASF.
