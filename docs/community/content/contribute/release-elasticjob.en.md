+++
title = "ElasticJob Release Guide"
weight = 8
chapter = true
+++

## GPG Settings

Please refer to [Release Guide](/en/contribute/release/).

## Apache Maven Central Repository Release

**1. Set settings.xml**

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

**2. Update Release Notes**

Update the following file in master branch, and submit a PR to master branch:

```
https://github.com/apache/shardingsphere-elasticjob/blob/master/RELEASE-NOTES.md
```

**3. Create Release Branch**

Suppose ElasticJob source codes downloaded from github is under `~/elasticjob/` directory and the version to be released is `${RELEASE.VERSION}`. 
Create `${RELEASE.VERSION}-release` branch, where all the following operations are performed.

```shell
## ${name} is the properly branch, e.g. master, dev-4.x
git clone --branch ${name} https://github.com/apache/shardingsphere-elasticjob.git ~/elasticjob
cd ~/elasticjob/
git pull
git checkout -b ${RELEASE.VERSION}-release
git push origin ${RELEASE.VERSION}-release
```

**4. Pre-Release Check**

```shell
mvn release:prepare -Prelease -Darguments="-DskipTests" -DautoVersionSubmodules=true -DdryRun=true -Dusername=${Github username}
```

-Prelease: choose release profile, which will pack all the source codes and jar files.

-DautoVersionSubmodules=true: it can make the version number is inputted only once and not for each sub-module.

-DdryRun=true: rehearsal, which means not to generate or submit new version number and new tag.

**5. Prepare for the Release**

First, clean local pre-release check information.

```shell
mvn release:clean
```

Then, prepare to execute the release. Before releasing, update the POM of the module `examples`, changing the version from ${CURRENT.VERSION} to ${RELEASE.VERSION}.

```shell
mvn release:prepare -Prelease -Darguments="-DskipTests" -DautoVersionSubmodules=true -DpushChanges=false -Dusername=${Github username}
```

It is basically the same as the previous rehearsal command, but deleting -DdryRun=true parameter.

-DpushChanges=false: do not submit the edited version number and tag to Github automatically.

After making sure there is no mistake in local files, submit them to GitHub.

```shell
git push origin ${RELEASE.VERSION}-release
git push origin --tags
```

**6. Deploy the Release**

```shell
mvn release:perform -Prelease -Darguments="-DskipTests" -DautoVersionSubmodules=true -Dusername=${Github username}
```

After that command is executed, the version to be released will be uploaded to Apache staging repository automatically. 
Visit [https://repository.apache.org/#stagingRepositories](https://repository.apache.org/#stagingRepositories) and use Apache LDAP account to log in; then you can see the uploaded version, the content of `Repository` column is the ${STAGING.REPOSITORY}. 
Click `Close` to tell Nexus that the construction is finished, because only in this way, this version can be usable. 
If there is any problem in gpg signature, `Close` will fail, but you can see the failure information through `Activity`.

## Apache SVN Repository Release

**1. Checkout ShardingSphere Release Directory**

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

**2. Add gpg Public Key**

Only the account in its first deployment needs to add that. 
It is alright for `KEYS` to only include the public key of the deployed account.

```shell
gpg -a --export ${GPG username} >> KEYS
```

**3. Add the Release Content to SVN Directory**

Create folder by version number.

```shell
mkdir -p ~/ss_svn/dev/shardingsphere/elasticjob-${RELEASE.VERSION}
cd ~/ss_svn/dev/shardingsphere/elasticjob-${RELEASE.VERSION}
```

Add source code packages and binary packages to SVN working directory.

```shell
cp -f ~/elasticjob/elasticjob-distribution/elasticjob-src-distribution/target/*.zip ~/ss_svn/dev/shardingsphere/elasticjob-${RELEASE.VERSION}
cp -f ~/elasticjob/elasticjob-distribution/elasticjob-src-distribution/target/*.zip.asc ~/ss_svn/dev/shardingsphere/elasticjob-${RELEASE.VERSION}
cp -f ~/elasticjob/elasticjob-distribution/elasticjob-lite-distribution/target/*.tar.gz ~/ss_svn/dev/shardingsphere/elasticjob-${RELEASE.VERSION}
cp -f ~/elasticjob/elasticjob-distribution/elasticjob-lite-distribution/target/*.tar.gz.asc ~/ss_svn/dev/shardingsphere/elasticjob-${RELEASE.VERSION}
cp -f ~/elasticjob/elasticjob-distribution/elasticjob-cloud-executor-distribution/target/*.tar.gz ~/ss_svn/dev/shardingsphere/elasticjob-${RELEASE.VERSION}
cp -f ~/elasticjob/elasticjob-distribution/elasticjob-cloud-executor-distribution/target/*.tar.gz.asc ~/ss_svn/dev/shardingsphere/elasticjob-${RELEASE.VERSION}
cp -f ~/elasticjob/elasticjob-distribution/elasticjob-cloud-scheduler-distribution/target/*.tar.gz ~/ss_svn/dev/shardingsphere/elasticjob-${RELEASE.VERSION}
cp -f ~/elasticjob/elasticjob-distribution/elasticjob-cloud-scheduler-distribution/target/*.tar.gz.asc ~/ss_svn/dev/shardingsphere/elasticjob-${RELEASE.VERSION}
```

**4. Generate sign files**

```shell
shasum -a 512 apache-shardingsphere-elasticjob-${RELEASE.VERSION}-src.zip >> apache-shardingsphere-elasticjob-${RELEASE.VERSION}-src.zip.sha512
shasum -b -a 512 apache-shardingsphere-elasticjob-${RELEASE.VERSION}-lite-bin.tar.gz >> apache-shardingsphere-elasticjob-${RELEASE.VERSION}-lite-bin.tar.gz.sha512
shasum -b -a 512 apache-shardingsphere-elasticjob-${RELEASE.VERSION}-cloud-executor-bin.tar.gz >> apache-shardingsphere-elasticjob-${RELEASE.VERSION}-cloud-executor-bin.tar.gz.sha512
shasum -b -a 512 apache-shardingsphere-elasticjob-${RELEASE.VERSION}-cloud-scheduler-bin.tar.gz >> apache-shardingsphere-elasticjob-${RELEASE.VERSION}-cloud-scheduler-bin.tar.gz.sha512
```

**5. Commit to Apache SVN**

```shell
svn add *
svn --username=${APACHE LDAP username} commit -m "release elasticjob-${RELEASE.VERSION}"
```

## Check Release

**Check sha512 hash**

```shell
shasum -c apache-shardingsphere-elasticjob-${RELEASE.VERSION}-src.zip.sha512
shasum -c apache-shardingsphere-elasticjob-${RELEASE.VERSION}-lite-bin.tar.gz.sha512
shasum -c apache-shardingsphere-elasticjob-${RELEASE.VERSION}-cloud-executor-bin.tar.gz.sha512
shasum -c apache-shardingsphere-elasticjob-${RELEASE.VERSION}-cloud-scheduler-bin.tar.gz.sha512
```

**Check gpg Signature**

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

```shell
gpg --verify apache-shardingsphere-elasticjob-${RELEASE.VERSION}-src.zip.asc apache-shardingsphere-elasticjob-${RELEASE.VERSION}-src.zip
gpg --verify apache-shardingsphere-elasticjob-${RELEASE.VERSION}-lite-bin.tar.gz.asc apache-shardingsphere-elasticjob-${RELEASE.VERSION}-lite-bin.tar.gz
gpg --verify apache-shardingsphere-elasticjob-${RELEASE.VERSION}-cloud-executor-bin.tar.gz.asc apache-shardingsphere-elasticjob-${RELEASE.VERSION}-cloud-executor-bin.tar.gz
gpg --verify apache-shardingsphere-elasticjob-${RELEASE.VERSION}-cloud-scheduler-bin.tar.gz.asc apache-shardingsphere-elasticjob-${RELEASE.VERSION}-cloud-scheduler-bin.tar.gz
```

**Check Released Files**

**Compare release source with github tag**

```
curl -Lo tag-${RELEASE.VERSION}.zip https://github.com/apache/shardingsphere-elasticjob/archive/${RELEASE.VERSION}.zip
unzip tag-${RELEASE.VERSION}.zip
unzip apache-shardingsphere-elasticjob-${RELEASE.VERSION}-src.zip
diff -r apache-shardingsphere-elasticjob-${RELEASE.VERSION}-src-release shardingsphere-elasticjob-${RELEASE.VERSION}
```

**Check source package**

*   Check whether source tarball is oversized for including nonessential files
*   `LICENSE` and `NOTICE` files exist
*   Correct year in `NOTICE` file
*   There is only text files but no binary files
*   All source files have ASF headers
*   Codes can be compiled and pass the unit tests (./mvnw install)
*   Check if there is any extra files or folders, empty folders for example

**Check binary packages**

Decompress `apache-shardingsphere-elasticjob-${RELEASE.VERSION}-lite-bin.tar.gz`, `apache-shardingsphere-elasticjob-${RELEASE.VERSION}-cloud-executor-bin.tar.gz`
and `apache-shardingsphere-elasticjob-${RELEASE.VERSION}-cloud-scheduler-bin.tar.gz`
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

**Vote procedure**

1. ShardingSphere community vote: send the vote e-mail to `dev@shardingsphere.apache.org`. 
PMC needs to check the rightness of the version according to the document before they vote. 
After at least 72 hours and with at least 3 `+1 PMC member` votes, it can come to the next stage of the vote.

2. Announce the vote result: send the result vote e-mail to `dev@shardingsphere.apache.org`.

**Vote Templates**

1. ShardingSphere Community Vote Template

Title:

```
[VOTE] Release Apache ShardingSphere ElasticJob-${RELEASE.VERSION}

```

Body:

```
Hello ShardingSphere Community,

This is a call for vote to release Apache ShardingSphere ElasticJob-${RELEASE.VERSION}

Release notes:
https://github.com/apache/shardingsphere-elasticjob/blob/master/RELEASE-NOTES.md

The release candidates:
https://dist.apache.org/repos/dist/dev/shardingsphere/elasticjob-${RELEASE.VERSION}/

Maven 2 staging repository:
https://repository.apache.org/content/repositories/${STAGING.REPOSITORY}/org/apache/shardingsphere/

Git tag for the release:
https://github.com/apache/shardingsphere-elasticjob/tree/${RELEASE.VERSION}/

Release Commit ID:
https://github.com/apache/shardingsphere-elasticjob/commit/xxxxxxxxxxxxxxxxxxxxxxx

Keys to verify the Release Candidate:
https://dist.apache.org/repos/dist/dev/shardingsphere/KEYS

Look at here for how to verify this release candidate:
https://shardingsphere.apache.org/community/en/contribute/release-elasticjob/

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
[RESULT][VOTE] Release Apache ShardingSphere ElasticJob-${RELEASE.VERSION}
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

**1. Move source packages, binary packages and KEYS from the `dev` directory to `release` directory**

```shell
svn mv https://dist.apache.org/repos/dist/dev/shardingsphere/elasticjob-${RELEASE.VERSION} https://dist.apache.org/repos/dist/release/shardingsphere/ -m "transfer packages for elasticjob-${RELEASE.VERSION}"
svn delete https://dist.apache.org/repos/dist/release/shardingsphere/KEYS -m "delete KEYS"
svn cp https://dist.apache.org/repos/dist/dev/shardingsphere/KEYS https://dist.apache.org/repos/dist/release/shardingsphere/ -m "transfer KEYS for elasticjob-${RELEASE.VERSION}"
```

**2. Find ShardingSphere in staging repository and click `Release`**

**3. Merge release branch to `master` and delete release branch on Github**

```shell
git checkout master
git merge origin/${RELEASE.VERSION}-release
git pull
git push origin master
git push --delete origin ${RELEASE.VERSION}-release
git branch -d ${RELEASE.VERSION}-release
```

**4. Update the download page**

https://shardingsphere.apache.org/elasticjob/current/en/downloads/

https://shardingsphere.apache.org/elasticjob/current/cn/downloads/

GPG signatures and hashes (SHA* etc) should use URL start with `https://downloads.apache.org/shardingsphere/`

Keep one latest versions in `Latest releases`.

## Docker Release

**1. Preparation**

Install docker locally and start the docker service

**2. Compile Docker Image**

```shell
git checkout ${RELEASE.VERSION}
cd ~/elasticjob/elasticjob-distribution/elasticjob-cloud-scheduler-distribution/
mvn clean package -Prelease,docker
```

**3. Tag the local Docker Image**

Check the image ID through `docker images`, for example: e9ea51023687

```shell
docker tag e9ea51023687 apache/shardingsphere-elasticjob-cloud-scheduler:latest
docker tag e9ea51023687 apache/shardingsphere-elasticjob-cloud-scheduler:${RELEASE.VERSION}
```

**4. Publish Docker Image**

```shell
docker push apache/shardingsphere-elasticjob-cloud-scheduler:latest
docker push apache/shardingsphere-elasticjob-cloud-scheduler:${RELEASE_VERSION}
```

**5. Confirm the successful release**

Login [Docker Hub](https://hub.docker.com/r/apache/shardingsphere-elasticjob-cloud-scheduler/) to check whether there are published images

## Publish release in GitHub

Click `Edit` in [GitHub Releases](https://github.com/apache/shardingsphere-elasticjob/releases)'s `${RELEASE_VERSION}` version

Edit version number and release notes, click `Publish release`

## Send e-mail to `dev@shardingsphere.apache.org` and `announce@apache.org` to announce the release is finished

Announcement e-mail template:

Title:

```
[ANNOUNCE] Apache ShardingSphere ElasticJob-${RELEASE.VERSION} available
```

Body:

```
Hi all,

Apache ShardingSphere Team is glad to announce the new release of Apache ShardingSphere ElasticJob-${RELEASE.VERSION}.

ElasticJob is a distributed scheduling solution consisting of two separate projects, ElasticJob-Lite and ElasticJob-Cloud.
Through the functions of flexible scheduling, resource management and job management, it creates a distributed scheduling solution suitable for Internet scenarios, and provides diversified job ecosystem through open architecture design. It uses a unified job API for each project. Developers only need code one time and can deploy at will.
ElasticJob became an Apache ShardingSphere Sub project on May 28 2020.

Download Links: https://shardingsphere.apache.org/elasticjob/current/en/downloads/

Release Notes: https://github.com/apache/shardingsphere-elasticjob/blob/master/RELEASE-NOTES.md

Website: http://shardingsphere.apache.org/elasticjob/

ShardingSphere Resources:
- Issue: https://github.com/apache/shardingsphere-elasticjob/issues/
- Mailing list: dev@shardingsphere.apache.org
- Documents: https://shardingsphere.apache.org/elasticjob/current/en/overview/



- Apache ShardingSphere Team

```
