---
title: Installation
weight: 15
---

Les étapes suivantes sont là pour vous aider à initialiser votre site. Si vous ne connaissez pas du tout Hugo, il est fortement conseillé de vous entrainer en suivant ce [super tuto pour débutants](https://gohugo.io/overview/quickstart/).

## Créer votre projet

Hugo fournit une commande `new` pour créer un nouveau site.

```
hugo new site <new_project>
```

## Installer le thème

Installer le thème **Hugo-theme-learn** en suivant [cette documentation](https://gohugo.io/themes/installing/)

Le repo du thème est : https://github.com/matcornic/hugo-theme-learn.git

Sinon, vous pouvez [télécharger le thème sous forme d'un fichier .zip](https://github.com/matcornic/hugo-theme-learn/archive/master.zip) et extrayez le dans votre dossier de thèmes.

## Configuration simple

Lorsque vous générez votre site, vous pouvez définir un thème en utilisant l'option `--theme`. Il est conseillé de modifier votre fichier de configuration `config.toml` and définir votre thème par défaut. En passant, ajoutez les prérequis à l'utilisation de la fonctionnalité de recherche.

```toml
# Modifiez le thème pour qu'il soit utilisé par défaut à chaque génération de site.
theme = "hugo-theme-learn"

# Pour la fonctionnalité de recherche
[outputs]
home = [ "HTML", "RSS", "JSON"]
```

## Créer votre première page chapitre

Les *chapitres* sont des pages contenant d'autre pages filles. Elles ont un affichage spécial et contiennent habituellement juste un _nom_ de chapitre, le _titre_ et un _résumé_ de la section.

```
### Chapitre 1

# Démarrage

Découvrez comment utiliser ce thème Hugo et apprenez en les concepts
```

s'affiche comme

![Un chapitre](/en/basics/installation/images/chapter.png?classes=shadow&width=60pc)

**Hugo-theme-learn** fournit des archétypes pour créer des squelettes pour votre site. Commencez par créer votre premier chapitre avec la commande suivante:

```
hugo new --kind chapter basics/_index.md
```

En ouvrant le fichier généré, vous devriez voir la propriété `chapter=true` en haut, paramètre quit définit que le page est un _chapitre_.

## Créer votre première page

Puis, créez votre premier page dans le chapitre précédent. Pour ce faire, il existe deux possibilités :

```
hugo new basics/first-content.md
hugo new basics/second-content/_index.md
```

N'hésitez pas à éditer ces fichiers en ajoutant des exemple de contenu et en remplaçant le paramètre `title` au début du fichier. 

## Lancer le site localement

Lancez la commande suivante :

```
hugo serve
```

Se rendre sur `http://localhost:1313`

Vous devriez voir trois choses:

1. Vous avez un menu **Basics** à gauche, qui contient deux sous-menu avec des noms égal au paramètre `title` des fichiers précédemment générés.
2. La page d'accueil vous explique comment la modifier. Suivez les instructions.
3. Avec la commande `hugo serve`, la page se rafraichit automatiquement à chaque fois que vous sauvegardez. Super !

## Générez le site

Quand votre site est prêt à être déployé, lancez la commande suivante:

```
hugo
```

Un dossier `public` a été généré. Il contient tout le contenu statique et les ressources nécessaires pour votre site. Votre site peut maintenant être déployé en utilisant n'importe quel serveur !

{{% notice note %}}
Ce site peut être automatiquement publié et hébergé avec [Netlify](https://www.netlify.com/) ([Plus d'infos](https://www.netlify.com/blog/2015/07/30/hosting-hugo-on-netlifyinsanely-fast-deploys/)). Sinon, vous pouvez utiliser les [Github pages](https://gohugo.io/hosting-and-deployment/hosting-on-github/)
{{% /notice %}}