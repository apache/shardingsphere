---
title: Archétypes
weight: 10
---

En utilisant la commande: `hugo new [chemin vers nouveau contenu]`, vous pouvez créer un nouveau fichier avec la date et le title automatiquement initialisé. Même si c'est une fonctionnalité intéressante, elle reste limitée pour les auteurs actifs qui ont besoin de mieux : les [archetypes](https://gohugo.io/content/archetypes/).

Les archétypes sont des squelettes de pages préconfigurées avec un Front Matter par défaut. Merci de vous référer à la documentation pour connaitre les différents types de page.

## Chapitre {#archetypes-chapter}

Pour créer un chapitre, lancez les commandes suivantes

```
hugo new --kind chapter <name>/_index.md
```

Cela crééra une page avec le Front Matter suivant:

```markdown
+++
title = "{{ replace .Name "-" " " | title }}"
date = {{ .Date }}
weight = 5
chapter = true
pre = "<b>X. </b>"
+++

### Chapter X

# Some Chapter title

Lorem Ipsum.
```

## Défaut

Pour créer une page classique, lancer l'une des deux commandes suivantes

```
# Soit
hugo new <chapter>/<name>/_index.md
# Ou
hugo new <chapter>/<name>.md
```

Cela crééra une page avec le Front Matter suivant:

```markdown
+++
title = "{{ replace .Name "-" " " | title }}"
date =  {{ .Date }}
weight = 5
+++

Lorem Ipsum.
```