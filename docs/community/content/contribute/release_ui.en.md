+++
title = "ShardingSphere-UI Release Guide"
weight = 7
chapter = true
+++

## GPG Settings

Please refer to [Release Guide](/en/contribute/release/).

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

### Update Release Notes

```
https://github.com/apache/shardingsphere-ui/blob/master/RELEASE-NOTES.md
```

### Create Release Branch

Suppose ShardingSphere source codes downloaded from github is under `~/shardingsphere-ui/` directory and the version to be released is `${RELEASE.VERSION}`. 
Create `${RELEASE.VERSION}-release` branch, where all the following operations are performed.

```shell
## ${name} is the properly branch, e.g. master, dev-4.x
git clone --branch ${name} https://github.com/apache/shardingsphere-ui.git ~/shardingsphere-ui
cd ~/shardingsphere-ui/
git pull
git checkout -b ${RELEASE.VERSION}-release
git push origin ${RELEASE.VERSION}-release
```

### Pre-Release Check

```shell
cd ~/shardingsphere-ui
mvn release:prepare -Prelease -Darguments="-DskipTests" -DautoVersionSubmodules=true -DdryRun=true -Dusername=${Github username}
```

-Prelease: choose release profile, which will pack all the source codes, jar files and executable binary packages of ShardingSphere-UI.

-DautoVersionSubmodules=true: it can make the version number is inputted only once and not for each sub-module.

-DdryRun=true: rehearsal, which means not to generate or submit new version number and new tag.

### Prepare for the Release

First, clean local pre-release check information.

```shell
cd ~/shardingsphere-ui
mvn release:clean
```

Then, prepare to execute the release.

```shell
cd ~/shardingsphere-ui
mvn release:prepare -Prelease -Darguments="-DskipTests" -DautoVersionSubmodules=true -DpushChanges=false -Dusername=${Github username}
```

It is basically the same as the previous rehearsal command, but deleting -DdryRun=true parameter.

-DpushChanges=false: do not submit the edited version number and tag to Github automatically.

After making sure there is no mistake in local files, submit them to GitHub.

```shell
git push origin ${RELEASE.VERSION}-release
git push origin --tags
```

### Deploy the Release

```shell
cd ~/shardingsphere-ui
mvn release:perform -Prelease -Darguments="-DskipTests" -DautoVersionSubmodules=true -Dusername=${Github username}
```

## Apache SVN Repository Release

### Checkout ShardingSphere Release Directory

If there is no local work directory, create one at first.

```shell
mkdir -p ~/ss_svn/dev/
cd ~/ss_svn/dev/
```

After the creation, checkout ShardingSphere release directory from Apache SVN.

```shell
svn --username=${APACHE LDAP username} co https://dist.apache.org/repos/dist/dev/shardingsphere
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
mkdir -p ~/ss_svn/dev/shardingsphere/shardingsphere-ui-${RELEASE.VERSION}
cd ~/ss_svn/dev/shardingsphere/shardingsphere-ui-${RELEASE.VERSION}
```

Add source code packages, binary packages and executable binary packages of ShardingSphere-Proxy to SVN working directory.

```shell
cp -f ~/shardingsphere-ui/shardingsphere-ui-distribution/shardingsphere-ui-src-distribution/target/*.zip ~/ss_svn/dev/shardingsphere/shardingsphere-ui-${RELEASE.VERSION}
cp -f ~/shardingsphere-ui/shardingsphere-ui-distribution/shardingsphere-ui-src-distribution/target/*.zip.asc ~/ss_svn/dev/shardingsphere/shardingsphere-ui-${RELEASE.VERSION}
cp -f ~/shardingsphere-ui/shardingsphere-ui-distribution/shardingsphere-ui-bin-distribution/target/*.tar.gz ~/ss_svn/dev/shardingsphere/shardingsphere-ui-${RELEASE.VERSION}
cp -f ~/shardingsphere-ui/shardingsphere-ui-distribution/shardingsphere-ui-bin-distribution/target/*.tar.gz.asc ~/ss_svn/dev/shardingsphere/shardingsphere-ui-${RELEASE.VERSION}
```

### Generate sign files

```shell
shasum -a 512 apache-shardingsphere-${RELEASE.VERSION}-shardingsphere-ui-src.zip >> apache-shardingsphere-${RELEASE.VERSION}-shardingsphere-ui-src.zip.sha512
shasum -b -a 512 apache-shardingsphere-${RELEASE.VERSION}-shardingsphere-ui-bin.tar.gz >> apache-shardingsphere-${RELEASE.VERSION}-shardingsphere-ui-bin.tar.gz.sha512
```

### Commit to Apache SVN

```shell
cd ~/ss_svn/dev/shardingsphere/
svn add shardingsphere-ui-${RELEASE.VERSION}
svn --username=${APACHE LDAP 用户名} commit -m "release shardingsphere-ui-${RELEASE.VERSION}"
```

## Check Release

### Check sha512 hash

```shell
shasum -c apache-shardingsphere-${RELEASE.VERSION}-shardingsphere-ui-src.zip.sha512
shasum -c apache-shardingsphere-${RELEASE.VERSION}-shardingsphere-ui-bin.tar.gz.sha512
```

### Check gpg Signature

First, import releaser's public key. 
Import KEYS from SVN repository to local. (The releaser does not need to import again; the checking assistant needs to import it, with the user name filled as the releaser's. )

```shell
curl https://dist.apache.org/repos/dist/dev/shardingsphere/KEYS >> KEYS
gpg --import KEYS
gpg --edit-key "${GPG username of releaser}"
  > trust

Please decide how far you trust this user to correctly verify other users' keys
(by looking at passports, checking fingerprints from different sources, etc.)

  1 = I don't know or won't say
  2 = I do NOT trust
  3 = I trust marginally
  4 = I trust fully
  5 = I trust ultimately
  m = back to the main menu

Your decision? 5

  > save
```

Then, check the gpg signature.

```shell
gpg --verify apache-shardingsphere-${RELEASE.VERSION}-shardingsphere-ui-src.zip.asc apache-shardingsphere-${RELEASE.VERSION}-shardingsphere-ui-src.zip
gpg --verify apache-shardingsphere-${RELEASE.VERSION}-shardingsphere-ui-bin.tar.gz.asc apache-shardingsphere-${RELEASE.VERSION}-shardingsphere-ui-bin.tar.gz
```

### Check Released Files

#### compare release source with github tag

```
curl -Lo tag-shardingsphere-ui-${RELEASE.VERSION}.zip https://github.com/apache/shardingsphere-ui/archive/shardingsphere-ui-${RELEASE.VERSION}.zip
unzip tag-shardingsphere-ui-${RELEASE.VERSION}.zip
unzip apache-shardingsphere-${RELEASE.VERSION}-shardingsphere-ui-src.zip
diff -r apache-shardingsphere-${RELEASE.VERSION}-shardingsphere-ui-src/ shardingsphere-shardingsphere-ui-${RELEASE.VERSION}/
```

#### Check source package

*   Check whether source tarball is oversized for including nonessential files
*   `LICENSE` and `NOTICE` files exist
*   Correct year in `NOTICE` file
*   There is only text files but no binary files
*   All source files have ASF headers
*   Codes can be compiled and pass the unit tests (./mvnw install)
*   Check if there is any extra files or folders, empty folders for example

#### Check binary packages

Decompress `apache-shardingsphere-${RELEASE.VERSION}-shardingsphere-ui-bin.tar.gz`
to check the following items:

*   `LICENSE` and `NOTICE` files exist
*   Correct year in `NOTICE` file
*   All text files have ASF headers
*   Check the third party dependency license:
    *   The software have a compatible license
    *   All software licenses mentioned in `LICENSE`
    *   All the third party dependency licenses are under `licenses` folder
    *   If it depends on Apache license and has a `NOTICE` file, that `NOTICE` file need to be added to `NOTICE` file of the release

## Call for a Vote

### Vote procedure

1. ShardingSphere community vote: send the vote e-mail to `dev@shardingsphere.apache.org`. 
PMC needs to check the rightness of the version according to the document before they vote. 
After at least 72 hours and with at least 3 `+1 PMC member` votes, it can come to the next stage of the vote.

2. Announce the vote result: send the result vote e-mail to `dev@shardingsphere.apache.org`.

### Vote Templates

1. ShardingSphere Community Vote Template

Title:

```
[VOTE] Release Apache ShardingSphere UI ${RELEASE.VERSION}

```

Body:

```
Hello ShardingSphere Community,

This is a call for vote to release Apache ShardingSphere UI version ${RELEASE.VERSION}

Release notes:
https://github.com/apache/shardingsphere-ui/blob/master/RELEASE-NOTES.md

The release candidates:
https://dist.apache.org/repos/dist/dev/shardingsphere/shardingsphere-ui-${RELEASE.VERSION}/

Git tag for the release:
https://github.com/apache/shardingsphere-ui/tree/shardingsphere-ui-${RELEASE.VERSION}/

Release Commit ID:
https://github.com/apache/shardingsphere-ui/commit/xxxxxxxxxxxxxxxxxxxxxxx

Keys to verify the Release Candidate:
https://dist.apache.org/repos/dist/dev/shardingsphere/KEYS

Look at here for how to verify this release candidate:
https://shardingsphere.apache.org/community/en/contribute/release_ui/

GPG user ID:
${YOUR.GPG.USER.ID}

The vote will be open for at least 72 hours or until necessary number of votes are reached.

Please vote accordingly:

[ ] +1 approve 

[ ] +0 no opinion
 
[ ] -1 disapprove with the reason

PMC vote is +1 binding, all others is +1 non-binding.

Checklist for reference:

[ ] Download links are valid.

[ ] Checksums and PGP signatures are valid.

[ ] Source code distributions have correct names matching the current release.

[ ] LICENSE and NOTICE files are correct for each ShardingSphere repo.

[ ] All files have license headers if necessary.

[ ] No compiled archives bundled in source archive.
```

2. Announce the vote result:

Title：

```
[RESULT][VOTE] Release Apache ShardingSphere UI ${RELEASE.VERSION}
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

### Move source packages, binary packages and KEYS from the `dev` directory to `release` directory

```shell
svn mv https://dist.apache.org/repos/dist/dev/shardingsphere/shardingsphere-ui-${RELEASE.VERSION} https://dist.apache.org/repos/dist/release/shardingsphere/ -m "transfer packages for shardingsphere-ui-${RELEASE.VERSION}"
svn delete https://dist.apache.org/repos/dist/release/shardingsphere/KEYS -m "delete KEYS"
svn cp https://dist.apache.org/repos/dist/dev/shardingsphere/KEYS https://dist.apache.org/repos/dist/release/shardingsphere/ -m "transfer KEYS for shardingsphere-ui-${RELEASE.VERSION}"
```

### Merge release branch to `master` and delete release branch on Github

```shell
git checkout master
git merge origin/${RELEASE.VERSION}-release
git push origin master
git push --delete origin ${RELEASE.VERSION}-release
```

### Update the download page

https://shardingsphere.apache.org/document/current/en/downloads/

https://shardingsphere.apache.org/document/current/cn/downloads/

GPG signatures and hashes (SHA* etc) should use URL start with `https://downloads.apache.org/shardingsphere/`

Keep one latest versions in `Latest releases`. Incubating stage versions will be archived automatically in [Archive repository](https://archive.apache.org/dist/incubator/shardingsphere/)

### Publish release in GitHub

Click `Edit` in [GitHub Releases](https://github.com/apache/shardingsphere-ui/releases)'s `shardingsphere-ui-${RELEASE_VERSION}` version

Edit version number and release notes, click `Publish release`

### Send e-mail to `dev@shardingsphere.apache.org` and `announce@apache.org` to announce the release is finished

Announcement e-mail template:

Title:

```
[ANNOUNCE] Apache ShardingSphere UI ${RELEASE.VERSION} available
```

Body:

```
Hi all,

Apache ShardingSphere Team is glad to announce the new release of Apache ShardingSphere UI ${RELEASE.VERSION}.

ShardingSphere is an open-source ecosystem consisted of a set of distributed database middleware solutions, including 2 independent products, ShardingSphere-JDBC & ShardingSphere-Proxy. 
They both provide functions of data sharding, distributed transaction and database orchestration, applicable in a variety of situations such as Java isomorphism, heterogeneous language. 
Aiming at reasonably making full use of the computation and storage capacity of the database in a distributed system, ShardingSphere defines itself as a middleware, rather than a totally new type of database. 
As the cornerstone of many enterprises, relational database still takes a huge market share. 
Therefore, at the current stage, we prefer to focus on its increment instead of a total overturn.

Download Links: https://shardingsphere.apache.org/document/current/en/downloads/

Release Notes: https://github.com/apache/shardingsphere-ui/blob/master/RELEASE-NOTES.md

Website: https://shardingsphere.apache.org/

ShardingSphere Resources:
- Issue: https://github.com/apache/shardingsphere-ui/issues/
- Mailing list: dev@shardingsphere.apache.org
- Documents: https://shardingsphere.apache.org/document/current/



- Apache ShardingSphere Team

```
