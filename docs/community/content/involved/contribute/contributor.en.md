+++
title = "Contributor Guide"
weight = 1
chapter = true
+++

You can report a bug, submit a new function enhancement suggestion, or submit a pull request directly.

## Submit an Issue

 - Before submitting an issue, please go through a comprehensive search to make sure the problem cannot be solved just by searching.
 - Check the [Issue List](https://github.com/apache/shardingsphere/issues) to make sure the problem is not repeated.
 - [Create](https://github.com/apache/shardingsphere/issues/new/choose) a new issue and choose the type of issue.
 - Define the issue with a clear and descriptive title.
 - Fill in necessary information according to the template.
 - Choose a label after issue created, for example: bug, enhancement, discussion.
 - Please pay attention for your issue, you may need provide more information during discussion.

## Developer Flow

**1. Prepare repository**

Go to [ShardingSphere GitHub Repo]( https://github.com/apache/shardingsphere ) and fork repository to your account.

Clone repository to local machine.

```shell
git clone https://github.com/(your_github_name)/shardingsphere.git
```

Add ShardingSphere remote repository.

```shell
cd shardingsphere
git remote add apache https://github.com/apache/shardingsphere.git
git remote -v
```

Build and install all modules, it'll install modules into Maven local repository cache, and also generate Java class files of parser from ANTLR grammar `.g4` files to prevent from compile error of parser on IDE.

```shell
cd shardingsphere
./mvnw clean install -DskipITs -DskipTests -P-dev,release,all
```

When you pull the latest code from ShardingSphere and create new branch later, you might get similar compile error of parser again, then you could run this command again.

**2. Choose Issue**

 - Please choose the issue to be edited. If it is a new issue discovered or a new function enhancement to offer, please create an issue and set the right label for it.
 - After choosing the relevant issue, please reply with a deadline to indicate that you are working on it.
 - Find a mentor from the [Developer List](/en/team/) and he/she will give you feedback about the design and the implementation of function in time.

**3. Create Branch**

 - Switch to forked master branch, update local branch, then create a new branch.

```shell
git checkout master
git fetch apache
git rebase apache/master
git push origin master # optional
git checkout -b issueNo
```

 **Notice** ：We will merge PR using squash, commit log will be different with upstream if you use old branch.

**4. Coding**

  - Please obey the [Code of Conduct](/en/involved/conduct/code/) during the process of development and finish the check before submitting the pull request.
  - push code to your fork repo.

```shell
git add modified-file-names
git commit -m 'commit log'
git push origin issueNo
```

**5. Submit Pull Request**

 - Send a pull request to the master branch.
 - The mentor will do code review before discussing some details (including the design, the implementation and the performance) with you. The request will be merged into the branch of current development version after the edit is well enough.
 - At last, congratulations on being an official contributor of ShardingSphere

**6. Update Release Note**

 - After coding is completed, please update the [Release Note](https://github.com/apache/shardingsphere/blob/master/RELEASE-NOTES.md) of current development version. According to the different types of issues, add them in the `API Change`, `New Feature`, `Enhancement` or `Bug Fix` categories. `RELEASE-NOTES` needs to follow the unified format: `{feature_name}: {description} - {issue/pr link}`, for example: `SQL Parser: Support PostgreSQL, openGauss function table and update from segment parse - #32994`.

**7. Delete Branch**

 - You can delete the remote branch (origin/issueNo) and the local branch (issueNo) associated with the remote branch (origin/issueNo) after the mentor merged the pull request into the master branch of ShardingSphere.
 
```shell
git checkout master
git branch -d issueNo
git remote prune origin # If you delete branch on GitHub PR page, else you could delete origin branch with following command
git push origin --delete issueNo
```

**Notice**:  Please note that in order to show your id in the contributor list, don't forget the configurations below:

```shell
git config --global user.name "username"
git config --global user.email "username@mail.com"
```
