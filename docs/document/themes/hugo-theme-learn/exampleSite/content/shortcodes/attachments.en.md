---
title: Attachments
description : "The Attachments shortcode displays a list of files attached to a page."
---

The Attachments shortcode displays a list of files attached to a page.

{{% attachments /%}}

## Usage

The shortcurt lists files found in a **specific folder**.
Currently, it support two implementations for pages

1. If your page is a markdown file, attachements must be placed in a **folder** named like your page and ending with **.files**.

    > * content
    >   * _index.md
    >   * page.files
    >      * attachment.pdf
    >   * page.md

2. If your page is a **folder**, attachements must be placed in a nested **'files'** folder.

    > * content
    >   * _index.md
    >   * page
    >      * index.md
    >      * files
    >          * attachment.pdf

Be aware that if you use a multilingual website, you will need to have as many folders as languages.

That's all!

### Parameters

| Parameter | Default | Description |
|:--|:--|:--|
| title | "Attachments" | List's title  |
| style | "" | Choose between "orange", "grey", "blue" and "green" for nice style |
| pattern | ".*" | A regular expression, used to filter the attachments by file name. The **pattern** parameter value must be a [regular expression](https://en.wikipedia.org/wiki/Regular_expression). |

For example:

* To match a file suffix of '.jpg', use `.*\.jpg$` (not `*.jpg`).
* To match file names ending in '.jpg' or '.png', use `.*\.(jpg|png)$`.

### Examples

#### List of attachments ending in pdf or mp4


    {{%/*attachments title="Related files" pattern=".*\.(pdf|mp4)$"/*/%}}

renders as

{{%attachments title="Related files" pattern=".*\.(pdf|mp4)$"/%}}

#### Colored styled box

    {{%/*attachments style="orange" /*/%}}

renders as

{{% attachments style="orange" /%}}


    {{%/*attachments style="grey" /*/%}}

renders as 

{{% attachments style="grey" /%}}

    {{%/*attachments style="blue" /*/%}}

renders as

{{% attachments style="blue" /%}}
    
    {{%/*attachments style="green" /*/%}}

renders as

{{% attachments style="green" /%}}
