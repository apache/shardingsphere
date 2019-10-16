+++
title = "Documents Contributor Guide"
weight = 5
chapter = true
+++

In the contributor guide, we have mentioned how to submit issue and pr. here we will introduce how to submit pr to document and update `asf-site` branch, i.e. website

## Before you start, use the `master` branch

If you are a novice, you can be prepared to rely on as follows

1. download [shardingsphere-doc](https://github.com/apache/incubator-shardingsphere-doc.git):

```
## download the code of shardingsphere-doc
git clone https://github.com/apache/incubator-shardingsphere-doc.git
```

## incubator-shardingsphere-doc module design

#### Project structure

```
incubator-shardingsphere-doc
├─community
│  ├─archetypes
│  ├─content
│  │  ├─company
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

#### Operation steps

1. First, you need to locate the files you want to operate in the master branch directory structure. If you want to make changes, and you use the idea tool, you can use the shortcut key `Ctrl + Shift + F` to search quickly depending on the content before and after.
2. After the file operation, submit the pr to the master branch.
3. After updating the master branch, you need to modify the corresponding asf-site branch, that is website. You need to execute the `build.sh` script, which will  generate the target folder automatically, which is the required file for the website. `build.sh` is the created script, which can be executed directly.
4. Before switching branches, you need to copy all the files in the target directory and delete the target directory under the master branch.
5. You need to switch branches with command `git checkout asf-site`, replacing all the files in the asf-site branch with the files copied in **step 4** (the files in the target directory).
6. Finally, you can submit PR for the asf-site branch.

#### Need attention

1. For compatibility, use **Hugo version 0.37.1**.

2. Do not submit extra directories. You can replace and submit folders and files under the root directory of asf-site branch as follows.

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

   