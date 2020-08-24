---
date: 2016-04-09T16:50:16+02:00
title: Configuration
weight: 20
---

## Global site parameters

On top of [Hugo global configuration](https://gohugo.io/overview/configuration/), **Hugo-theme-learn** lets you define the following parameters in your `config.toml` (here, values are default).

Note that some of these parameters are explained in details in other sections of this documentation.

```toml
[params]
  # Prefix URL to edit current page. Will display an "Edit this page" button on top right hand corner of every page. 
  # Useful to give opportunity to people to create merge request for your doc.
  # See the config.toml file from this documentation site to have an example.
  editURL = ""
  # Author of the site, will be used in meta information
  author = ""
  # Description of the site, will be used in meta information
  description = ""
  # Shows a checkmark for visited pages on the menu
  showVisitedLinks = false
  # Disable search function. It will hide search bar
  disableSearch = false
  # Javascript and CSS cache are automatically busted when new version of site is generated. 
  # Set this to true to disable this behavior (some proxies don't handle well this optimization)
  disableAssetsBusting = false
  # Set this to true to disable copy-to-clipboard button for inline code.
  disableInlineCopyToClipBoard = false
  # A title for shortcuts in menu is set by default. Set this to true to disable it. 
  disableShortcutsTitle = false
  # When using mulitlingual website, disable the switch language button.
  disableLanguageSwitchingButton = false
  # Order sections in menu by "weight" or "title". Default to "weight"
  ordersectionsby = "weight"
  # Change default color scheme with a variant one. Can be "red", "blue", "green".
  themeVariant = ""
```

## Activate search

If not already present, add the follow lines in the same `config.toml` file.

```toml
[outputs]
home = [ "HTML", "RSS", "JSON"]
```

Learn theme uses the last improvement available in hugo version 20+ to generate a json index file ready to be consumed by lunr.js javascript search engine.

> Hugo generate lunrjs index.json at the root of public folder. 
> When you build the site with `hugo server`, hugo generates it internally and of course it doesn’t show up in the filesystem
