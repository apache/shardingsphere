---
title : "Mermaid"
description : "Generation of diagram and flowchart from text in a similar manner as markdown"
---

[Mermaid](https://mermaidjs.github.io/) is a library helping you to generate diagram and flowcharts from text, in a similar manner as Markdown.

Just insert your mermaid code in the `mermaid` shortcode and that's it.

## Flowchart example
	{{</*mermaid align="left"*/>}}
	graph LR;
		A[Hard edge] -->|Link text| B(Round edge)
    	B --> C{Decision}
    	C -->|One| D[Result one]
    	C -->|Two| E[Result two]
    {{</* /mermaid */>}}

renders as

{{<mermaid align="left">}}
graph LR;
	A[Hard edge] -->|Link text| B(Round edge)
    B --> C{Decision}
    C -->|One| D[Result one]
    C -->|Two| E[Result two]
{{< /mermaid >}}

## Sequence example

	{{</*mermaid*/>}}
	sequenceDiagram
	    participant Alice
	    participant Bob
	    Alice->>John: Hello John, how are you?
	    loop Healthcheck
	        John->John: Fight against hypochondria
	    end
	    Note right of John: Rational thoughts <br/>prevail...
	    John-->Alice: Great!
	    John->Bob: How about you?
	    Bob-->John: Jolly good!
	{{</* /mermaid */>}}

renders as

{{<mermaid>}}
sequenceDiagram
    participant Alice
    participant Bob
    Alice->>John: Hello John, how are you?
    loop Healthcheck
        John->John: Fight against hypochondria
    end
    Note right of John: Rational thoughts <br/>prevail...
    John-->Alice: Great!
    John->Bob: How about you?
    Bob-->John: Jolly good!
{{< /mermaid >}}

## GANTT Example

	{{</*mermaid*/>}}
	gantt
	        dateFormat  YYYY-MM-DD
	        title Adding GANTT diagram functionality to mermaid
	        section A section
	        Completed task            :done,    des1, 2014-01-06,2014-01-08
	        Active task               :active,  des2, 2014-01-09, 3d
	        Future task               :         des3, after des2, 5d
	        Future task2               :         des4, after des3, 5d
	        section Critical tasks
	        Completed task in the critical line :crit, done, 2014-01-06,24h
	        Implement parser and jison          :crit, done, after des1, 2d
	        Create tests for parser             :crit, active, 3d
	        Future task in critical line        :crit, 5d
	        Create tests for renderer           :2d
	        Add to mermaid                      :1d
	{{</* /mermaid */>}}


render as

{{<mermaid>}}
gantt
        dateFormat  YYYY-MM-DD
        title Adding GANTT diagram functionality to mermaid
        section A section
        Completed task            :done,    des1, 2014-01-06,2014-01-08
        Active task               :active,  des2, 2014-01-09, 3d
        Future task               :         des3, after des2, 5d
        Future task2               :         des4, after des3, 5d
        section Critical tasks
        Completed task in the critical line :crit, done, 2014-01-06,24h
        Implement parser and jison          :crit, done, after des1, 2d
        Create tests for parser             :crit, active, 3d
        Future task in critical line        :crit, 5d
        Create tests for renderer           :2d
        Add to mermaid                      :1d
{{</mermaid>}}



