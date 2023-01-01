---
date: 2016-04-09T16:50:16+02:00
title: Configuration
weight: 20
---

## Paramètres globaux du site

En plus de la [configuration globale d'Hugo](https://gohugo.io/overview/configuration/), **Hugo-theme-learn** vous permet de définir les paramètres suivant dans votre fichier `config.toml` (ci-dessous sont affichées les valeurs par défaut).

Notez que certains de ces paramètres sont expliqués en détails dans d'autres sections de cette documentation.

```toml
[params]
  # L'URL préfixe pour éditer la page courante. Ce paramètre affichera un bouton "Modifier cette page" on haut de de chacune des pages.
  # Pratique pour donner les possibilité à vos utilisateurs de créer une merge request pour votre doc.
  # Allez voir le fichier config.toml de cette documentation pour avoir un exemple.
  editURL = ""
  # Autheur du site, est utilisé dans les informations meta
  author = ""
  # Description du site, est utilisé dans les informations meta
  description = ""
  # Affiche une icône lorsque la page a été visitée
  showVisitedLinks = false
  # Désactive la fonction de recherche. Une valeur à true cache la barre de recherche.
  disableSearch = false
  # Par défaut, le cache Javascript et CSS est automatiquement vidé lorsqu'une nouvelle version du site est générée.
  # Utilisez ce paramètre lorsque vous voulez désactiver ce comportement (c'est parfois incompatible avec certains proxys)
  disableAssetsBusting = false
  # Utilisez ce paramètre pour désactiver le bouton copy-to-clipboard pour le code formatté sur une ligne.
  disableInlineCopyToClipBoard = false
  # Un titre est défini par défaut lorsque vous utilisez un raccourci dans le menu. Utilisez ce paramètre pour le cacher.
  disableShortcutsTitle = false
  # S'il est réglé sur faux, un bouton Accueil apparaîtra sous la barre de recherche dans le menu.
  # Il redirige vers la page d'accueil de la langue actuelle si cela est spécifié. (La valeur par défaut est "/")
  disableLandingPageButton = true
  # Si défini à true, empêche Hugo d'inclure le module "mermaid" s'il n'est pas nécessaire (réduira les temps de chargement et le trafic)
  disableMermaid = false
  # Spécifie l'emplacement distant du mermaid js
  customMermaidURL = "https://unpkg.com/mermaid@8.8.0/dist/mermaid.min.js"
  # Quand vous utilisez un site multi-langue, utilisez ce paramètre pour désactiver le bouton de changement de langue.
  disableLanguageSwitchingButton = false
  # Ordonne les sections dans menu par poids ("weight") ou titre ("title"). Défaut à "weight"
  ordersectionsby = "weight"
  # Utilisez ce paramètre pour modifier le schéma de couleur du site. Les valeurs par défaut sont "red", "blue", "green".
  themeVariant = ""
  # Fournissez une liste de fichiers css personnalisés à charger par rapport depuis le dossier `static/` à la racine du site.
  custom_css = ["css/foo.css", "css/bar.css"]
```

## Activer la recherche {#activer-recherche}

Si ce n'est pas déjà présent, ajoutez les lignes suivantes dans le fichier `config.toml`.

```toml
[outputs]
home = [ "HTML", "RSS", "JSON"]
```

Le thème *Learn* utilise les dernières améliorations d'Hugo pour générer un fichier d'index JSON, prêt à être consommé par le moteur de recherche lunr.js.

> Hugo génère lunrjs index.json à la racine du dossier `public`.
> Quand vous générez le site avec `hugo server`, Hugo génère le fichier en mémoire, il n'est donc pas disponible sur le disque.

## Mermaid

Les paramètres de configuration du mermaid peuvent également être définis sur une page spécifique. Dans ce cas, le paramètre global sera écrasé par le paramètre local.

> Exemple:
>
> Mermaid est globalement handicapé. Par défaut, elle ne sera chargée par aucune page.  
> À la page "Architecture", vous avez besoin d'un diagramme de classe. Vous pouvez régler les paramètres de mermaid localement pour ne charger que la sirène sur cette page (pas sur les autres).

Vous pouvez également désactiver mermaid pour des pages spécifiques tout en l'activant globalement.
<<<<<<< HEAD

## Configuration du bouton Accueil

Si l'option `disableLandingPage` est définie sur `false`, un bouton
"Accueil" apparaîtra dans le menu de gauche. C'est une alternative pour cliquer
sur le logo. Pour modifier le vous devrez configurer deux paramètres pour les
langues définies :

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

Si ces paramètres ne sont pas configurés pour une langue spécifique, ils
obtiendront leur valeurs par défaut:

```toml
landingPageURL = "/"
landingPageName = "<i class='fas fa-home'></i> Home"
```

Le bouton d'accueil va ressembler à ceci:

![Default Home Button](/en/basics/configuration/images/home_button_defaults.jpg?width=100%)
=======
>>>>>>> 023fe7ef2b4c45fe66ac932d9e25d09f30b74a4e
