---
date: 2016-04-09T16:50:16+02:00
title: Menu extra shortcuts
weight: 25
---

You can define additional menu entries or shortcuts in the navigation menu without any link to content.

## Basic configuration

Edit the website configuration `config.toml` and add a `[[menu.shortcuts]]` entry for each link your want to add.

Example from the current website:

    [[menu.shortcuts]] 
    name = "<i class='fab fa-github'></i> Github repo"
    identifier = "ds"
    url = "https://github.com/matcornic/hugo-theme-learn"
    weight = 10

    [[menu.shortcuts]]
    name = "<i class='fas fa-camera'></i> Showcases"
    url = "/showcase"
    weight = 11

    [[menu.shortcuts]]
    name = "<i class='fas fa-bookmark'></i> Hugo Documentation"
    identifier = "hugodoc"
    url = "https://gohugo.io/"
    weight = 20

    [[menu.shortcuts]]
    name = "<i class='fas fa-bullhorn'></i> Credits"
    url = "/credits"
    weight = 30

By default, shortcuts are preceded by a title. This title can be disabled by setting `disableShortcutsTitle=true`. 
However, if you want to keep the title but change its value, it can be overriden by changing your local i18n translation string configuration. 

For example, in your local `i18n/en.toml` file, add the following content

    [Shortcuts-Title]
    other = "<Your value>"

Read more about [hugo menu](https://gohugo.io/extras/menus/) and [hugo i18n translation strings](https://gohugo.io/content-management/multilingual/#translation-of-strings)

## Configuration for Multilingual mode {#i18n}

When using a multilingual website, you can set different menus for each language. In the `config.toml` file, prefix your menu configuration by `Languages.<language-id>`. 


Example from the current website:

    [Languages]
    [Languages.en]
    title = "Documentation for Hugo Learn Theme"
    weight = 1
    languageName = "English"

    [[Languages.en.menu.shortcuts]] 
    name = "<i class='fab fa-github'></i> Github repo"
    identifier = "ds"
    url = "https://github.com/matcornic/hugo-theme-learn"
    weight = 10

    [[Languages.en.menu.shortcuts]]
    name = "<i class='fas fa-camera'></i> Showcases"
    url = "/showcase"
    weight = 11

    [[Languages.en.menu.shortcuts]]
    name = "<i class='fas fa-bookmark'></i> Hugo Documentation"
    identifier = "hugodoc"
    url = "https://gohugo.io/"
    weight = 20

    [[Languages.en.menu.shortcuts]]
    name = "<i class='fas fa-bullhorn'></i> Credits"
    url = "/credits"
    weight = 30

    [Languages.fr]
    title = "Documentation du thème Hugo Learn"
    weight = 2
    languageName = "Français"

    [[Languages.fr.menu.shortcuts]]
    name = "<i class='fab fa-github'></i> Repo Github"
    identifier = "ds"
    url = "https://github.com/matcornic/hugo-theme-learn"
    weight = 10

    [[Languages.fr.menu.shortcuts]]
    name = "<i class='fas fa-camera'></i> Vitrine"
    url = "/showcase"
    weight = 11

    [[Languages.fr.menu.shortcuts]]
    name = "<i class='fas fa-bookmark'></i> Documentation Hugo"
    identifier = "hugodoc"
    url = "https://gohugo.io/"
    weight = 20

    [[Languages.fr.menu.shortcuts]]
    name = "<i class='fas fa-bullhorn'></i> Crédits"
    url = "/credits"
    weight = 30

Read more about [hugo menu](https://gohugo.io/extras/menus/) and [hugo multilingual menus](https://gohugo.io/content-management/multilingual/#menus)