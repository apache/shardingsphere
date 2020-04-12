---
date: 2016-04-09T16:50:16+02:00
title: Shortcodes
pre: "<b>3. </b>"
weight: 15
---

Hugo utilise Markdown pour son format simple. Cependant, il y a beaucoup de chose que Markdown ne supporte pas bien. On pourrait utiliser du HTML pur pour améliorer les capacité du Markdown.

Mais c'est probablement une mauvaise idée. Tout le monde utilise le Markdown parce que c'est pur et simple à lire même lorsqu'il est affiché en texte brut. Vous devez éviter le HTML autant que possible pour garder le contenu simple.

Cependant, pour éviter les limitations, Hugo a créé les [shortcodes](https://gohugo.io/extras/shortcodes/). Un shortcode est un bout de code (*snippet*) dans une page.

**Hugo-theme-learn** fournit de multiple shortcodes en plus de ceux existant.

{{%children style="h2" description="true" %}}
