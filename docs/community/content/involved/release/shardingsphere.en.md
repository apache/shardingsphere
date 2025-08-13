+++
title = "ShardingSphere Release Guide"
weight = 1
chapter = true
+++

## Prepare Before Release

The preparation work is carried out **7 days before cutting release**, so that contributors can control the development progress according to the release plan.

### 1. Check and Update LICENSE and NOTICE

Check and update dependency version in LICENSE.

Check and update year in NOTICE.

### 2. Confirm Release Notes

The release note should be provided in English / Chinese, confirm whether English and Chinese description are clear,
and shall be classified according to the following labels:

1. New Feature
1. API Change
1. Enhancement
1. Bug Fix

### 3. Create Milestone for Next Development

1. Create a [Github Milestone](https://github.com/apache/shardingsphere/milestones);
1. Set milestone title to next development version;
1. **Set the `due date` as the next version release cutting date** 。

### 4. Confirm Issue List

Open [GitHub issues](https://github.com/apache/shardingsphere/issues), filter the issue whose milestone is `${RELEASE.VERSION}` and status is open:

1. Close the completed issue;
1. For outstanding issues, communicate with the developer in charge. If this release is not affected, modify milestone to the next version;
1. Confirm that there is no issue in open status under milestone of release version.

### 5. Confirm Pull Request List

Open [GitHub pull requests](https://github.com/apache/shardingsphere/pulls), filter pull requests whose milestone is `${RELEASE.VERSION}` and status is open:

1. Review the open pull request and merge;
1. For pull requests that cannot merge and do not affect this release, modify milestone to the next version;
1. Confirm that there is no open pull request under milestone of release version.

### 6. Call for a Discussion

1. Create a [GitHub Discussion](https://github.com/apache/shardingsphere/discussions) contains all the release notes and **release cutting date** ;
1. Send email to [dev@shardingsphere.apache.org](mailto:dev@shardingsphere.apache.org) with the GitHub Discussion and **release cutting date** in the message body;
1. Follow the mailing list and confirm that the community developers have no questions about the release note.

## GPG Settings

### 1. Install GPG

Download installation package on [official GnuPG website](https://www.gnupg.org/download/index.html).
The command of GnuPG 1.x version can differ a little from that of 2.x version.
The following instructions take `GnuPG-2.1.23` version for example.
After the installation, execute the following command to check the version number.

```shell
gpg --version
```

### 2. Create Key

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

> To be noticed: Please use personal Apache email address for key creation.

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

### 3. Check Generated Key

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

Or run `gpg --list-sigs` to query it.

### 4. Upload the Public Key to Key Server

The command is as follows:

```shell
gpg --keyserver hkp://keyserver.ubuntu.com --send-key 700E6065
```

`keyserver.ubuntu.com` is randomly chosen from public key server.
Each server will automatically synchronize with one another, so it would be okay to choose any one.

## Prepare Branch for Release

### 1. Close milestone

Open [GitHub milestone](https://github.com/apache/shardingsphere/milestones)

1. Confirm that the milestone completion status of `${RELEASE.VERSION}` is 100%;
1. Click `close` to close milestone.

### 2. Confirm the Release Commit and Create Release Branch

Suppose ShardingSphere source codes downloaded from GitHub is under `~/open_source/shardingsphere/`, clone a new one into `~/shardingsphere/` directory from local.

Suppose the version to be released is `${RELEASE.VERSION}`, create `${RELEASE.VERSION}-release` branch, where all the following operations will be performed.

Reference command:
```shell
cd ~
git clone ~/open_source/shardingsphere
cd ~/shardingsphere/
git remote remove origin
git remote add origin https://github.com/apache/shardingsphere
git fetch
git checkout -b master --track origin/master
git checkout -b ${RELEASE.VERSION}-release
git push origin ${RELEASE.VERSION}-release
```

### 3. Update Release Notes And Example Version

Update the following file in release branch, and submit a PR to release branch:

```
https://github.com/apache/shardingsphere/blob/${RELEASE.VERSION}-release/RELEASE-NOTES.md
```

Update the POM of the module `examples`, changing the version from ${DEVELOPMENT.VERSION} to ${RELEASE.VERSION}, and submit a PR to release branch.

### 4. Update the download page

Update the following pages:
* <https://shardingsphere.apache.org/document/current/en/downloads/>
* <https://shardingsphere.apache.org/document/current/cn/downloads/>

GPG signatures and hashes (SHA* etc) should be prefixed with `https://downloads.apache.org/shardingsphere/`

### 5. Update README files

Update `${RELEASE.VERSION}` and `${NEXT.RELEASE.VERSION}` in README.md and README_ZH.md.

### 6. Update ShardingSphereDriver

Update `MAJOR_DRIVER_VERSION` and `MINOR_DRIVER_VERSION` in ShardingSphereDriver.java.

## Apache Maven Central Repository Release

### 1. Set settings-security.xml and settings.xml

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

### 2. Pre-Release Check

```shell
export GPG_TTY=$(tty)
```

```shell
./mvnw release:prepare -P-dev,release,all -Darguments="-DskipTests" -DautoVersionSubmodules=true -DdryRun=true -Dusername=${Github username}
```

-P-dev,release,all: choose release profile, which will pack all the source codes, jar files and executable binary packages of ShardingSphere-Proxy.

-DautoVersionSubmodules=true: it can make the version number is inputted only once and not for each sub-module.

-DdryRun=true: rehearsal, which means not to generate or submit new version number and new tag.

### 3. Prepare for the Release

First, clean local pre-release check information.

```shell
./mvnw release:clean
```

Then, prepare to execute the release.

```shell
./mvnw release:prepare -P-dev,release,all -Darguments="-DskipTests" -DautoVersionSubmodules=true -DpushChanges=false -Dusername=${Github username}
```

It is basically the same as the previous rehearsal command, but deleting -DdryRun=true parameter.

-DpushChanges=false: do not submit the edited version number and tag to Github automatically.

**Refer to [Check Release](#check-release), after making sure there is no mistake in local files**, submit them to GitHub.

```shell
git push origin ${RELEASE.VERSION}-release
git push origin ${RELEASE.VERSION}
```
### 4. Deploy the Release

```shell
./mvnw release:perform -P-dev,release,all -Darguments="-DskipTests" -DautoVersionSubmodules=true -DlocalCheckout=true -Dusername=${Github username}
```

-DlocalCheckout=true: checkout code from local repository instead of remote repository.

After that command is executed, the version to be released will be uploaded to Apache staging repository automatically.

Visit [staging repository](https://repository.apache.org/#stagingRepositories) and use Apache LDAP account to log in; then you can see the uploaded version, the content of `Repository` column is the ${STAGING.REPOSITORY}.

Click `Close` to tell Nexus that the construction is finished, because only in this way, this version can be usable.
If there is any problem in gpg signature, `Close` will fail, but you can see the failure information through `Activity`.

## Apache SVN Repository Release

### 1. Checkout ShardingSphere Release Directory

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

### 2. Add gpg Public Key and commit

Only the account in its **first deployment** needs to add that.
It is alright for `KEYS` to only include the public key of the deployed account.

```shell
gpg -a --export ${GPG username} >> KEYS
svn --username=${APACHE LDAP username} commit -m 'Add gpg key for ${APACHE LDAP username}'
```

You could run `gpg --show-keys KEYS` to check whether your public key is added or not.

### 3. Add the Release Content to SVN Directory

Create folder by version number.

```shell
mkdir ${RELEASE.VERSION}
```

Add source code packages, binary packages and executable binary packages of ShardingSphere-Proxy to SVN working directory.

```shell
cd ${RELEASE.VERSION}
cp -f ~/shardingsphere/distribution/src/target/*.zip* .
cp -f ~/shardingsphere/distribution/jdbc/target/*.tar.gz* .
cp -f ~/shardingsphere/distribution/proxy/target/*.tar.gz* .
cp -f ~/shardingsphere/distribution/agent/target/*.tar.gz* .
```

### 4. Commit to Apache SVN

```shell
svn add * --parents
svn --username=${APACHE LDAP username} commit -m "release ${RELEASE.VERSION}"
```

## Check Release

### 1. Check sha512 hash

```shell
shasum -c *.sha512
```

### 2. Check gpg Signature

First, import releaser's public key. Import KEYS from SVN repository to local. (The releaser does not need to import again; the checking assistant needs to import it, with the user name filled as the releaser's. )

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

Checking can be performed by the following command under Bash:
```bash
for each in $(ls *.asc); do gpg --verify $each ${each%.asc}; done
```

Or checking each file manually:
```shell
gpg --verify apache-shardingsphere-${RELEASE.VERSION}-src.zip.asc apache-shardingsphere-${RELEASE.VERSION}-src.zip
gpg --verify apache-shardingsphere-${RELEASE.VERSION}-shardingsphere-jdbc-bin.tar.gz.asc apache-shardingsphere-${RELEASE.VERSION}-shardingsphere-jdbc-bin.tar.gz
gpg --verify apache-shardingsphere-${RELEASE.VERSION}-shardingsphere-proxy-bin.tar.gz.asc apache-shardingsphere-${RELEASE.VERSION}-shardingsphere-proxy-bin.tar.gz
gpg --verify apache-shardingsphere-${RELEASE.VERSION}-shardingsphere-agent-bin.tar.gz.asc apache-shardingsphere-${RELEASE.VERSION}-shardingsphere-agent-bin.tar.gz
```

### 3. Check Released Files

**3.1 Compare release source with github tag**

```
curl -Lo tag-${RELEASE.VERSION}.zip https://github.com/apache/shardingsphere/archive/${RELEASE.VERSION}.zip
unzip tag-${RELEASE.VERSION}.zip
unzip apache-shardingsphere-${RELEASE.VERSION}-src.zip
diff -r apache-shardingsphere-${RELEASE.VERSION}-src-release shardingsphere-${RELEASE.VERSION}
```

**3.2 Check source package**

*   Check whether source tarball is oversized for including nonessential files
*   `LICENSE` and `NOTICE` files exist
*   Correct year in `NOTICE` file
*   There is only text files but no binary files
*   All source files have ASF headers
*   Codes can be compiled and pass the unit tests (./mvnw -T 1C install)
*   Check if there is any extra files or folders, empty folders for example

**3.3 Check binary packages**

Decompress
- `apache-shardingsphere-${RELEASE.VERSION}-shardingsphere-jdbc-bin.tar.gz`
- `apache-shardingsphere-${RELEASE.VERSION}-shardingsphere-proxy-bin.tar.gz`
- `apache-shardingsphere-${RELEASE.VERSION}-shardingsphere-agent-bin.tar.gz`

And check the following items:

*   `LICENSE` and `NOTICE` files exist
*   Correct year in `NOTICE` file
*   All text files have ASF headers
*   Check the third party dependency license:
    *   The software has a compatible license
    *   All software licenses mentioned in `LICENSE`
    *   All the third party dependency licenses are under `licenses` folder
    *   If it depends on Apache license and has a `NOTICE` file, that `NOTICE` file need to be added to `NOTICE` file of the release

## Call for a Vote

**Vote procedure**

1. ShardingSphere community vote: send the vote e-mail to `dev@shardingsphere.apache.org`.
PMC needs to check the rightness of the version according to the document before they vote.
After at least **72 hours** and with at least **3 `+1 PMC member`** votes, it can come to the next stage of the vote.

2. Announce the vote result: send the result vote e-mail to [dev@shardingsphere.apache.org](mailto:dev@shardingsphere.apache.org).

**Vote Templates**

1. ShardingSphere Community Vote Template

Title:

```
[VOTE] Release Apache ShardingSphere ${RELEASE.VERSION}

```

Body:

```
Hello ShardingSphere Community,

This is a call for vote to release Apache ShardingSphere version ${RELEASE.VERSION}

Release notes:
https://github.com/apache/shardingsphere/blob/${RELEASE.VERSION}-release/RELEASE-NOTES.md

The release candidates:
https://dist.apache.org/repos/dist/dev/shardingsphere/${RELEASE.VERSION}/

Maven 2 staging repository:
https://repository.apache.org/content/repositories/${STAGING.REPOSITORY}/org/apache/shardingsphere/

Git tag for the release:
https://github.com/apache/shardingsphere/tree/${RELEASE.VERSION}

Release Commit ID:
https://github.com/apache/shardingsphere/commit/xxxxxxxxxxxxxxxxxxxxxxx

Keys to verify the Release Candidate:
https://dist.apache.org/repos/dist/dev/shardingsphere/KEYS

Look at here for how to verify this release candidate:
https://shardingsphere.apache.org/community/en/involved/release/shardingsphere/

GPG user ID:
${YOUR.GPG.USER.ID}

The vote will be open for at least 72 hours or until necessary number of votes are reached.

Please vote accordingly:

[ ] +1 approve

[ ] +0 no opinion

[ ] -1 disapprove with the reason

PMC vote is "+1 binding", all others is "+1 non-binding".

Checklist for reference:

[ ] Download links are valid.

[ ] Checksums and PGP signatures are valid.

[ ] Source code distributions have correct names matching the current release.

[ ] LICENSE and NOTICE files are correct for each ShardingSphere repo.

[ ] All files have license headers if necessary.

[ ] No compiled archives bundled in source archive.
```

> To be noticed: `Release Commit ID` uses the commit id corresponding to `prepare release ${RELEASE.VERSION}` log on release branch.

2. Announce the vote result:

Title：

```
[RESULT][VOTE] Release Apache ShardingSphere ${RELEASE.VERSION}
```

Body:

```
We’ve received 3 "+1 binding" votes and one "+1 non-binding" vote:

+1 binding, xxx
+1 binding, xxx
+1 binding, xxx

+1 non-binding, xxx

Thank you everyone for taking the time to review the release and help us.
I will process to publish the release and send ANNOUNCE.

```

## Finish the Release

### 1. Move source packages, binary packages and KEYS from the `dev` directory to `release` directory

> Note: This step requires the help of PMC.

Move release candidates to release area:
```shell
svn mv https://dist.apache.org/repos/dist/dev/shardingsphere/${RELEASE.VERSION} https://dist.apache.org/repos/dist/release/shardingsphere/ -m "transfer packages for ${RELEASE.VERSION}"
```

If KEYS has changed, update the KEYS file in the release area:
```shell
svn delete https://dist.apache.org/repos/dist/release/shardingsphere/KEYS -m "delete KEYS"
svn cp https://dist.apache.org/repos/dist/dev/shardingsphere/KEYS https://dist.apache.org/repos/dist/release/shardingsphere/ -m "transfer KEYS for ${RELEASE.VERSION}"
```

### 2. Find ShardingSphere in [staging repository](https://repository.apache.org/#stagingRepositories ) and click `Release`

### 3. Docker Release

3.1 Preparation

Install and start docker service

(You may skip this step if you are using Docker Desktop) Configure QEMU:
```shell
docker run --privileged --rm tonistiigi/binfmt --install all
```

Refer to: [Docker Buildx: Build multi-platform images](https://docs.docker.com/buildx/working-with-buildx/#build-multi-platform-images)

3.2 Login Docker Registry

```shell
docker login
```

3.3 Build and push ShardingSphere-Proxy Docker image

```shell
cd ~/shardingsphere
git checkout ${RELEASE.VERSION}
./mvnw -pl distribution/proxy -B -P-dev,release,all,docker.buildx.push clean package
```

3.4 Confirm the successful release

Go to [Docker Hub](https://hub.docker.com/r/apache/shardingsphere-proxy/) and check whether there is a published image. And make sure that the image supports both `linux/amd64` and `linux/arm64`.

```shell
docker logout
```

3.5 Log in to GitHub Packages Container Registry

```shell
docker login ghcr.io/apache/shardingsphere
```

3.6 Build and push ShardingSphere Agent Docker image

```shell
cd ~/shardingsphere
git checkout ${RELEASE.VERSION}
./mvnw -am -pl distribution/agent -P-dev,release,all,docker.buildx.push -T 1C -DskipTests clean package
```

3.7 Confirm the successful release

Check [GitHub Packages](https://github.com/apache/shardingsphere/pkgs/container/shardingsphere-agent) for released images, and make sure that the image supports both `linux/amd64` and `linux/arm64`.

```shell
docker logout
```

### 4. Publish release on GitHub

Click `Draft a new release` in [GitHub Releases](https://github.com/apache/shardingsphere/releases).

Edit release version and release notes, select `Set as the latest release`, click `Publish release`.

### 5. Remove previous release from Release Area

> Note: This step requires the help of PMC.

Keep the latest version in [**Release Area**](https://dist.apache.org/repos/dist/release/shardingsphere/) only.

Incubating stage versions will be archived automatically in [Archive repository](https://archive.apache.org/dist/incubator/shardingsphere/)

Remove the previous release from the [**Release Area**](https://dist.apache.org/repos/dist/release/shardingsphere/) after confirming the previous release exists in [Archive repository](https://archive.apache.org/dist/shardingsphere/),

```shell
svn del -m "Archiving release ${PREVIOUS.RELEASE.VERSION}" https://dist.apache.org/repos/dist/release/shardingsphere/${PREVIOUS.RELEASE.VERSION}
```

Previous releases will be archived automatically in [Archive repository](https://archive.apache.org/dist/shardingsphere/).

Incubating stage versions will be archived automatically in [Incubator Archive repository](https://archive.apache.org/dist/incubator/shardingsphere/)

Refer to [Release Download Pages for Projects](https://infra.apache.org/release-download-pages.html).

### 6. Add entrance of documents of the new release into home page

Refer to commit:
Update the version number of document files, including index.html, indexzh.html, learning.html, legacy.html, and legacyzh.html under the shardingsphere-doc repository to the current version. [Reference commit](https://github.com/apache/shardingsphere-doc/commit/9fdf438d1170129d2690b5dee316403984579430)
Update language.html(docs/document/themes/hugo-theme-learn/layouts/partials/language.html) under the shardingsphere repository and add the current version number for navigation. [Reference commit](https://github.com/apache/shardingsphere/pull/29017/files)

### 7. Update Example Version

Update the POM of the module examples, changing the version from ${RELEASE.VERSION} to ${NEXT.DEVELOPMENT.VERSION}, and commit changes to release branch.

### 8. Merge release branch to `master` and delete release branch on GitHub

After confirmed that download links of new release in download pages are available, create a Pull Request on GitHub to merge `${RELEASE.VERSION}-release` into `master.
If code conflicted, you may merge `master` into `${RELEASE.VERSION}-release` before merging Pull Request.

### 9. Announce release completed by email

Send e-mail to `dev@shardingsphere.apache.org` and `announce@apache.org` with **plain text mode** to announce the release is completed.

Announcement e-mail template:

Title:

```
[ANNOUNCE] Apache ShardingSphere ${RELEASE.VERSION} available
```

Body:

```
Hi all,

Apache ShardingSphere Team is glad to announce the new release of Apache ShardingSphere ${RELEASE.VERSION}.

Apache ShardingSphere is an open source ecosystem that allows you to transform any database into a distributed database system.
The project includes a JDBC and a Proxy, and its core adopts a micro-kernel and pluggable architecture.
Thanks to its plugin-oriented architecture, features can be flexibly expanded at will.

The project is committed to providing a multi-source heterogeneous, enhanced database platform and further building an ecosystem around the upper layer of the platform.
Database Plus, the design philosophy of Apache ShardingSphere, aims at building the standard and ecosystem on the upper layer of the heterogeneous database.
It focuses on how to make full and reasonable use of the computing and storage capabilities of existing databases rather than creating a brand new database.
It attaches greater importance to the collaboration between multiple databases instead of the database itself.

Download Links: https://shardingsphere.apache.org/document/current/en/downloads/

Release Notes: https://github.com/apache/shardingsphere/blob/master/RELEASE-NOTES.md

Website: https://shardingsphere.apache.org/

ShardingSphere Resources:
- Issue: https://github.com/apache/shardingsphere/issues/
- Mailing list: dev@shardingsphere.apache.org
- Documents: https://shardingsphere.apache.org/document/current/



- Apache ShardingSphere Team

```

## Appendix: How to abort release procedure

You may refer to the following steps to abort current release procedure if you found any problem which blocks the release procedure.

### Vote down the release and describe the reason

Reply -1 to voting e-mail and describe the reason.

### Remove release candidates from dev area

```shell
svn del https://dist.apache.org/repos/dist/dev/shardingsphere/${RELEASE.VERSION} -m "Drop ${RELEASE.VERSION} release candidates"
```

### Drop Maven Staging Repository

Check the Staging Repository in <https://repository.apache.org/#stagingRepositories> and **Drop** it。

### Reset release branch and delete tag

Reset branch `${RELEASE.VERSION}-release` to the commit before the commits made by `maven-release-plugin`:
```shell
git checkout ${RELEASE.VERSION}-release
git reset --hard ${COMMIT_ID_BEFORE_RELEASE}
git push origin --force
```

Delete tag：
```shell
git tag -d ${RELEASE.VERSION}
git push origin -d ${RELEASE.VERSION}
```
