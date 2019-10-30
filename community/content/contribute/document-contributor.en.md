+++
title = "Documentation Guide"
weight = 5
chapter = true
+++

In the contributor guide, we have mentioned how to submit issue and pull request. Here we will introduce how to submit pull request to official documentation and how to update the website.

## Fork Documentation

Fork master branch of [Documentation](https://github.com/apache/incubator-shardingsphere-doc).

## Structure Describe

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

## Operation steps

1. First, you need to locate the files you want to operate in the master branch directory structure. If you want to make changes, and you use the idea tool, you can use the shortcut key `Ctrl + Shift + F` to search quickly depending on the content before and after.
1. After the file operation, submit the pr to the master branch.
1. After updating the master branch, you need to modify the corresponding asf-site branch, that is website. You need to execute the `build.sh` script, which will  generate the target folder automatically, which is the required file for the website. `build.sh` is the created script, which can be executed directly.
1. Before switching branches, you need to copy all the files in the target directory and delete the target directory under the master branch.
1. You need to switch branches with command `git checkout asf-site`, replacing all the files in the asf-site branch with the files copied in **step 4** (the files in the target directory).
1. Finally, you can submit PR for the asf-site branch.

## Need attention

1. Please use version `0.37.1` of Hugo for compatibility reason.
1. Do not submit extra directories. You can replace and submit folders and files under the root directory of asf-site branch as follows.

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
