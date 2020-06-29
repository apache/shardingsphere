+++
title = "Exploration and expansion of the community"
weight = 3
chapter = true
+++

## Exploration and expansion of the Apache ShardingSphere community 

Apache ShardingSphere community was invited to attend the Apache Event-join Apache open source community held by at Tsinghua University on November 9th. As an honored speaker, Liang Zhang, the PPMC, gave a talk named exploring and expanding the Apache ShardingSphere community. The main points of this topic are as follows.

### 01 Why open source?

For individuals, you can gain a better career and find enjoyment.

#### A better career

1. More career opportunities. Industries can establish the cloud platform and provide business services through open-source projects. Besides, by contributing to the open-source community, industries can enhance their technical influence. Since industries can benefit from open source, many of them are hungry for open-source talents. The open-source job is always technique-related and community-based, which relatively makes your schedule listen to yourself.

2. Better capability. By contributing to the open-source community, you can improve not only technical skills but also soft skills like Language skills, social skills, and marketing skills. In addition, you will achieve innovation ability as well,  if you have a chance to create a brand new open-source project.

3. Great personal relationship. You will not only focus on your job, salary, KPI, e.t. Those industry-related activities, such as attending a seminar,  tech sharing, writing blogs, even tech investing, will attract your attention. Besides, you can also communicate and make friends with foreign people worldwide in the international open-source community. As this phrase said, engage your body familiar with job skills; engage your mind closed to the world. The open-source experience will bring you more potential opportunities.

4. Earned public recognition. The effort of open-source work you did is totally open to anyone at any time, which means you can gradually receive public recognition. In contrast, the effort of in-company work is supposed to be judged by your leaders themselves, not the public, which means an inadequate evaluation is likely to give.

#### An enjoyable career

1. Self-growth. If you enjoy doing one work, you will grow up with pleasure. As a software engineer, you should learn to enjoy your work and be a self-driven person, which is the real shortcut to success.

2. Accomplishment sense. When your work is recognized by others, when your work made the project better and better, when you got new skills, you will strongly feel the sense of accomplishment and the pleasure of your career.

### 02 What is the Apache software foundation?

The Apache Software Foundation is a non-profit organization, many of whose projects have played essential roles in the internet industry since 1999.  Besides, those Apache projects have been the first choice for developers to create their projects. Therefore, if you knew little about Apache projects, such as Apache Tomcat, Apache Commons, Apache Maven, Apache Hadoop, etc., you could not receive a better offer as a Java Developer or a Big Data engineer.

Recently, more and more Chinese projects entered the Apache foundation. There are 9 Top-Level projects and 10 incubator projects from China until now, which is a piece of good news for those Chinese newcomers since it is challenging for them to involve a wholly foreign open source community at the beginning. Whether it is a Chinese project, it must be under the Apache regulations and follow its policy. However, it is still more accessible for people to start with an Apache project coming from their country. The reason is that they have more chances to communicate with each other in their first language so as to dive into an Apache community quickly. 

### 03 What is Apache ShardingSphere?

Apache ShardingSphere (TLP) is an open-source ecosystem consisted of a set of distributed database middleware solutions. From the GitHub data, its community is active and growing up fast, which benefits from its stable micro-core and scalable architecture.

### 04 Apache ShardingSphere Community Statistics

Since the community is as important as the project itself for an open-source project, ShardingSphere takes more effort to build its community. Thanks to everyone's contribution, the community has fast growth, and the following statistics give us clear evidence. 

1. Mail list
Looking at the data from dev@, we found 38 emails about 20 topics from 14 participants in May 2019, but with four months of effort, the data rose to 148 emails concerning 34 issues from 37 participants. Besides, we moved the GitBox emails to notification@ to avoid including notification emails and display the pure email data.

2. GitHub 
The commits graph of GitHub was almost effected by my commits before June 2018, which means its development only depends on one person and is full of the risk. To turn this situation around, we did a lot of works. Finally, we got 33 contributors, 100 merged PRs, 167 handled issues, 591 modified files, and 4K new stars, which marks it has become an active and open community.

3. Contributors
There were only 11 initial committers during an extended period. This situation was turned around until two or three months ago. And presently, we have 15 PMCs, 10 committers, and 90+ contributors. However, we are still ongoing, so we believe more and more PMCs, committers and contributors will dive into our community in the future.

### 05 The core ideas of the Apache ShardingSphere community

1. Community is over code.  This is one of the essential principles of Apache Software Foundation, and exactly what we believe. Although a project itself is necessary, the more important point is to build an active and open community, only by which this project can evolve and develop continuously. 

2. Believe contributors. Anyone willing to join an open-source community is generally full of enthusiasm and wants to make the project better through their contribution. Therefore, the community is supposed to respect and welcome them and provide the needed help. However, one point needed note is that committers and contributors are different. We would like to grant more privileges to a committer and trust them more since they have become familiar with this project and built faith with others.

3. Automated Testing. More contributors, more Pull requests. In this situation, it is hard to guarantee the effectiveness and efficiency of the project by manual. Therefore, we decide to rely on automated testing to evaluate each Pull request, which helps contributors and reviewers merely focus on their work.

4. Self-service first. More questions get increased with more and more users. How to handle these questions efficiently? Our solution is to build user self-service channels firstly and provide other communication ways. In general, users can solve some of the common issues with documents and FAQ pages by themselves. If that does not work, users can raize their special issues on GitHub or discuss them by email. 

5. Public and online working mode. Public work means the issue only discussed in public is valid—otherwise, invalid and no chance to be solved. Online work means most of the discussions are online and asynchronous, requiring participants to provide information as detailed as possible.

The Apache Software Foundation provides a community maturity assessment model from the seven aspects of code, copyright, release, quality, community. unanimous resolution and product independence. At present, Apache ShardingSphere has completed the evaluation and passed the evaluation of all 34 sub-projects.

### 06 Increase the activity of the Apache ShardingSphere community

1. Project structure adjustment. Apache ShardingSphere is composed of database protocol layer, SQL layer, distributed transaction layer and storage layer at the technical structure level. At the functional level it is composed of many functions such as sharding, high availability governance, data desensitization, and read-write separation. The current ShardingSphere architecture is being adjusted to a completely SPI-based architecture. Extending any technology or implementing any function will not affect other functions. For example, Apache ShardingSphere's support for SQL will be completely separated according to the dialect of the database. The changes of the parsing part of MySQL have no impact on other databases. Therefore, ShardingSphere can support many contributors to contribute code at the same time without conflicting with each other.

2. Project guide. Apache ShardingSphere provides a lot of guides for contributors, allowing potential contributors to learn how to participate in the Apache ShardingSphere community. These guides include email subscription guide, contributor guide, submitter guide, publishing guide, and documentation guide, which basically cover all the elements of participating in the community. The community is also working on the contribution guide related to the technical module. The current testing framework and SQL parsing guidelines are in progress.

3. Specification. Great importance is attached to standardized code. Therefore, the code specification is also an important part of the Apache ShardingSphere project. While the community provides code specification documents, it also provides code inspection tools such as checkstyle. Codes that do not meet the specifications cannot be passed by continuous integration tools. Therefore, they cannot be merged into the development backbone.

4. Demarcation of demand boundaries. Apache ShardingSphere itself is an active community as well as JD's basic database middleware that supports a lot of online applications. Therefore, the scenarios of the project are split into company and community. In the company scenario, the function will strive to maintain stability, have a higher priority and have a deadline for project submission. In the community scenario, we will maintain a continuous open mindset. The priority is moderate. As a team of JD.com’s Apache ShardingSphere, we are also a part of the community. While participating in the community, we will use the stable version of Apache ShardingSphere within the company.

5. Diverse communication channels. In addition to email and GitHub issues, the community also provides WeChat groups for communication. In particular, email and GitHub are always the basis for core developers to deal with problems. WeChat communication is inconvenient to archive and secondary query, so it is only used for further communication. If the user does not create a problem through email or GitHub, the core developer will not directly feedback through WeChat.

There are many ways to contribute to the community, including but not limited to sharing (speech, articles), answering questions, participating in discussions, investigating issues, website design, document updates, article document translation, code submission, community operations and promotion. Code submission is just one of many ways to contribute. Warmly welcome students who are willing to participate in the community to contribute to the community in a diversified manner and increase the feelings of self-worth.

May the Apache ShardingSphere community be one of your candidates for participation.
