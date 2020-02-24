---
title: Site param
description : "Afficher la valeur d'un paramètre global du site dans votre page"
---

Les shortcode `siteparam` est utilisé pour vous aider à afficher des valeurs provenant des paramètres globaux du site. 

Par exemple, dans ce site, le paramètre `editURL`  est utilisé dans le fichier `config.toml`

```toml
[params]
  editURL = "https://github.com/matcornic/hugo-theme-learn/edit/master/exampleSite/content/"
```

Utilisez le shortcode `siteparam` pour affichier sa valeur.

```
Valeur de `editURL` : {{%/* siteparam "editURL" */%}}
```

s'affiche comme

Valeur de `editURL` : {{% siteparam "editURL" %}}