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
            Ajout à Mermaid                          :1d
    {{</* /mermaid */>}}

renders as

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

### Class example

    {{/* mermaid */}}
    classDiagram
      Class01 <|-- AveryLongClass : Cool
      Class03 *-- Class04
      Class05 o-- Class06
      Class07 .. Class08
      Class09 --> C2 : Where am i?
      Class09 --* C3
      Class09 --|> Class07
      Class07 : equals()
      Class07 : Object[] elementData
      Class01 : size()
      Class01 : int chimp
      Class01 : int gorilla
      Class08 <--> C2: Cool label
    {{/* /mermaid */}}

renders as

{{< mermaid >}}
classDiagram
  Class01 <|-- AveryLongClass : Cool
  Class03 *-- Class04
  Class05 o-- Class06
  Class07 .. Class08
  Class09 --> C2 : Where am i?
  Class09 --* C3
  Class09 --|> Class07
  Class07 : equals()
  Class07 : Object[] elementData
  Class01 : size()
  Class01 : int chimp
  Class01 : int gorilla
  Class08 <--> C2: Cool label
{{< /mermaid >}}

### Git example

    {{</* mermaid */>}}
    gitGraph:
    options
    {
      "nodeSpacing": 150,
      "nodeRadius": 10
    }
    end
      commit
      branch newbranch
      checkout newbranch
      commit
      commit
      checkout master
      commit
      commit
      merge newbranch
    {{</* /mermaid */>}}

renders as

{{< mermaid >}}
gitGraph:
options
{
  "nodeSpacing": 150,
  "nodeRadius": 10
}
end
  commit
  branch newbranch
  checkout newbranch
  commit
  commit
  checkout master
  commit
  commit
  merge newbranch
{{< /mermaid >}}

### State Diagrams

    {{</* mermaid */>}}
    stateDiagram-v2
      ouvert: Ouvert
      clos: Clos
      fermé: Fermé
      ouvert --> clos
      clos   --> fermé: Lock
      fermé --> clos: Unlock
      clos --> ouvert: Open
    {{</* /mermaid */>}}

renders as

{{<mermaid>}}
stateDiagram-v2
  ouvert: Ouvert
  clos: Clos
  fermé: Fermé
  ouvert --> clos
  clos   --> fermé: Lock
  fermé --> clos: Unlock
  clos --> ouvert: Open
{{</mermaid>}}
