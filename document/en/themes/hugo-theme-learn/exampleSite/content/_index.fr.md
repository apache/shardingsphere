---
title: "Learn Theme for Hugo"
---

# Thème Hugo learn

[Hugo-theme-learn](http://github.com/matcornic/hugo-theme-learn) est un thème pour [Hugo](https://gohugo.io/), un générateur de site statique, rapide et modern, écrit en Go. Tandis que Hugo est souvent utilisé pour des blogs, ce thème multi-langue est **entièrement conçu pour la documentation**.

Ce thème est un portage partiel du [thème Learn](http://learn.getgrav.org/) de [Grav](https://getgrav.org/), un CMS modern écrit en PHP.

{{% notice tip %}}Le thème Learn fonctionne grâce à la structure de page aborescentes pour organiser le contenu: tous les contenus sont des pages qui appartiennent à d'autres pages. [Plus d'infos]({{%relref "cont/pages/_index.md"%}}) 
{{% /notice %}}

## Fonctionnalités principales

* [Recherche automatique]({{%relref "basics/configuration/_index.md#activer-recherche" %}})
* [Mode multi-langue]({{%relref "cont/i18n/_index.md" %}})
* **Nombre de niveau infini dans le menu**
* **Boutons suivant/précédent automatiquement générés pour naviguer entre les items du menu**
* [Taille d'image, ombres...]({{%relref "cont/markdown.fr.md#images" %}})
* [Fichiers joints]({{%relref "shortcodes/attachments.fr.md" %}})
* [Lister les pages filles]({{%relref "shortcodes/children/_index.md" %}})
* [Diagrammes Mermaid]({{%relref "shortcodes/mermaid.fr.md" %}}) (flowchart, sequence, gantt)
* [Style configurable and variantes de couleurs]({{%relref "basics/style-customization/_index.md"%}})
* [Boutons]({{%relref "shortcodes/button.fr.md" %}}), [Messages Astuce/Note/Info/Attention]({{%relref "shortcodes/notice.fr.md" %}}), [Expand]({{%relref "shortcodes/expand.fr.md" %}})

![Screenshot](https://github.com/matcornic/hugo-theme-learn/raw/master/images/screenshot.png?width=40pc&classes=shadow)

## Contribuer à cette documentation

N'hésitez pas à mettre à jour ce contenu en cliquant sur le lien **Modifier cette page** en haut de chaque page, et créer la Pull Request associée.

{{% notice info %}}
Votre modification sera déployée automatiquement quand elle sera mergée.
{{% /notice %}}

## Site de documentation

Cette documentation statique a été générée avec Hugo avec une simple commande : `hugo -t hugo-theme-learn` -- le code source est [disponible sur Github](https://github.com/matcornic/hugo-theme-learn)

{{% notice note %}}
Le site est auomatiquement publié et hébergé par [Netlify](https://www.netlify.com/). Plus d'infos sur le [déploiement de site Hugo avec Netlify](https://www.netlify.com/blog/2015/07/30/hosting-hugo-on-netlifyinsanely-fast-deploys/)(En anglais)
{{% /notice %}}
