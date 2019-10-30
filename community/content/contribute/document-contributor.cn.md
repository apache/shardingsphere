+++
title = "官方文档贡献指南"
weight = 5
chapter = true
+++

在贡献者指南里，已经提到如何提交issue与pull request,  这里我们将要介绍如何给官方文档提交pull request, 并更新至在线页面。

## Fork文档项目

Fork [官方文档](https://github.com/apache/incubator-shardingsphere-doc)的master分支。

## 目录结构说明

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

### 操作步骤

1. 首先，你需要在master分支目录结构中定位出你要操作的文件。如果你要进行修改，并且你使用的是idea工具，你可以依赖前后内容使用快捷键 Ctrl + Shift + F进行快速搜索 。
1. 文件操作完成后，并提pr到master分支。
1. 在更新完master分支之后，就要修改对应的asf-site分支，即website。你需要执行build.sh脚本, 将会自动生成target文件夹，即为website所需文件。build.sh为已创建的脚本，直接执行即可。
1. 在切换分支前你需要将target目录里面的文件全部复制下来，并删除master分支下的target目录
1. 你需要使用命令 `git checkout asf-site` 切换分支，将asf-site分支中的文件全部替换为**第4步**所复制的文件(target目录中的文件)。
1. 最后，你可以针对asf-site分支提交pr了。

### 注意点

1. 为了兼容性，请使用Hugo的`0.37.1`版本。
1. 不要提交多余的目录，请参考下列你需要替换与提交的asf-site分支根目录下的文件夹及文件。

   ```
   │  check.html
   │  index.html
   │  index_m.html
   │  index_m_zh.html
   │  index_zh.html
   │
   ├─community
   ├─css
   ├─document
   ├─images
   └─schema
   ```
