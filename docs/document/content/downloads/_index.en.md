+++
pre = "<b>9. </b>"
title = "Downloads"
weight = 9
chapter = true
extracss = true
+++

## Latest Releases

Apache ShardingSphere is released as source code tarballs with corresponding binary tarballs for convenience.
The downloads are distributed via mirror sites and should be checked for tampering using GPG or SHA-512.

##### Apache ShardingSphere - Version: 5.5.2 ( Release Date: January 16th, 2025 )

- Source Codes: [<u>SRC</u>](https://www.apache.org/dyn/closer.lua/shardingsphere/5.5.2/apache-shardingsphere-5.5.2-src.zip) ( [<u>ASC</u>](https://downloads.apache.org/shardingsphere/5.5.2/apache-shardingsphere-5.5.2-src.zip.asc), [<u>SHA512</u>](https://downloads.apache.org/shardingsphere/5.5.2/apache-shardingsphere-5.5.2-src.zip.sha512) )
- ShardingSphere-JDBC Binary Distribution: [<u>TAR</u>](https://www.apache.org/dyn/closer.lua/shardingsphere/5.5.2/apache-shardingsphere-5.5.2-shardingsphere-jdbc-bin.tar.gz) ( [<u>ASC</u>](https://downloads.apache.org/shardingsphere/5.5.2/apache-shardingsphere-5.5.2-shardingsphere-jdbc-bin.tar.gz.asc), [<u>SHA512</u>](https://downloads.apache.org/shardingsphere/5.5.2/apache-shardingsphere-5.5.2-shardingsphere-jdbc-bin.tar.gz.sha512) )
- ShardingSphere-Proxy Binary Distribution: [<u>TAR</u>](https://www.apache.org/dyn/closer.lua/shardingsphere/5.5.2/apache-shardingsphere-5.5.2-shardingsphere-proxy-bin.tar.gz) ( [<u>ASC</u>](https://downloads.apache.org/shardingsphere/5.5.2/apache-shardingsphere-5.5.2-shardingsphere-proxy-bin.tar.gz.asc), [<u>SHA512</u>](https://downloads.apache.org/shardingsphere/5.5.2/apache-shardingsphere-5.5.2-shardingsphere-proxy-bin.tar.gz.sha512) )
- ShardingSphere-Agent Binary Distribution: [<u>TAR</u>](https://www.apache.org/dyn/closer.lua/shardingsphere/5.5.2/apache-shardingsphere-5.5.2-shardingsphere-agent-bin.tar.gz) ( [<u>ASC</u>](https://downloads.apache.org/shardingsphere/5.5.2/apache-shardingsphere-5.5.2-shardingsphere-agent-bin.tar.gz.asc), [<u>SHA512</u>](https://downloads.apache.org/shardingsphere/5.5.2/apache-shardingsphere-5.5.2-shardingsphere-agent-bin.tar.gz.sha512) )

## All Releases

Find all releases in the [Archive repository](https://archive.apache.org/dist/shardingsphere/).</br>
Find all incubator releases in the [Archive incubator repository](https://archive.apache.org/dist/incubator/shardingsphere/).

## Verify the Releases

[PGP signatures KEYS](https://downloads.apache.org/shardingsphere/KEYS)

It is essential that you verify the integrity of the downloaded files using the PGP or SHA signatures.
The PGP signatures can be verified using GPG or PGP.
Please download the KEYS as well as the asc signature files for relevant distribution.
It is recommended to get these files from the main distribution directory and not from the mirrors.

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
