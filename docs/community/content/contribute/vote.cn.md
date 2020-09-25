+++ 
title = "接收新的提交者的流程指南" 
weight = 12 
chapter = true 
+++

## 接收新的提交者流程如下：

**1. 在 @private 邮件列表讨论并投票，投票由现有的PMC成员发起，邮件内容主要陈述提名者的活动和社区贡献**

```
例如：

To：private@shardingsphere.apache.org

Title： [VOTE] New committer: （提名者）

Content：

Hi, everyone

This is ^a formal vote^ about inviting （提名者）as our new committer. 
He/She really made an effort to improve ShardingSphere Parser and fix many issues. 
The following links will direct you to his/her work.

https://github.com/apache/shardingsphere/pull/6319
https://github.com/apache/shardingsphere/pull/6293
https://github.com/apache/shardingsphere/pull/6259
https://github.com/apache/shardingsphere/pull/6211

[^ means these words are subject to change in different case]
```
**2. 等待3*24h社区Committer投票**

**3. PMC成员总结社区投票结果并发布邮件**

```
例如：

To: private@shardingsphere.apache.org

Title: [RESULT] [VOTE] New committer: （提名者）

Content:

Hi all PMCs, 

I am glad to receive your votes, and the voting result is,
6   +1 votes, 0   +/-1 votes, 0   -1 votes

Therefore, I will send the invitation to （提名者）.
```

**4. 如果投票通过，对提名者发布邀请函并抄送@private 邮件列表给发送邀请函**

```
To: 提名者邮箱

cc: private@shardingsphere.apache.org

Title： Invitation to become ShardingSphere committer: （提名者）

Content:

Hello （提名者）,

The ShardingSphere Project Management Committee (PMC) hereby offers you committer privileges to the project. These privileges are offered on the understanding that you'll use them reasonably and with common sense. We like to work on trust
rather than unnecessary constraints.
Being a committer enables you to more easily make changes without needing to go through the patch submission process.
Being a committer does not require you to participate any more than you already do. It does tend to make one even more committed.  You will probably find that you spend more time here.
Of course, you can decline and instead remain as a contributor, participating as you do now.
A. This personal invitation is a chance for you to accept or decline in private.  Either way, please let us know in reply to the private@shardingsphere.apache.org 
address only.
B. If you accept, the next step is to register an iCLA:
    1. Details of the iCLA and the forms are found through this link: http://www.apache.org/licenses/#clas

    2. Instructions for its completion and return to the Secretary of the ASF are found at http://www.apache.org/licenses/#submitting

    3. When you transmit the completed iCLA, request to notify the Apache ShardingSphere and choose a unique Apache id. Look to see if your preferred id is already taken at http://people.apache.org/committer-index.html        
This will allow the Secretary to notify the PMC when your iCLA has been recorded.
When recording of your iCLA is noticed, you will receive a follow-up message with the next steps for establishing you as a committer.

Best wishes,
(PMC)

```
**5. 准Committer回复邀请函内容，回复表达同意与感谢**

**6. PMC做出回复**
```
Hi (提名者),

Welcome! Here are the next steps. After that we will make an announcement to the shardingsphere-dev list.

You need to send a Contributor License Agreement to the ASF. Normally you would send an Individual CLA. If you also make contributions done in work time or using work resources then see the Corporate CLA. Ask us if you have any issues. 
http://www.apache.org/licenses/#clas

You need to choose a preferred ASF user name and alternatives. In order to ensure it is available you can view a list of taken ids at
http://people.apache.org/committer-inde(.ht
Please notify us when you have submitted the CLA and by what means you did so. This will enable us to monitor its progress.

We will arrange for your Apache user account when the CLA has been recorded.

After that is done, please make follow-up replies to the shardingsphere-dev list. We generally discuss everything there and keep the private@shardingsphere.apache.org list for occasional matters which must be private.

The developer section of the website describes the roles and provides other resources:
http://www.apache.org/foundation/how-it-works.html
http://www.apache.org/dev/

The incubator also has some useful information for new committers in incubating projects:
http://incubator.apache.org/guides/committer.html

Just as before you became a committer, participation in any ASF community requires adherence to the ASF Code of Conduct:
https://www.apache.org/foundation/policies/conduct.html

Here is the guideline for all of the ShardingSphere committers:
https://shardingsphere.apache.org/community/en/contribute/committer/
```
**7. 准Committer签署iCLA 具体步骤参考[签署iCLA指南](https://shardingsphere.apache.org/community/cn/contribute/icla/)**

**8. 等待Secretary通知创建账户**

**9. PMC添加新Committer到[roster](https://whimsy.apache.org/roster/committee/shardingsphere)**

**10. 新Committer开通github权限，完成[Setup](https://gitbox.apache.org/setup/)内容**

**11. PMC在社区宣布并欢迎新的Committer加入**

```
To: dev@shardingsphere.apache.org

Title: [ANNOUNCE] New committer: （提名者）

Content:

Hi community,

The Project Management Committee (PMC) for Apache ShardingSphere
has invited （提名者）to become a committer and we are pleased 
to announce that he has accepted.

（提名者） is active in ShardingSphere community, hope see your further interactions with the community! 

Thanks for your contributions.
```

**12. 新Commiter更新[提交者名单](/cn/team/)**

