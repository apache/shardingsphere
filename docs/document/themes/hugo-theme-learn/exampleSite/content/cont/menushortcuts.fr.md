---
date: 2016-04-09T16:50:16+02:00
title: Raccourcis du menu
weight: 25
---

Vous pouvez définir des entrées ou raccourcis supplémentaires dans le menu sans avoir besoin d'être lié à un contenu du site.

## Configuration simple

Editez le fichier de configuration `config.toml` et ajoutez une entrée `[[menu.shortcuts]]` pour chaque lien que vous voulez ajouter.

Exemple pour ce site:

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

Par défaut, les raccourcis sont précédés par un titre. Ce titre peut être désactivé en ajouter le paramètre `disableShortcutsTitle=true` dans la section `params` de votre `config.toml`. 
Cependant, si vous voulez garder le titre mais changer sa valeur, vous pouvez modifier votre configuration multilangue locale en changeant les *translation string*. 

Par exemple, dans votre fichier local `i18n/en.toml`, ajouter le contenu

    [Shortcuts-Title]
    other = "<Votre valeur>"

Plus d'infos sur [les menus Hugo](https://gohugo.io/extras/menus/) et sur [les translations strings](https://gohugo.io/content-management/multilingual/#translation-of-strings)

## Configuration pour le mode multi-langue {#i18n}

Quand vous utilisez un site multi-langue, vous pouvez avoir des menus différents pour chaque langage. Dans le fichier de configuration `config.toml`, préfixez votre configuration par `Languages.<language-id>`. 


Par exemple, avec ce site :

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

Plus d'infos sur [les menus Hugo](https://gohugo.io/extras/menus/) et les [menus multi-langue Hugo](https://gohugo.io/content-management/multilingual/#menus)