---
date: 2016-04-09T16:50:16+02:00
title: Multi-langue et i18n
weight: 30
---

**Learn** est complètement compatible avec le mode multi-langue d'Hugo.

Il fournit :

- Des *translation strings* pour les valeurs par défaut utilisées par le thème (Anglais et Français). N'hésitez pas à contribuer !
- Génération automatique du menu avec le contenu multi-langue
- Modification de la langue dans le navigateur

![I18n menu](/cont/i18n/images/i18n-menu.gif)

## Configuration simple

Après avoir appris [comment Hugo gère les sites multi-langue](https://gohugo.io/content-management/multilingual), définissez vos langues dans votre fichier `config.toml`.

Par exemple, pour ce site, avec du contenu en français et en anglais.

```toml
# Anglais est la langue par défaut
defaultContentLanguage = "en"
# Force d'avoir /en/ma-page et /fr/ma-page routes, même avec la langue par défaut.
defaultContentLanguageInSubdir= true

[Languages]
[Languages.en]
title = "Documentation for Hugo Learn Theme"
weight = 1
languageName = "English"

[Languages.fr]
title = "Documentation du thème Hugo Learn"
weight = 2
languageName = "Français"
```

Puis, pour chaque nouvelle page, ajoutez *l'id* de la langue du fichier.

- Le fichier `my-page.md` est découpé en deux fichiers :
    - en anglais : `my-page.en.md`
    - en français : `my-page.fr.md`
- Le fichier `_index.md` est découpé en deux fichiers :
    - en anglais: `_index.en.md`
    - en français: `_index.fr.md`

{{% notice info %}}
Attention, seulement les pages traduites sont affichées dans le menu. Le contenu n'est pas remplacé par les pages de la langue par défaut.
{{% /notice %}}

{{% notice tip %}}
Utilisez le paramètre du Front Matter [slug](https://gohugo.io/content-management/multilingual/#translate-your-content) pour traduire également les URLs.
{{% /notice %}}

## Surcharger les *translation strings*

Les *Translations strings* sont utilisées comme valeurs par défaut dans le thème (Bouton *Modifier la page*, Element de subsitution *Recherche*, etc.). Les traductions sont disponibles en français et en anglais mais vous pouvez utiliser n'importe quelle autre langue et surcharger avec vos propres valeurs.

Pour surcharger ces valeurs, créer un nouveau fichier dans votre dossier i18n local `i18n/<idlanguage>.toml` et inspirez vous du thème `themes/hugo-theme-learn/i18n/en.toml` 

D'ailleurs, ces traductions pour servir à tout le monde, donc svp prenez le temps de [proposer une Pull Request](https://github.com/matcornic/hugo-theme-learn/pulls) ! 

## Désactiver le changement de langue

Vous pouvez changer de langue directement dans le navigateur. C'est une super fonctionnalité, mais vous avez peut-être besoin de la désactiver. 

Pour ce faire, ajouter le paramètre `disableLanguageSwitchingButton=true` dans votre `config.toml`

```toml
[params]
  # Quand vous utilisez un site en multi-langue, désactive le bouton de changment de langue.
  disableLanguageSwitchingButton = true
```

![I18n menu](/cont/i18n/images/i18n-menu.gif)