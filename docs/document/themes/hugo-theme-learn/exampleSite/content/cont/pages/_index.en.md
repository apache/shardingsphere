---
date: 2016-04-09T16:50:16+02:00
title: Pages organization
weight: 5
---

In **Hugo**, pages are the core of your site. Once it is configured, pages are definitely the added value to your documentation site.

## Folders

Organize your site like [any other Hugo project](https://gohugo.io/content/organization/). Typically, you will have a *content* folder with all your pages.

    content
    ├── level-one 
    │   ├── level-two
    │   │   ├── level-three
    │   │   │   ├── level-four
    │   │   │   │   ├── _index.md       <-- /level-one/level-two/level-three/level-four
    │   │   │   │   ├── page-4-a.md     <-- /level-one/level-two/level-three/level-four/page-4-a
    │   │   │   │   ├── page-4-b.md     <-- /level-one/level-two/level-three/level-four/page-4-b
    │   │   │   │   └── page-4-c.md     <-- /level-one/level-two/level-three/level-four/page-4-c
    │   │   │   ├── _index.md           <-- /level-one/level-two/level-three
    │   │   │   ├── page-3-a.md         <-- /level-one/level-two/level-three/page-3-a
    │   │   │   ├── page-3-b.md         <-- /level-one/level-two/level-three/page-3-b
    │   │   │   └── page-3-c.md         <-- /level-one/level-two/level-three/page-3-c
    │   │   ├── _index.md               <-- /level-one/level-two
    │   │   ├── page-2-a.md             <-- /level-one/level-two/page-2-a
    │   │   ├── page-2-b.md             <-- /level-one/level-two/page-2-b
    │   │   └── page-2-c.md             <-- /level-one/level-two/page-2-c
    │   ├── _index.md                   <-- /level-one
    │   ├── page-1-a.md                 <-- /level-one/page-1-a
    │   ├── page-1-b.md                 <-- /level-one/page-1-b
    │   └── page-1-c.md                 <-- /level-one/page-1-c
    ├── _index.md                       <-- /
    └── page-top.md                     <-- /page-top

{{% notice note %}}
`_index.md` is required in each folder, it’s your “folder home page”
{{% /notice %}}

## Types

**Hugo-theme-learn** defines two types of pages. *Default* and *Chapter*. Both can be used at any level of the documentation, the only difference being layout display.

A **Chapter** displays a page meant to be used as introduction for a set of child pages. Commonly, it contains a simple title and a catch line to define content that can be found under it.
You can define any HTML as prefix for the menu. In the example below, it's just a number but that could be an [icon](https://fortawesome.github.io/Font-Awesome/).

![Chapter page](/cont/pages/images/pages-chapter.png?width=50pc)

```markdown
+++
title = "Basics"
chapter = true
weight = 5
pre = "<b>1. </b>"
+++

### Chapter 1

# Basics

Discover what this Hugo theme is all about and the core-concepts behind it.
```

To tell **Hugo-theme-learn** to consider a page as a chapter, set `chapter=true` in the Front Matter of the page.

A **Default** page is any other content page.

![Default page](/cont/pages/images/pages-default.png?width=50pc)

```toml
+++
title = "Installation"
weight = 15
+++
```

The following steps are here to help you initialize your new website. If you don't know Hugo at all, we strongly suggest you to train by following this [great documentation for beginners](https://gohugo.io/overview/quickstart/).

## Create your project

Hugo provides a `new` command to create a new website.

```
hugo new site <new_project>
```

**Hugo-theme-learn** provides [archetypes]({{< relref "cont/archetypes.fr.md" >}}) to help you create this kind of pages.

## Front Matter configuration

Each Hugo page has to define a [Front Matter](https://gohugo.io/content/front-matter/) in *yaml*, *toml* or *json*.

**Hugo-theme-learn** uses the following parameters on top of Hugo ones :

```toml
+++
# Table of content (toc) is enabled by default. Set this parameter to true to disable it.
# Note: Toc is always disabled for chapter pages
disableToc = "false"
# If set, this will be used for the page's menu entry (instead of the `title` attribute)
menuTitle = ""
# The title of the page in menu will be prefixed by this HTML content
pre = ""
# The title of the page in menu will be postfixed by this HTML content
post = ""
# Set the page as a chapter, changing the way it's displayed
chapter = false
# Hide a menu entry by setting this to true
hidden = false
# Display name of this page modifier. If set, it will be displayed in the footer. 
LastModifierDisplayName = ""
# Email of this page modifier. If set with LastModifierDisplayName, it will be displayed in the footer
LastModifierEmail = ""
+++
```

### Add icon to a menu entry

In the page frontmatter, add a `pre` param to insert any HTML code before the menu label. The example below uses the Github icon.

```toml
+++
title = "Github repo"
pre = "<i class='fa fa-github'></i> "
+++
```

![Title with icon](/cont/pages/images/frontmatter-icon.png)

### Ordering sibling menu/page entries

Hugo provides a [flexible way](https://gohugo.io/content/ordering/) to handle order for your pages.

The simplest way is to set `weight` parameter to a number.

```toml
+++
title = "My page"
weight = 5
+++
```

### Using a custom title for menu entries

By default, **Hugo-theme-learn** will use a page's `title` attribute for the menu item (or `linkTitle` if defined).

But a page's title has to be descriptive on its own while the menu is a hierarchy.  
We've added the `menuTitle` parameter for that purpose:

For example (for a page named `content/install/linux.md`): 

```toml
+++
title = "Install on Linux"
menuTitle = "Linux"
+++
```

## Homepage

To configure your home page, you basically have three choices:

1. Create an `_index.md` document in `content` folder and fill the file with *Markdown content*
2. Create an `index.html` file in the `static` folder and fill the file with *HTML content*
3. Configure your server to automatically redirect home page to one your documentation page
