+++
title = "官方文档贡献指南"
weight = 7
chapter = true
+++

如果您想帮助贡献 ShardingSphere 文档或网站，我们很乐意为您提供帮助！任何人都可以贡献，无论您是刚接触项目还是已经使用 ShardingSphere 很长时间，无论是自我认同的开发人员、最终用户，还是那些无法忍受错别字的人，都可以对文档或者网站进行贡献。

在贡献者指南里，已经提到如何提交 Issues 与 Pull Request，这里我们将要介绍如何给官方文档提交 Pull Request。

## 前置条件

- 熟悉 [官方网站](https://shardingsphere.apache.org/index_zh.html)。
- 熟悉 [GitHub 协同开发流程](https://help.github.com/cn/github/collaborating-with-issues-and-pull-requests)。
- 熟练掌握 [Markdown](https://help.github.com/cn/github/writing-on-github/basic-writing-and-formatting-syntax)。
- 熟悉 [Hugo](https://gohugo.io/)。

## Fork 文档项目

Fork [ShardingSphere](https://github.com/apache/shardingsphere) 的 master 分支。

## 目录结构说明

```
shardingsphere
├─docs
  ├─community
  │  ├─archetypes
  │  ├─content
  │  │  ├─contribute
  │  │  ├─powered-by
  │  │  ├─security
  │  │  └─team
  │  ├─layouts
  │  ├─static
  │  └─themes
  ├─document
  │  ├─archetypes
  │  ├─content
  │  │  ├─concepts
  │  │  │  ├─adaptor
  │  │  │  ├─distsql
  │  │  │  ├─mode
  │  │  │  └─pluggable         
  │  │  ├─dev-manual
  │  │  ├─downloads
  │  │  ├─features
  │  │  │  ├─encrypt
  │  │  │  ├─governance
  │  │  │  ├─readwrite-splitting
  │  │  │  ├─scaling
  │  │  │  ├─shadow
  │  │  │  ├─sharding
  │  │  │  │  ├─concept
  │  │  │  │  ├─principle
  │  │  │  │  └─use-norms
  │  │  │  ├─test-engine
  │  │  │  └─transaction
  │  │  │      ├─concept
  │  │  │      ├─principle
  │  │  │      └─use-norms
  │  │  ├─others
  │  │  │  ├─api-change-history
  │  │  │  └─faq    
  │  │  ├─overview
  │  │  ├─quick-start
  │  │  ├─user-manual
  │  │  │  ├─shardingsphere-jdbc
  │  │  │  │  ├─configuration
  │  │  │  │  └─usage
  │  │  │  ├─shardingsphere-proxy
  │  │  │  ├─shardingsphere-sidecar
  │  │  │  └─shardingsphere-scaling  
  │  ├─i18n
  │  ├─layouts
  │  ├─static
  │  └─themes
```

## 文档基础知识

ShardingSphere 文档使用 Markdown 编写，并使用 Hugo 进行处理生成 html，部署于 [asf-site](https://github.com/apache/shardingsphere-doc/tree/asf-site) 分支，源代码位于 [Github](https://github.com/apache/shardingsphere/tree/master)。

- [官方教程最新版本](https://shardingsphere.apache.org/document/current/cn/overview/) 源存储在 `/document/`
- [社区介绍及贡献](https://shardingsphere.apache.org/community/cn/contribute/) 相关文档源都储存在 `/community/content/`

您可以从 [Github](https://github.com/apache/shardingsphere/issues) 网站上提交问题，编辑内容和查看其他人的更改。

## 页面模板

页面模板位于 themes 中的 `layouts/partials/` 目录中。

## 提出具体可查找的问题

任何拥有 Github 帐户的人都可以针对 ShardingSphere 文档提出问题（错误报告）。如果您发现错误，即使您不知道如何修复它，也应提出问题。

**如何提出问题？**

1. 附加出现问题的文档链接。

2. 详细描述问题。

3. 描述问题对用户造成的困扰。

4. 提出建议修复的方式。

5. 在 [Issues](https://github.com/apache/shardingsphere/issues) 中 `New issue` 提出您的问题。

## 提交更改

**操作步骤如下:**

1. 首先，你需要在 master 分支目录结构中定位出你要操作的文件。
2. 文件操作完成后，提 pull request 到 master 分支。

## 约定

- 非特别说明，请使用 Hugo 的 `0.70.0` 版本。

- asf-site 分支由官方定期更新，您无需向 asf-site 提交 pull request。
