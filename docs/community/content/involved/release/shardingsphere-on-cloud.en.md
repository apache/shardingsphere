+++
title = "ShardingSphere on Cloud Release Guide"
weight = 2
chapter = true
+++

## Prepare before release

### 1. Confirm release notes

The release note should be provided in English / Chinese, confirm whether English and Chinese description are clear,
and shall be classified according to the following labels:

1. New Feature
1. API Change
1. Enhancement
1. Bug Fix

### 2. Confirm issue list

Open [Github Issues](https://github.com/apache/shardingsphere-on-cloud/issues)，filter the issue whose milestone is `${RELEASE.VERSION}` and status is open:

1. Close the completed issue;
1. For outstanding issues, communicate with the developer in charge. If this release is not affected, modify milestone to the next version;
1. Confirm that there is no issue in open status under milestone of release version.

### 3. Confirm pull request list

Open [Github Pull requests](https://github.com/apache/shardingsphere-on-cloud/pulls), filter pull requests whose milestone is `${RELEASE.VERSION}` and status is open:

1. Review the open pull request and merge;
1. For pull requests that cannot merge and do not affect this release, modify milestone to the next version;
1. Confirm that there is no open pull request under milestone of release version.

### 4. Call for a discussion

1. Send email to [dev@shardingsphere.apache.org](mailto:dev@shardingsphere.apache.org)
1. Follow the mailing list and confirm that the community developers have no questions about the release note.

### 5. Close milestone

Open [GitHub milestone](https://github.com/apache/shardingsphere-on-cloud/milestones)

1. Confirm that the milestone completion status of `${RELEASE.VERSION}` is 100%;
1. Click `close` to close milestone.

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

> To be noticed: Please use Apache mail for key creation.

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

### 4. Export v1 version secret

``` shell
gpg --export >~/.gnupg/pubring.gpg
gpg --export-secret-keys >~/.gnupg/secring.gpg
```

### 5. Upload the Public Key to Key Server

The command is as follows:

```shell
gpg --keyserver hkp://keyserver.ubuntu.com --send-key 700E6065
```

`keyserver.ubuntu.com` is randomly chosen from public key server.
Each server will automatically synchronize with one another, so it would be okay to choose any one.

## Prepare Branch for Release

### 1. Create Release Branch

Suppose ShardingSphere source codes downloaded from github is under `~/shardingsphere-on-cloud/` and the version to be released is `${RELEASE.VERSION}`。
Create `${RELEASE.VERSION}-release` branch, where all the following operations are performed.

```shell
## ${name} is the properly branch, e.g. master, dev-4.x
git clone --branch ${name} https://github.com/apache/shardingsphere-on-cloud.git ~/shardingsphere-on-cloud
cd ~/shardingsphere-on-cloud/
git pull
git checkout -b ${RELEASE.VERSION}-release
git push origin ${RELEASE.VERSION}-release
```
### 2. Update charts version

Update the version in `Chart.yaml` file in release branch:

```
~/shardingsphere-on-cloud/charts/shardingsphere-operator/Chart.yaml
~/shardingsphere-on-cloud/charts/shardingsphere-operator-cluster/Chart.yaml
~/shardingsphere-on-cloud/charts/shardingsphere-proxy/Chart.yaml
```

Modify `version` to `${RELEASE.VERSION}`, `appVersion` to the corresponding application version, and submit a PR to release branch.

### 3. Package charts

```shell
cd ~/shardingsphere-on-cloud/charts
helm package --sign --key ${GPG username} --keyring /.gnupg/secring.gpg  `changed charts`
```

### 4. Update the download page

Update the following pages:
* <https://shardingsphere.apache.org/document/current/en/downloads/>
* <https://shardingsphere.apache.org/document/current/cn/downloads/>

GPG signatures and hashes (SHA* etc) should be prefixed with: `https://downloads.apache.org/shardingsphere/`。

### Apache SVN Repository Release

**1. Checkout ShardingSphere Release Directory**

If there is no local work directory, create one at first.

```shell
mkdir -p ~/ss_svn/dev/
cd ~/ss_svn/dev/
```

After the creation, checkout ShardingSphere release directory from Apache SVN.

```shell
svn --username=${APACHE LDAP username} co https://dist.apache.org/repos/dist/dev/shardingsphere
cd ~/ss_svn/dev/shardingsphere/charts
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
mkdir -p ~/ss_svn/dev/shardingsphere/charts/${RELEASE.VERSION}
cd ~/ss_svn/dev/shardingsphere/charts/${RELEASE.VERSION}
```

Add charts packages of ShardingSphere-On-Cloud to SVN working directory.

```shell
cp -f ~/shardingsphere-on-cloud/charts/*.tgz
~/ss_svn/dev/shardingsphere/charts/${RELEASE.VERSION}
```

**4. Commit to Apache SVN**

```shell
svn add * --parents
svn --username=${APACHE LDAP username} commit -m "release ${RELEASE.VERSION}"
```

### Check Release

**Check sha512 hash**

```shell
shasum -c *.sha512
```

**Check gpg Signature**

First, import releaser's public key. Import KEYS from SVN repository to local. (The releaser does not need to import again; the checking assistant needs to import it, with the user name filled as the releaser's. )

```shell
curl https://dist.apache.org/repos/dist/dev/shardingsphere/KEYS >> KEYS
gpg --import KEYS
gpg --edit-key "${releaser gpg username}"
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
helm verify `changed charts file`
```

**Check Released Files**

**Compare release source with github tag**

```shell
curl -Lo tag-apache-shardingsphere-proxy-${RELEASE.VERSION}.tgz https://github.com/apache/shardingsphere-on-cloud/archive/apache-shardingsphere-proxy-${RELEASE.VERSION}.tgz
diff -r tag-apache-shardingsphere-proxy-${RELEASE.VERSION}.tgz  apache-shardingsphere-proxy-${RELEASE.VERSION}.tgz

curl -Lo tag-shardingsphere-cluster-${RELEASE.VERSION}.tgz https://github.com/apache/shardingsphere-on-cloud/archive/shardingsphere-cluster-${RELEASE.VERSION}.tgz
diff -r tag-shardingsphere-cluster-${RELEASE.VERSION}.tgz  shardingsphere-cluster-${RELEASE.VERSION}.tgz

curl -Lo tag-shardingsphere-operator-${RELEASE.VERSION}.tgz https://github.com/apache/shardingsphere-on-cloud/archive/shardingsphere-operator-${RELEASE.VERSION}.tgz
diff -r tag-shardingsphere-operator-${RELEASE.VERSION}.tgz  shardingsphere-operator-${RELEASE.VERSION}.tgz
```

**Check source package**

*   Check whether source tarball is oversized for including nonessential files
*   `LICENSE` and `NOTICE` files exist
*   Correct year in `NOTICE` file
*   There is only text files but no binary files
*   All source files have ASF headers
*   Codes can be installed with `helm install`
*   Check if there is any extra files or folders, empty folders for example

**3.3 Check Charts packages**

Decompress
- `apache-shardingsphere-proxy-${RELEASE.VERSION}.tgz`
- `shardingsphere-cluster-${RELEASE.VERSION}.tgz`
- `shardingsphere-operator-${RELEASE.VERSION}.tgz`

to check the following items:

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
   After at least 72 hours and with at least 3 `+1 PMC member` votes, it can come to the next stage of the vote.

2. Announce the vote result: send the result vote e-mail to `dev@shardingsphere.apache.org`.

**Vote Templates**

1. ShardingSphere Community Vote Template

Title:

```
[VOTE] Release Apache ShardingSphere ${RELEASE.VERSION}
```

Body:

```
Hello ShardingSphere Community,

This is a call for vote to release Apache ShardingSphere On Cloud version ${RELEASE.VERSION}

Release notes:
https://github.com/apache/shardingsphere-on-cloud/blob/${RELEASE.VERSION}-release/RELEASE-NOTES.md


Git tag for the release:
https://github.com/apache/shardingsphere-on-cloud/tree/${RELEASE.VERSION}/

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
[RESULT][VOTE] Release Apache ShardingSphere ${RELEASE.VERSION}
```

Body：

```
We’ve received 3 +1 binding votes and one +1 non-binding vote:

+1 binding, xxx
+1 binding, xxx
+1 binding, xxx

+1 non-binding, xxx

Thank you everyone for taking the time to review the release and help us. 
I will process to publish the release and send ANNOUNCE.
```

3. Announce release completed by email

Send e-mail to `dev@shardingsphere.apache.org` and `announce@apache.org` to announce the release is finished

Announcement e-mail template:

Title:

```
[ANNOUNCE] Apache ShardingSphere On-Cloud-${RELEASE.VERSION} available
```

Body：

```
Hi all,

Apache ShardingSphere Team is glad to announce the new release of Apache ShardingSphere On-Cloud-${RELEASE.VERSION}.

The shardingsphere-on-cloud project, including ShardingSphere Operator, Helm Charts, and other cloud solutions, aims at enhancing the deployment and management capabilities of Apache ShardingSphere Proxy on the cloud. 
ShardingSphere Operator is a Kubernetes software extension written with the Operator extension pattern of Kubernetes. ShardingSphere Operator can be used to quickly deploy an Apache ShardingSphere Proxy cluster in the Kubernetes environment and manage the entire cluster life cycle.


Release Notes: https://github.com/apache/shardingsphere-on-cloud/blob/master/RELEASE-NOTES.md




- Apache ShardingSphere Team

```
````