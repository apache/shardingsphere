+++
title = "Documents Contributor Guide"
weight = 5
chapter = true
+++

If you want to help contribute shardingsphere documents or websites, we are happy to help you! Anyone can contribute, whether you're new to a project or have been using shardingsphere for a long time, whether you're a self identified developer, end-user, or someone who can't stand typos,can contribute to documents or websites.
In the contributor guide, we have mentioned how to submit Issues and pull request. here we will introduce how to submit pull request to document.

## Precondition

- Familiar with [Official website](https://shardingsphere.apache.org)
- Familiar with [Collaborating with issues and pull requests](https://help.github.com/categories/collaborating-with-issues-and-pull-requests/)
- Familiar with [Markdown](https://www.markdownguide.org/getting-started)
- Familiar with [Hugo](https://gohugo.io/)

## Fock document
Fock the master branch of [official documents](https://github.com/apache/incubator-shardingsphere-doc).


## Directory structure description

```
incubator-shardingsphere-doc
├─community
│  ├─archetypes
│  ├─content
│  │  ├─poweredby
│  │  ├─contribute
│  │  ├─team
│  │  └─security
│  ├─layouts
│  ├─static
│  └─themes
├─dist
├─document
│  ├─current
│  │  ├─archetypes
│  │  ├─content
│  │  │  ├─downloads
│  │  │  ├─faq
│  │  │  ├─features
│  │  │  │  ├─orchestration
│  │  │  │  ├─read-write-split
│  │  │  │  ├─sharding
│  │  │  │  │  ├─concept
│  │  │  │  │  ├─other-features
│  │  │  │  │  ├─principle
│  │  │  │  │  └─use-norms
│  │  │  │  ├─spi
│  │  │  │  └─transaction
│  │  │  │      ├─concept
│  │  │  │      ├─function
│  │  │  │      └─principle
│  │  │  ├─manual
│  │  │  │  ├─sharding-jdbc
│  │  │  │  │  ├─configuration
│  │  │  │  │  └─usage
│  │  │  │  ├─sharding-proxy
│  │  │  │  ├─sharding-sidecar
│  │  │  │  └─sharding-ui
│  │  │  ├─overview
│  │  │  └─quick-start
│  │  ├─i18n
│  │  ├─layouts
│  │  ├─static
│  │  └─themes
│  └─legacy   
│      ├─1.x
│      │  └─cn
│      ├─2.x
│      │  ├─cn
│      │  └─en
│      └─3.x
│          ├─community
│          ├─document
│          ├─images
│          └─schema
└─homepage
    ├─css
    ├─images
    └─schema
```

## Document Basics

The ShardingSphere document is written in markdown, processed in Hugo, generated HTML, deployed in [asf-site](https://github.com/apache/incubator-shardingsphere-doc/tree/asf-site)   branch, and the source code is located in [Github](https://github.com/apache/incubator-shardingsphere-doc/tree/master).

- [Official homepage](https://shardingsphere.apache.org) document source is stored in`/homepage/`
- [Official course](https://shardingsphere.apache.org/document/current/en/overview/) source is stored in `/document/`，The [latest version](https://shardingsphere.apache.org/document/current/en/overview/) of the official tutorial document source is stored in `/ document / current /`, and the historical version document source is stored in `/document/legacy/`
- [Community introduction and contribution](https://shardingsphere.apache.org/community/en/contribute/) related document sources are stored in `/community/content/`

You can submit questions, edit content, and view other people's changes from [Github](https://github.com/apache/incubator-shardingsphere-doc/issues).

## Page template

The page template is located in the `layouts/partials/` directory in themes

## Ask specific and searchable questions

Anyone with a GitHub account can ask questions (error reports) about shardingsphere documents. If you find an error, ask questions even if you don't know how to fix it.

### How to ask questions

1. Attach the problem document link.
1. Describe the problem in detail.
1. Describe the problems caused to users.
1. Propose the repair method.
1. In [Issues](https://github.com/apache/incubator-shardingsphere-doc/issues),`New issue ` asks your question.

## Submission changes

### Operation steps

1. Locate the file you want to operate in the master branch directory structure.
1. After the file operation is completed, pull request is raised to the master branch.

## Appointment

- Unless otherwise specified, please use Hugo `0.37.1` version.
- asf-site branches are updated by the official on a regular basis, you do not need to submit pull request to asf-site.
