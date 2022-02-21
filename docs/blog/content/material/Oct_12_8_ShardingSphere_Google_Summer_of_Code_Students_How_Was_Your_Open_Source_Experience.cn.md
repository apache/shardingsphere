+++
title = "ShardingSphere X Google 编程之夏：同学，开源你怎么看？"
weight = 24
chapter = true
+++

![](https://shardingsphere.apache.org/blog/img/Blog_24_img_1_ShardingSphere_GSoC.png)

Apache ShardingSphere 社区有幸参与 Google 编程之夏（Google Summer of Code, 以下简称 GSoC)，指导年轻一代参与开源程序代码设计。GSoC 是 Google 公司主办的年度开源程序设计项目，第一届从 2005 年开始，主要目的是鼓励世界各地的学生参与开放源代码的程序设计。而 Apache ShardingSphere 项目也被选中成为 GSoC 一部分，为学生带来开源软件开发的有趣体验。

**Thanoshan** 和 **Liangda**两位学生在上一届的 GSoC 中积极与 Apache ShardingSphere 导师工作，接受指导，在 GSoC 结束后依旧在 ShardingSphere 项目中积极贡献。
他们欣然接受了我们的采访，分享自己的 GSoC 项目申请经验，谈自己对 Apache ShardingSphere 项目和开源社区的看法，以及个人的未来计划。

						<个人简介>

![](https://shardingsphere.apache.org/blog/img/Blog_24_img_3_Thanoshan_Photo.png)

**Thanoshan**

国家：斯里兰卡
院校：斯里兰卡萨巴拉加穆瓦大学
专业：计算机与信息系统

![](https://shardingsphere.apache.org/blog/img/Blog_24_img_4_Liangda_Photo.png)

**Liangda**

国家：德国
院校：曼海姆大学
专业：商业信息学

## Q&A

**Q1:**
你们是如何喜欢上软件开发的呢？

**Thanoshan：**我的专业学位和软件开发有关，不过我个人在上大学之前就对软件开发就非常感兴趣。

**Liangda：**应该是受到我所学专业的影响。高中时，我对商业和计算机科学都非常感兴趣，所以我选择了商业信息学专业，这个专业把这两个领域结合在一起了，上大学后，我开始了自己的编程之旅，接触到了软件开发，学习了 Java，C++，Python，还有软件设计模式等知识。

**Q2:**
为什么选择申请 GSoC？

**Thanoshan：**在斯里兰卡，我的很多学长学姐都已经参加过了 GSoC，他们建议我去了解一下这个项目机会，积极安利我一定要试试。然后，我搜索了很多这个项目的相关信息，去了解这个项目，并明确如何能够申请这个项目。

**Liangda：**我第一次知道 GSoC 是在去年夏天，我无意间在网络上找到一篇分享 GSoC 经验和申请的博文，那时我就对这个项目非常着迷。不过当时开源对我来说还是十分陌生的概念，我就望而却步，没有信心申请。之后在 2020 年的寒假，我有机会在学校的一个聊天机器人开发项目中担任助研，这个项目使用了 Rasa，Rasa 是一套开源机器学习框架。这也是我第一次深入了解开源框架，接触开源社区。这是一次非常棒的经历，所以我一下子就想到了 GSoC，我对自己说“为什么不试试 GSoC，继续去探索开源呢？”。

**Q3:**
你觉得 GSoC 有何独到之处？

**Liangda：**我认为 GSoC 很棒，因为它为学生提供了深潜开源社区的机会，去学习尖端的技术。现在 GSoC 项目有近 200 家开源社区参与，每一个社区都会提供一些项目任务，这些任务需要不同的 Stack 和技术，难度也从新手入门级别到专业难度级别，所以我觉得每个学生如果能花一些时间精力仔细研究的话，都能从中找到适合自己的项目。另一方面，GSoC 之所以特别是因为这个项目能够为开源社区带来新鲜血液。因此，GSoC 对学生和开源社区而言都是非常有益的。

**Q4:**
你在 GSoC 加入的是哪一个项目?

**Thanoshan：**我参加的是 ShardingSphere ANTLR 项目。我对 Java 相关开发技术真的非常感兴趣，Java 是我最喜欢的编程语言。我也对开源软件开发非常有兴趣，所以我在选择 GSoC 项目时，关注了 Java 和开源这两点。我想要向其他人请教学习，开源能够让我与他人合作。选择 ShardingSphere 的真正原因就在于它是Java开发的，这就是我参与这个项目的主要动机。

**Liangda：**我在 GSoC 项目中加入了 Apache ShardingSphere Parser 引擎的任务，这个 Parser 的设计初衷就是去处理不同数据库采用不同的 SQL，比如 MySQL，PostgreSQL，Oracle 这类的。我的任务主要是关注校对数据定义语言和事务控制语言的 Oracle SQL 定义。


**Q5:**
那你是如何开始在 ShardingSphere 项目中参与贡献的呢？有什么特别的故事吗？

**Liangda：**的确有个有趣的故事。我发现 ShardingSphere 这个项目需要学生掌握 Java，Database SQL 还有 Antlr 的知识，我感觉自己终于找到与自己知识技能完美匹配的项目了。但是当我去查看记录项目细节的 Jira 页面时，却发现 Thanoshan 从一月份就开始贡献了，那时他已经提交了很多 pull request, 进展很快。我感觉自己应该没有机会了，所以我选择放弃，继续去其他项目。但是到三月份，我打开 Apache 基金会的项目列表，出乎意料地发现 Apache ShardingSphere 又新增了一些项目任务，而且那时还没有学生认领！所以，我意识到我的机会来了，我必须抓住这个机会，于是我就开始处理一些基础的 issues。

**Q6:**
那你们怎么评价 ShardingSphere 社区的工作体验？一开始加入的时候感觉有难度吗？

**Thanoshan：**实际上，在刚开始加入 ShardingSphere 项目的时候，我还不熟悉 ANTLR，也没有用过 ANTLR，但是我的导师 Trista 写了一个帮助学生入门的 issue，写得很棒。她列出一系列需要完成的任务以及学习步骤，所以我就按照导师建议的步骤去练习，最后我学会了 ANTLR，就感觉没有那么困难了。所以我觉得这个项目一开始的难度对我来说是中等的，但是感谢我的导师提供的帮助和支持，这个项目就不那么难了。我从导师的指导中学到了很多，我的每一位导师都帮助我更好地完成任务，让我受益良多。我有三位导师，他们都非常和蔼可亲，愿意帮助我，我真的从他们身上学到很多。

**Liangda：**刚开始的时候，我感觉非常难，因为我没有非常强的计算机背景，这也是我第一次参与这么大型的项目，光是初始安装就花了几个小时，而且在最初的几个 pull request 上，我也遇到挺多困难的。但是 Apache ShardingSphere 是一个非常有爱的社区，大家回答了我的每一个问题，我也得到社区成员的很多帮助。这个社区让我觉得非常温暖，这也是我决定贡献 Apache ShardingSphere 的其中一个原因。

**Q7:**
你觉得成为 GSoC student 是一种什么体验？

**Liangda：**是非常棒的体验。GSoC 项目组织有序，时间线和日程安排清晰。从中我能够学到很多关于开源软件开发的内容，提高知识和技能。还有一点，项目结束阶段的 GSoC 学生峰会也很有趣，让我大开眼界。

**Q8:**
你在 ShardingSphere 项目中为了加入 GSoC 有付出额外的努力吗？

**Liangda：**我觉得自己参加的意愿非常强烈，而且我能够深入思考所面临的难题并找到问题根源。如果我无法独立解决某个 issue，我会勇敢的在 GitHub 上提问，所以我上手非常快。在对项目熟悉之后，我积极回答问题，查看其他成员的 pull request，帮助解决一些我力所能及的问题。此外，我在写项目 proposal 时，我定期会和导师联系，寻求导师的反馈。我想说的是，清楚导师的预期想法是非常重要的。

**Q9:**
你有什么话或是建议想对计划在 GSoC 参与 ShardingSphere 项目的同学说呢？

**Thanoshan：**我想说的是，其实导师和 ShardingSphere 社区会指定一些任务，所以如果想参与的话，应该在申请 GSoC 之前就加入社区。你们可以在项目正式开始前，就在 ShardingSphere 社区完成一些和 GSoC 主项目有关的基础项目，这绝对会帮助你在选拔过程中取得竞争优势。我想强调的就是，尽可能早地加入 ShardingSphere 社区吧。

**Liangda：**非常建议大家尝试一下 ShardingSphere 项目，绝对会有非常好的体验。此外，社区有些 issues 上会有“good first issue”或者“volunteer wanted”的标签，建议尽量先去尝试这些 issue。入门最简单的方式就是实操，如果你觉得毫无头绪的话，勇敢地去提问，表达自己的想法，我们都会在社区里支持你们的。


**Q10:**
对 ShardingSphere 有什么建议吗？

**Thanoshan：**ShardingSphere 是一个大项目，我觉得加分项可能是它的文档。我真的很喜欢和 ShardingSphere 社区合作，我也能够在社区文档的帮助下很方便地理解一些概念。我看到已经有社区的 contributor 在提交 pull request 继续提升文档的质量。此外，我觉得 ShardingSphere 目前新增了一些安装指导，新的 contributor 就可以直接去读这些文档，明白他们应该怎么做，我觉得这一改变也很好，我也非常开心 ShardingSphere 能这么做。

**Liangda：**我觉得暑期和 ShardingShpere 社区的互动体验非常好。不过，我希望能够有机会和导师视频电话，这样会方便学生和导师的相互沟通，促进彼此的了解。


**Q11:**
热爱成就精彩人生。可以请问你们对什么事情有特别热衷吗？

**Thanoshan：**我非常热衷开源。我在斯里兰卡的一家支持开源项目的公司 99X 里参与了一些开源项目，至少五个吧，我会做一些简单的贡献，比如说写 read-me 文档，写贡献者指南，去修改文档问题，或者修复一些漏洞之类的。这是我接触开源的开始，之后，我真的对开源越来越感兴趣。

**Liangda：**我非常喜欢运营社区。我是曼海姆大学的中国留学生及学者协会的主席，我们协会致力于帮助中国留学生的学习和就业，我们也会促进中德社会文化的交流。此外，在参与了 GSoC 之后，现在我也爱上了开源社区。我觉得开源社区真的很棒，因为在开源社区里面，来自世界各地的成员一起工作努力去完善一个项目，能够彼此支持。

**Q12:**
有什么关于 ShardingSphere 社区的问题想提问的吗？

**Thanoshan：**只有一个问题，能否给我一些小贴士，关于如何在 ShardingSphere 社区更积极切实地做贡献呢？

**主持人：**当然可以。贡献其实有很多种方式。首先，你可以帮忙处理社区的 issue。其次，你提到了的文档，帮助改进文档也是贡献的方式之一。我注意到你非常擅长写作，我读过你发布在 Medium 上的文章，如果你愿意的话，也可以帮助社区修改文档。此外，如果你的发布文章浏览量很多的，也会帮助到社区的运营。贡献真的有很多方式，有一部分是编程，当然也有和编程无关的贡献。我们社区对大家做出的任何贡献都非常感激。我不太确定，你是否有计划未来成为 Apache 基金会的committer，如果有的话，就会有一个投票的环节，投票时社区会把你在编程之外的贡献也考虑在内。

**Thanoshan：**成为 committer 也在我的目标之中。毕业后，我绝对会尝试成为一名 committer。

**主持人：**实际上，我最近也和 ShardingSphere PMC Chair 讨论了这个话题，他和我说，ShardingSphere 社区欢迎成员做出编程的贡献，即使你们还在学习中，只要表现出愿意贡献的想法，能够和社区共同学习成长，ShardingSphere 社区绝对是非常欢迎你们的。如果你想成为 committer 的话，你需要参与一个投票的过程，当然如果你在 ShardingSphere 项目上合作愉快的，你一定能实现你的目标的。

### 结语

**Thanoshan** 和 **Liangda** 分享了很多自己在 GSoC 以及 ShardingSphere 社区的经验。再次非常感谢他们。他们反馈在 GSoC 和 ShardingSphere 项目的体验很棒，也认可 ShardingSphere 社区的帮助，他们在导师的支持下参与贡献了大型项目，他们一再强调的一点就是：“不要畏惧尝试！”

Apache ShardingSphere 社区欢迎大家的加入，无论你会不会编程，或者你还只是个初学者，我们都欢迎你的加入，让我们一起助力 ShardingSphere 社区发展！

如果你对 Apache ShardingSphere 感兴趣，欢迎点击下方链接联系我们，或查看我们的 GitHub 社区。

**ShardingSphere Github：**https://github.com/apache/shardingsphere

**ShardingSphere Twitter：**https://twitter.com/ShardingSphere

**ShardingSphere Slack Channel：**apacheshardingsphere.slack.com
