---
date: 2018-11-29T08:41:44+01:00
title: Tags
weight: 40
tags: ["documentation", "tutorial"]
---


Le *thème Learn* supporte une des taxonomy par défaut de GoHugo : les tags.

## Configuration 

Il suffit d'ajouter un tableau de tags sur la page  : 

```markdown
---
date: 2018-11-29T08:41:44+01:00
title: Tutoriel pour le thème
weight: 15
tags: ["tutoriel", "theme"] 
---
```

## Comportement

Les tags sont affichés en haut de la page, dans l'ordre dans lequel ils ont été saisis. 

Chaque tag est un lien vers une page *Taxonomy*, qui affiche tous les article avec ce tag.


## Liste des tags

Il est possible de rajouter un raccourci dans le fichier `config.toml` afin d'afficher une page listant tous les tags

```toml
[[menu.shortcuts]]
name = "<i class='fas fa-tags'></i> Tags"
url = "/tags"
weight = 30
```