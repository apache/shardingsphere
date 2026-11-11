+++
title = "Documents Contribute Guide"
weight = 3
chapter = true
+++

If you want to help contribute shardingsphere documents or websites, we are happy to help you! Anyone can contribute, whether you're new to a project or have been using shardingsphere for a long time, whether you're a self identified developer, end-user, or someone who can't stand typos,can contribute to documents or websites.
In the contributor guide, we have mentioned how to submit Issues and pull request. here we will introduce how to submit pull request to document.

## Precondition

- Familiar with [Official website](https://shardingsphere.apache.org).
- Familiar with [Collaborating with issues and pull requests](https://docs.github.com/en/pull-requests).
- Familiar with [Markdown](https://www.markdownguide.org/getting-started).
- Familiar with [Hugo](https://gohugo.io/).

## Fork document
Fork the master branch of [ShardingSphere](https://github.com/apache/shardingsphere).


## Directory structure description

```
docs
├─document
│  ├─content
│  └─static
├─community
│  ├─content
│  └─static
└─blog
   ├─content
   └─static
```

## Document Basics

ShardingSphere documentation is written in Markdown and generated with Hugo.
The Markdown sources and article media are maintained in the [docs directory](https://github.com/apache/shardingsphere/tree/master/docs), and the generated website is deployed to the [asf-site](https://github.com/apache/shardingsphere-doc/tree/asf-site) branch of `shardingsphere-doc`.

- [Latest version Official course](https://shardingsphere.apache.org/document/current/en/overview/) source is stored in `docs/document/content/`.
- [Community introduction and contribution](https://shardingsphere.apache.org/community/en/involved/) related document sources are stored in `docs/community/content/`.
- Blog article sources are stored in `docs/blog/content/`.

Article media is stored in each site's `static` directory.

You can submit questions, edit content, and view other people's changes from [Github](https://github.com/apache/shardingsphere/issues).

## Page template

Website templates and build configuration are maintained in [shardingsphere-doc](https://github.com/apache/shardingsphere-doc).

## Ask specific and searchable questions

Anyone with a GitHub account can ask questions (error reports) about shardingsphere documents. If you find an error, ask questions even if you don't know how to fix it.

**How to ask questions?**

1. Attach the problem document link.
2. Describe the problem in detail.
3. Describe the problems caused to users.
4. Propose the repair method.
5. In [Issues](https://github.com/apache/shardingsphere/issues),`New issue ` asks your question.

## Submission changes

**Operation steps:**

1. Locate the file you want to operate in the master branch directory structure.
2. After the file operation is completed, pull request is raised to the master branch.

## Appointment

- Use the Hugo version specified in the [website build workflow](https://github.com/apache/shardingsphere-doc/blob/asf-site/.github/workflows/build-website.yml) in `shardingsphere-doc`.
- asf-site branches are updated by the official on a regular basis, you do not need to submit pull request to asf-site.
