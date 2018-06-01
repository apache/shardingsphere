---
title : "Mermaid"
description : "Génération de diagrammes à partir de texte, dans le même style que Markdown"
---

[Mermaid](https://mermaidjs.github.io/) est une bibliothèque Javascript qui permet de générer des diagrammes (séquence, état, gantt, etc.) à partir de texte, dans le même style que Markdown.

Insérer votre code Mermaid dans un shortcode `mermaid` et c'est tout.

## Flowchart example
	{{</*mermaid align="left"*/>}}
	graph LR;
		A[Bords droits] -->|Lien texte| B(Bords arondis)
    	B --> C{Décision}
    	C -->|Un| D[Résultat un]
    	C -->|Deux| E[Résultat deux]
    {{</* /mermaid */>}}

renders as

{{<mermaid align="left">}}
graph LR;
	A[Bords droits] -->|Lien texte| B(Bords arondis)
	B --> C{Décision}
	C -->|Un| D[Résultat un]
	C -->|Deux| E[Résultat deux]
{{< /mermaid >}}

## Sequence example

	{{</*mermaid*/>}}
	sequenceDiagram
	    participant Alice
	    participant Bob
	    Alice->>John: Salut John, comment vas-tu?
	    loop Vérification
	        John->John: Se bat contre l'hyponcodrie.
	    end
	    Note right of John: Les pensées rationnelles<br/>prédominent...
	    John-->Alice: Super!
	    John->Bob: Et toi?
	    Bob-->John: Au top!
	{{</* /mermaid */>}}

renders as

{{<mermaid>}}
sequenceDiagram
	participant Alice
	participant Bob
	Alice->>John: Salut John, comment vas-tu?
	loop Vérification
		John->John: Se bat contre l'hyponcodrie.
	end
	Note right of John: Les pensées rationnelles<br/>prédominent...
	John-->Alice: Super!
	John->Bob: Et toi?
	Bob-->John: Au top!
{{< /mermaid >}}

## GANTT Example

	{{</*mermaid*/>}}
	gantt
	        dateFormat  YYYY-MM-DD
	        title Ajout de la fonctionnalité de GANTT à Mermaid
	        section Une section
	        Tâche complétée            :done,    des1, 2014-01-06,2014-01-08
	        Tâche en cours             :active,  des2, 2014-01-09, 3d
	        Future tâche               :         des3, after des2, 5d
	        Future tâche 2             :         des4, after des3, 5d
	        section Tâches critiques
	        Tâche complétée dans le chemin critique :crit, done, 2014-01-06,24h
	        Implémenter le parser et jison          :crit, done, after des1, 2d
	        Créer des tests pour le parser          :crit, active, 3d
	        Future tâche dans le chemin critique    :crit, 5d
	        Créer des tests pour le renderer        :2d
	        Ajout à Mermaid                      	:1d
	{{</* /mermaid */>}}


render as

{{<mermaid>}}
gantt
		dateFormat  YYYY-MM-DD
		title Ajout de la fonctionnalité de GANTT à Mermaid
		section Une section
		Tâche complétée            :done,    des1, 2014-01-06,2014-01-08
		Tâche en cours             :active,  des2, 2014-01-09, 3d
		Future tâche               :         des3, after des2, 5d
		Future tâche 2             :         des4, after des3, 5d
		section Tâches critiques
		Tâche complétée dans le chemin critique :crit, done, 2014-01-06,24h
		Implémenter le parser et jison          :crit, done, after des1, 2d
		Créer des tests pour le parser             :crit, active, 3d
		Future tâche dans le chemin critique        :crit, 5d
		Créer des tests pour le renderer           :2d
		Ajout à Mermaid                      :1d
{{</mermaid>}}



