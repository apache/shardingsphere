---
title : Children
description : List the child pages of a page
---

Use the children shortcode to list the child pages of a page and the further descendants (children's children). By default, the shortcode displays links to the child pages.

## Usage

| Parameter | Default | Description |
|:--|:--|:--|
| page | _current_ | Specify the page name (section name) to display children for |
| style | "li" | Choose the style used to display descendants. It could be any HTML tag name |
| showhidden | "false" | When true, child pages hidden from the menu will be displayed |
| description  | "false" | Allows you to include a short text under each page in the list.<br/>when no description exists for the page, children shortcode takes the first 70 words of your content. [read more info about summaries on gohugo.io](https://gohugo.io/content/summaries/)  |
| depth | 1 | Enter a number to specify the depth of descendants to display. For example, if the value is 2, the shortcode will display 2 levels of child pages. <br/> **Tips:** set 999 to get all descendants|
| sort | none | Sort Children By<br><li><strong>Weight</strong> - to sort on menu order</li><li><strong>Name</strong> - to sort alphabetically on menu label</li><li><strong>Identifier</strong> - to sort alphabetically on identifier set in frontmatter</li><li><strong>URL</strong> - URL</li> |

## Demo

	{{%/* children  */%}}

{{% children %}}

	{{%/* children description="true"   */%}}

{{%children description="true"   %}}

	{{%/* children depth="3" showhidden="true" */%}}

{{% children depth="3" showhidden="true" %}}

	{{%/* children style="h2" depth="3" description="true" */%}}

{{% children style="h2" depth="3" description="true" %}}

	{{%/* children style="div" depth="999" */%}}

{{% children style="div" depth="999" %}}






