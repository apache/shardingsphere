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


## Prepare Branch for Release

### 1. Create Release Branch

Suppose `ShardingSphere on Cloud` source codes downloaded from github is under `~/shardingsphere-on-cloud/` and the version to be released is `${RELEASE.VERSION}`。
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

### 3. Create Release Tag

Create a release tag in release branch and submit a PR to release branch.

```shell
git tag ${RELEASE.VERSION}
git push origin --tags
```

### 4. Package charts

Before packaging charts, you need to download dependent packages through `helm dependency build` command, and then package charts. The specific operation steps are as follows:

```shell
cd ~/shardingsphere-on-cloud/charts/shardingsphere-operator
helm dependency build

cd ~/shardingsphere-on-cloud/charts/shardingsphere-operator-cluster
helm dependency build

cd ~/shardingsphere-on-cloud/charts/shardingsphere-proxy/charts/governance
helm dependency build

cd ~/shardingsphere-on-cloud/charts/shardingsphere-proxy
helm dependency build

cd ~/shardingsphere-on-cloud/charts
helm package shardingsphere-operator
helm package shardingsphere-operator-cluster
helm package shardingsphere-proxy
```

### 5. Update the download page

Update the following pages:
* <https://shardingsphere.apache.org/document/current/en/downloads/>
* <https://shardingsphere.apache.org/document/current/cn/downloads/>


### Check Release

**1. Check Released Files**

Decompress:

- `apache-shardingsphere-operator-charts-${RELEASE.VERSION}.tgz`
- `apache-shardingsphere-operator-cluster-charts-${RELEASE.VERSION}.tgz`
- `apache-shardingsphere-proxy-charts-${RELEASE.VERSION}.tgz`

To check the following items:

* `LICENSE` and `NOTICE` files exist
* Correct year in `NOTICE` file
* All text files have ASF headers
* Check the third party dependency license:
  *   The software has a compatible license
  *   All software licenses mentioned in `LICENSE`
  *   All the third party dependency licenses are under `licenses` folder
  *   If it depends on Apache license and has a `NOTICE` file, that `NOTICE` file need to be added to `NOTICE` file of the release
### 2. Check products

add repo
```shell
helm repo remove apache
helm repo add apache  https://apache.github.io/shardingsphere-on-cloud
helm search repo apache
```

If three products can be queried, the release is successful, and `helm repo add` and `helm search repo` will be verified according to the verification value in index.yaml


```shell
NAME                                              	CHART VERSION	           APP VERSION	DESCRIPTION
apache/apache-shardingsphere-operator-charts     	${RELEASE.VERSION}       	xxx     	A Helm chart for ShardingSphere-Operator
apache/apache-shardingsphere-operator-cluster-...	${RELEASE.VERSION}        	xxx      	A Helm chart for ShardingSphere-Operator-Cluster
apache/apache-shardingsphere-proxy-charts        	${RELEASE.VERSION}        	xxx         A Helm chart for ShardingSphere-Proxy-Cluster
```

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
[VOTE] Release Apache ShardingSphere on Cloud ${RELEASE.VERSION}
```

Body:

```
Hello ShardingSphere Community,

This is a call for vote to release Apache ShardingSphere on Cloud version ${RELEASE.VERSION}

Release notes:
https://github.com/apache/shardingsphere-on-cloud/blob/${RELEASE.VERSION}-release/RELEASE-NOTES.md


Git tag for the release:
https://github.com/apache/shardingsphere-on-cloud/tree/${RELEASE.VERSION}/

Release Commit ID:
https://github.com/apache/shardingsphere/commit/xxxxxxxxxxxxxxxxxxxxxxx


Look at here for how to verify this release candidate:
https://shardingsphere.apache.org/community/en/involved/release/shardingsphere/


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