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
  # If set to false, a Home button will appear below the search bar on the menu.
  # It is redirecting to the landing page of the current language if specified. (Default is "/")
  disableLandingPageButton = true
  # When using mulitlingual website, disable the switch language button.
  disableLanguageSwitchingButton = false
  # Hide breadcrumbs in the header and only show the current page title
  disableBreadcrumb = true
  # If set to true, prevents Hugo from including the mermaid module if not needed (will reduce load times and traffic)
  disableMermaid = false
  # Specifies the remote location of the mermaid js
  customMermaidURL = "https://unpkg.com/mermaid@8.8.0/dist/mermaid.min.js"
  # Hide Next and Previous page buttons normally displayed full height beside content
  disableNextPrev = true
  # Order sections in menu by "weight" or "title". Default to "weight"
  ordersectionsby = "weight"
  # Change default color scheme with a variant one. Can be "red", "blue", "green".
  themeVariant = ""
  # Provide a list of custom css files to load relative from the `static/` folder in the site root.
  custom_css = ["css/foo.css", "css/bar.css"]
  # Change the title separator. Default to "::".
  titleSeparator = "-"
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

## Mermaid

The mermaid configuration parameters can also be set on a specific page. In this case, the global parameter would be overwritten by the local one.

> Example:
>
> Mermaid is globally disabled. By default it won't be loaded by any page.  
> On page "Architecture" you need a class diagram. You can set the mermaid parameters locally to only load mermaid on this page (not on the others).

You also can disable mermaid for specific pages while globally enabled.

## Home Button Configuration

If the `disableLandingPage` option is set to `false`, an Home button will appear
on the left menu. It is an alternative for clicking on the logo. To edit the
appearance, you will have to configure two parameters for the defined languages:

```toml
[Lanugages]
[Lanugages.en]
...
landingPageURL = "/en"
landingPageName = "<i class='fas fa-home'></i> Redirect to Home"
...
[Lanugages.fr]
...
landingPageURL = "/fr"
landingPageName = "<i class='fas fa-home'></i> Accueil"
...
```

If those params are not configured for a specific language, they will get their
default values:

```toml
landingPageURL = "/"
landingPageName = "<i class='fas fa-home'></i> Home"
```

The home button is going to looks like this:

![Default Home Button](/en/basics/configuration/images/home_button_defaults.jpg?width=100%)
