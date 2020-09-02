+++
title = "Contributor Guide"
weight = 2
chapter = true
+++

You can report a bug, submit a new function enhancement suggestion, or submit a pull request directly.

## Submit an Issue

 - Before submitting an issue, please go through a comprehensive search to make sure the problem cannot be solved just by searching.
 - Check the [Issue List](https://github.com/apache/shardingsphere/issues) to make sure the problem is not repeated.
 - [Create](https://github.com/apache/shardingsphere/issues/new/choose) a new issue and choose the type of issue.
 - Define the issue with a clear and descriptive title.
 - Fill in necessary information according to the template.
 - Choose a label after issue created, for example: bug，enhancement，discussion.
 - Please pay attention for your issue, you may need provide more information during discussion.

## Developer Flow

### Fork ShardingSphere repo

 - Fork a `ShardingSphere` repo to your own repo to work, then setting upstream.

```shell
git remote add upstream https://github.com/apache/shardingsphere.git
```

### Choose Issue

 - Please choose the issue to be edited. If it is a new issue discovered or a new function enhancement to offer, please create an issue and set the right label for it.
 - After choosing the relevant issue, please reply with a deadline to indicate that you are working on it.
 - Find a mentor from the [Developer List](/en/contribute/contributor/) and he/she will give you feedback about the design and the implementation of function in time.

### Create Branch 

 - Switch to forked master branch, pull codes from upstream, then create a new branch.

```shell
git checkout master
git pull upstream master
git checkout -b issueNo
```

 **Notice** ：We will merge PR using squash, commit log will be different form upstream if you use old branch.

### Coding

  - Please obey the [Code of Conduct](/en/contribute/code-conduct/) during the process of development and finish the check before submitting the pull request.
  - push code to your fork repo.

```shell
git add modified-file-names
git commit -m 'commit log'
git push origin issueNo
```

### Submit Pull Request

 - Send a pull request to the master branch.
 - The mentor will do code review before discussing some details (including the design, the implementation and the performance) with you. The request will be merged into the branch of current development version after the edit is well enough.
 - At last, congratulate to be an official contributor of ShardingSphere

### Delete Branch

 - You can delete the remote branch (origin/issueNo) and the local branch (issueNo) associated with the remote branch (origin/issueNo) after the mentor merged the pull request into the master branch of ShardingSphere.
 
```shell
git checkout master
git branch -d issueNo
git push origin --delete issueNo
```

### Notice 

Please note that in order to show your id in the contributor list, don't forget the configurations below:

```shell
git config --global user.name "username"
git config --global user.email "username@mail.com"
```
