+++
title = "Release Guide"
weight = 4
chapter = true
+++

## GPG Settings

### Install GPG

Download installation package on [official GnuPG website](https://www.gnupg.org/download/index.html). 
The command of GnuPG 1.x version can differ a little from that of 2.x version. 
The following instructions take `GnuPG-2.1.23` version for example.
After the installation, execute the following command to check the version number.

```shell
gpg --version
```

### Create Key

After the installation, execute the following command to create key.

This command indicates `GnuPG-2.x` can be used:

```shell
gpg --full-gen-key
```

This command indicates `GnuPG-1.x` can be used:

```shell
gpg --gen-key
```

Finish the key creation according to instructions:

```shell
gpg (GnuPG) 2.0.12; Copyright (C) 2009 Free Software Foundation, Inc.
This is free software: you are free to change and redistribute it.
There is NO WARRANTY, to the extent permitted by law.

Please select what kind of key you want:
  (1) RSA and RSA (default)
  (2) DSA and Elgamal
  (3) DSA (sign only)
  (4) RSA (sign only)
Your selection? 1
RSA keys may be between 1024 and 4096 bits long.
What keysize do you want? (2048) 4096
Requested keysize is 4096 bits
Please specify how long the key should be valid.
        0 = key does not expire
     <n>  = key expires in n days
     <n>w = key expires in n weeks
     <n>m = key expires in n months
     <n>y = key expires in n years
Key is valid for? (0) 
Key does not expire at all
Is this correct? (y/N) y

GnuPG needs to construct a user ID to identify your key.

Real name: ${Input username}
Email address: ${Input email}
Comment: ${Input comment}
You selected this USER-ID:
   "${Inputed username} (${Inputed comment}) <${Inputed email}>"

Change (N)ame, (C)omment, (E)mail or (O)kay/(Q)uit? O
You need a Passphrase to protect your secret key. # Input passwords
```

### Check Generated Key

```shell
gpg --list-keys
```

Execution Result:

```shell
pub   4096R/700E6065 2019-03-20
uid                  ${Username} (${Comment}) <{Email}>
sub   4096R/0B7EF5B2 2019-03-20
```

Among them, 700E6065 is public key ID.

### Upload the Public Key to Key Server

The command is as follow:

```shell
gpg --keyserver hkp://pool.sks-keyservers.net --send-key 700E6065
```

`pool.sks-keyservers.net` is randomly chosen from [public key server](https://sks-keyservers.net/status/). 
Each server will automatically synchronize with one another, so it would be okay to choose any one.

## Apache Maven Central Repository Release

### Set settings.xml

Add the following template to `~/.m2/settings.xml`, all the passwords need to be filled in after encryption. 
For encryption settings, please see [here](http://maven.apache.org/guides/mini/guide-encryption.html).

```xml
<settings>
    <servers>
      <server>
          <id>apache.snapshots.https</id>
          <username> <!-- APACHE LDAP username --> </username>
          <password> <!-- APACHE LDAP encrypted password --> </password>
      </server>
      <server>
          <id>apache.releases.https</id>
          <username> <!-- APACHE LDAP username --> </username>
          <password> <!-- APACHE LDAP encrypted password --> </password>
      </server>
    </servers>
</settings>
```

### Inherit the Apache Parent POM

This parent POM sets up the defaults for your \<distributionManagement\> section to use the correct release and snapshot repositories. 
Be sure to remove \<distributionManagement\> section from your POM so they inherit correctly.

```xml
<parent>
    <groupId>org.apache</groupId>
    <artifactId>apache</artifactId>
    <version>21</version>
</parent>
```

### Create Release Branch

Suppose ShardingSphere source codes downloaded from github is under `~/incubator-shardingsphere/` directory and the version to be released is `4.0.0-RC`. 
Create `4.0.0-RC1-release` branch, where all the following operations are performed.

```shell
cd ~/incubator-shardingsphere/
git pull
git branch 4.0.0-RC1-release
git push origin 4.0.0-RC1-release
git checkout 4.0.0-RC1-release
```

### Pre-Release Check

```shell
mvn release:prepare -Prelease -Darguments="-DskipTests" -DautoVersionSubmodules=true -DdryRun=true -Dusername=${Github username}
```

-Prelease: choose release profile, which will pack all the source codes, jar files and executable binary packages of sharding-proxy.

-DautoVersionSubmodules=true: it can make the version number is inputted only once and not for each sub-module.

-DdryRun=true: rehearsal, which means not to generate or submit new version number and new tag.

### Prepare for the Release

First, clean local pre-release check information.

```shell
mvn release:clean
```

Then, prepare to execute the release.

```shell
mvn release:prepare -Prelease -Darguments="-DskipTests" -DautoVersionSubmodules=true -DpushChanges=false -Dusername=${Github username}
```

It is basically the same as the previous rehearsal command, but deleting -DdryRun=true parameter.

-DpushChanges=false: do not submit the edited version number and tag to Github automatically.

After making sure there is no mistake in local files, submit them to GitHub.

```shell
git push
git push origin --tags
```

### Deploy the Release

```shell
mvn release:perform -Prelease -Darguments="-DskipTests" -DautoVersionSubmodules=true -Dusername=${Github username}
```

After that command is executed, the version to be released will be uploaded to Apache staging repository automatically. 
Visit [https://repository.apache.org/#stagingRepositories](https://repository.apache.org/#stagingRepositories) and use Apache LDAP account to log in; then you can see the uploaded version. 
Click `Close` to tell Nexus that the construction is finished, because only in this way, this version can be usable. 
If there is any problem in gpg signature, `Close` will fail, but you can see the failure information through `Activity`.

## Apache SVN Repository Release

### Checkout ShardingSphere Release Directory

If there is no local work directory, create one at first.

```shell
mkdir -p ~/ss_svn/dev/
cd ~/ss_svn/dev/
```

After the creation, checkout ShardingSphere release directory from Apache SVN.

```shell
svn --username=${APACHE LDAP username} co https://dist.apache.org/repos/dist/dev/incubator/shardingsphere
cd ~/ss_svn/dev/shardingsphere
```

### Add gpg Public Key

Only the account in its first deployment needs to add that. 
It is alright for `KEYS` to only include the public key of the deployed account.

```shell
gpg -a --export ${GPG username} >> KEYS
```

### Add the Release Content to SVN Directory

Create folder by version number.

```shell
mkdir -p ~/ss_svn/dev/shardingsphere/4.0.0-RC1
cd ~/ss_svn/dev/shardingsphere/4.0.0-RC1
```

Add source code packages, binary packages and executable binary packages of sharding-proxy to SVN working directory.

```shell
cp ~/incubator-shardingsphere/shardingsphere-distribution/shardingsphere-basic-distribution/target/*.zip ~/ss_svn/dev/shardingsphere/4.0.0-RC1
cp ~/incubator-shardingsphere/shardingsphere-distribution/shardingsphere-basic-distribution/target/*.zip.asc ~/ss_svn/dev/shardingsphere/4.0.0-RC1
cp ~/incubator-shardingsphere/shardingsphere-distribution/shardingsphere-proxy-distribution/target/*.tar.gz ~/ss_svn/dev/shardingsphere/4.0.0-RC1
cp ~/incubator-shardingsphere/shardingsphere-distribution/shardingsphere-proxy-distribution/target/*.tar.gz.asc ~/ss_svn/dev/shardingsphere/4.0.0-RC1
```

### Generate sign files

```shell
shasum -a 512 apache-shardingsphere-incubating-4.0.0-RC1-src.zip > apache-shardingsphere-incubating-4.0.0-RC1-src.zip.sha512
shasum -b -a 512 apache-shardingsphere-incubating-4.0.0-RC1-bin.zip > apache-shardingsphere-incubating-4.0.0-RC1-bin.zip.sha512
shasum -b -a 512 apache-shardingsphere-incubating-4.0.0-RC1-sharding-proxy.tar.gz > apache-shardingsphere-incubating-4.0.0-RC1-sharding-proxy.tar.gz.sha512
```

### Commit to Apache SVN

```shell
svn add *
svn --username=${APACHE LDAP username} commit -m "release 4.0.0-RC1"
```

## Check Release

### Check sha512 hash

```shell
shasum -c apache-shardingsphere-incubating-4.0.0-RC1-src.zip.sha512
shasum -c apache-shardingsphere-incubating-4.0.0-RC1-bin.zip.sha512
shasum -c apache-shardingsphere-incubating-4.0.0-RC1-sharding-proxy.tar.gz.sha512
```

### Check gpg Signature

First, import releaser's public key. 
Import KEYS from SVN repository to local. (The releaser does not need to import again; the checking assistant needs to import it, with the user name filled as the releaser's. )

```shell
curl https://dist.apache.org/repos/dist/dev/incubator/shardingsphere/KEYS >> KEYS
gpg --import KEYS
gpg --edit-key "${GPG username of releaser}"
  > trust
  > save
```

Then, check the gpg signature.

```shell
gpg --verify apache-shardingsphere-incubating-4.0.0-RC1-src.zip.asc apache-shardingsphere-incubating-4.0.0-RC1-src.zip
gpg --verify apache-shardingsphere-incubating-4.0.0-RC1-bin.zip.asc apache-shardingsphere-incubating-4.0.0-RC1-bin.zip
gpg --verify apache-shardingsphere-incubating-4.0.0-RC1-sharding-proxy.tar.gz.asc apache-shardingsphere-incubating-4.0.0-RC1-sharding-proxy.tar.gz
```

### Check Released Files

#### Check source package

Decompress `apache-shardingsphere-incubating-4.0.0-RC1-src.zip` and check the following items:

*   The release files have the word `incubating` in their name
*   `DISCLAIMER` file exists
*   `LICENSE` and `NOTICE` files exist
*   There is only text files but no binary files
*   All source files have ASF headers
*   Codes can be compiled and pass the unit tests (mvn install)
*   The contents of the release match with what's tagged in version control (diff -r a verify_dir tag_dir)
*   Check if there is any extra files or folders, empty folders for example

#### Check binary packages

Decompress `apache-shardingsphere-incubating-4.0.0-RC1-bin.zip` and `apache-shardingsphere-incubating-4.0.0-RC1-sharding-proxy.tar.gz` to check the following items:

*   The release files have the word `incubating` in their name
*   `DISCLAIMER` file exists
*   `LICENSE` and `NOTICE` files exist
*   All text files have ASF headers
*   After the `sharding-proxy` binary package is rightly configured, it can run well (./start.sh)
*   Check the third party dependency license:
    *   The software have a compatible license
    *   All software licenses mentioned in `LICENSE`
    *   All the third party dependency licenses are under `licenses` folder
    *   If it depends on Apache license and has a `NOTICE` file, that `NOTICE` file need to be added to `NOTICE` file of the release

For the whole check list, please see [here](https://wiki.apache.org/incubator/IncubatorReleaseChecklist).

## Call for a Vote

### Vote procedure

1. ShardingSphere community vote: send the vote e-mail to `dev@shardingsphere.apache.org`. 
PPMC needs to check the rightness of the version according to the document before they vote. 
After at least 72 hours and with at least 3 `+1 binding` votes (only PPMC's votes are binding), it can come to the next stage of the vote.

2. Apache community vote: send the vote e-mail to `general@incubator.apache.org`.
After at least 72 hours and with at least 3 `+1 binding` votes (only IPMC's votes are binding), it can be officially released.

3. Announce the vote result: send the result vote e-mail to `general@incubator.apache.org`.

### Vote Templates

1. ShardingSphere Community Vote Template

Title:

```
[VOTE]: Release Apache ShardingSphere (Incubating) 4.0.0 [RC1]

```

Body:

```
Hello ShardingSphere Community,

This is a call for vote to release Apache ShardingSphere (Incubating) version 4.0.0-RC1

Release notes:
https://github.com/apache/incubator-shardingsphere/releases/edit/untagged-90bdf1e5cbba8422332f

The release candidates:
https://dist.apache.org/repos/dist/dev/incubator/shardingsphere/4.0.0-RC1/

Maven 2 staging repository:
https://repository.apache.org/content/repositories/staging/org/apache/shardingsphere/

Git tag for the release:
https://github.com/apache/incubator-shardingsphere/tree/4.0.0-RC1

Release Commit ID:
https://github.com/apache/incubator-shardingsphere/commit/90a17fd3ac5af99d0fe1bd8018ba1393b1864672

Keys to verify the Release Candidate:
https://dist.apache.org/repos/dist/dev/incubator/shardingsphere/KEYS

The vote will be open for at least 72 hours or until necessary number of votes are reached.

Please vote accordingly:

[ ] +1 approve 
[ ] +0 no opinion 
[ ] -1 disapprove with the reason

```

2. Apache Community Vote Template:

Title:

```
[VOTE]: Release Apache ShardingSphere (Incubating) 4.0.0 [RC1]

```

Body:

```
Hello all,

This is a call for vote to release Apache ShardingSphere (Incubating) version 4.0.0-RC1.

The Apache ShardingSphere community has voted on and approved a proposal to release
Apache ShardingSphere (Incubating) version 4.0.0-RC1.

We now kindly request the Incubator PMC members review and vote on this
incubator release.

ShardingSphere is an open-source ecosystem consisted of a set of distributed database middleware solutions, including 2 independent products, Sharding-JDBC & Sharding-Proxy. 
They both provide functions of data sharding, distributed transaction and database orchestration, applicable in a variety of situations such as Java isomorphism, heterogeneous language. 
Aiming at reasonably making full use of the computation and storage capacity of database in distributed system, ShardingSphere defines itself as a middleware, rather than a totally new type of database. 
As the cornerstone of many enterprises, relational database still takes a huge market share. 
Therefore, at current stage, we prefer to focus on its increment instead of a total overturn.

Sharding-JDBC defines itself as a lightweight Java framework that provides extra service at Java JDBC layer. 
With client end connecting directly to the database, it provides service in the form of jar and requires no extra deployment and dependence. 
It can be considered as an enhanced JDBC driver, which is fully compatible with JDBC and all kinds of ORM frameworks.

* Applicable in any ORM framework based on Java, such as JPA, Hibernate, Mybatis, Spring JDBC Template or direct use of JDBC.
* Based on any third-party database connection pool, such as DBCP, C3P0, BoneCP, Druid, HikariCP.
* Support any kind of database that conforms to JDBC standard: MySQL£¬Oracle£¬SQLServer and PostgreSQL for now.

Sharding-Proxy defines itself as a transparent database proxy, providing a database server that encapsulates database binary protocol to support heterogeneous languages. 
Friendlier to DBA, the MySQL/PostgreSQL version provided now can use any kind of client access (such as MySQL Command Client, MySQL Workbench, Navicat etc.) that is compatible of MySQL/PostgreSQL protocol to operate data.

* Totally transparent to applications, it can be used directly as MySQL and PostgreSQL.

* Applicable to any kind of compatible of client end that is compatible of MySQL and PostgreSQL protocol.

ShardingSphere community vote and result thread:
https://lists.apache.org/thread.html/xxxxxxxxxxxxxxxxxxxxxxx

Release notes:
https://github.com/apache/incubator-shardingsphere/releases/edit/untagged-90bdf1e5cbba8422332f

The release candidates:
https://dist.apache.org/repos/dist/dev/incubator/shardingsphere/4.0.0-RC1/

Maven 2 staging repository:
https://repository.apache.org/content/repositories/staging/org/apache/shardingsphere/

Git tag for the release:
https://github.com/apache/incubator-shardingsphere/tree/4.0.0-RC1

Release Commit ID:
https://github.com/apache/incubator-shardingsphere/commit/90a17fd3ac5af99d0fe1bd8018ba1393b1864672

Keys to verify the Release Candidate:
https://dist.apache.org/repos/dist/dev/incubator/shardingsphere/KEYS

Look at here for how to verify this release candidate:
https://shardingsphere.apache.org/community/en/contribute/release/

The vote will be open for at least 72 hours or until necessary number of
votes are reached.

Please vote accordingly:
[ ] +1 approve
[ ] +0 no opinion
[ ] -1 disapprove with the reason

```

3. Announce the vote result:

Title:

```
[RESULT] [VOTE]: Release Apache ShardingSphere (Incubating) 4.0.0 [RC1]

```

Body:

```
We’ve received 3 +1 binding votes and one +1 non-binding vote:

+1 binding, xxx
+1 binding, xxx
+1 binding, xxx

+1 non-binding, xxx

Thank you everyone for taking the time to review the release and help us. 
I will process to publish the release and send ANNOUNCE.

```

## Finish the Release

1. Move source packages and binary packages from the `dev` directory to `release` directory

```shell
svn mv https://dist.apache.org/repos/dist/dev/incubator/shardingsphere/4.0.0-RC1/ https://dist.apache.org/repos/dist/release/incubator/shardingsphere/
```

2. Find ShardingSphere in staging repository and click `Release`

3. Merge release branch to `dev` and delete release branch on Github

4. Send e-mail to `general@incubator.apache.org` and `dev@shardingsphere.apache.org` to announce the release is finished.

Announcement e-mail template:

Title:

```
[ANN] Apache ShardingSphere 4.0.0 [RC1] available

```

Body:

```
Hi all,

Apache ShardingSphere (incubating) Team is glad to announce the first release of Apache ShardingSphere Incubating 4.0.0-RC1.

ShardingSphere is an open-source ecosystem consisted of a set of distributed database middleware solutions, including 2 independent products, Sharding-JDBC & Sharding-Proxy. 
They both provide functions of data sharding, distributed transaction and database orchestration, applicable in a variety of situations such as Java isomorphism, heterogeneous language. 
Aiming at reasonably making full use of the computation and storage capacity of database in distributed system, ShardingSphere defines itself as a middleware, rather than a totally new type of database. 
As the cornerstone of many enterprises, relational database still takes a huge market share. 
Therefore, at current stage, we prefer to focus on its increment instead of a total overturn.

Sharding-JDBC defines itself as a lightweight Java framework that provides extra service at Java JDBC layer. 
With client end connecting directly to the database, it provides service in the form of jar and requires no extra deployment and dependence. 
It can be considered as an enhanced JDBC driver, which is fully compatible with JDBC and all kinds of ORM frameworks.

* Applicable in any ORM framework based on Java, such as JPA, Hibernate, Mybatis, Spring JDBC Template or direct use of JDBC.
* Based on any third-party database connection pool, such as DBCP, C3P0, BoneCP, Druid, HikariCP.
* Support any kind of database that conforms to JDBC standard: MySQL£¬Oracle£¬SQLServer and PostgreSQL for now.

Sharding-Proxy defines itself as a transparent database proxy, providing a database server that encapsulates database binary protocol to support heterogeneous languages. 
Friendlier to DBA, the MySQL/PostgreSQL version provided now can use any kind of client access (such as MySQL Command Client, MySQL Workbench, Navicat etc.) that is compatible of MySQL/PostgreSQL protocol to operate data.

* Totally transparent to applications, it can be used directly as MySQL and PostgreSQL.

* Applicable to any kind of compatible of client end that is compatible of MySQL and PostgreSQL protocol.

Vote Thread: 

Download Links: 

Release Notes: 

Website: https://shardingsphere.apache.org/

SkyWalking Resources:
- Issue: https://github.com/apache/incubator-shardingsphere/issues
- Mailing list: dev@shardingsphere.apache.org
- Documents: https://github.com/apache/incubator-shardingsphere/blob/dev/README.md
```
