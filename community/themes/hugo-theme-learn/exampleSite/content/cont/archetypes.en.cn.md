---
title: Archetypes
weight: 10
---

Using the command: `hugo new [relative new content path]`, you can start a content file with the date and title automatically set. While this is a welcome feature, active writers need more : [archetypes](https://gohugo.io/content/archetypes/).

It is pre-configured skeleton pages with default front matter. Please refer to the documentation for types of page to understand the differences.

## Chapter {#archetypes-chapter}

To create a Chapter page, run the following commands

```
hugo new --kind chapter <name>/_index.md
```

It will create a page with predefined Front-Matter:

```markdown
+++
title = "{{ replace .TranslationBaseName "-" " " | title }}"
date = {{ .Date }}
weight = 5
chapter = true
pre = "<b>X. </b>"
+++

### Chapter X

# Some Chapter title

Lorem Ipsum.
```

## Default

To create a default page, run either one of the following commands

```
# Either
hugo new <chapter>/<name>/_index.md
# Or
hugo new <chapter>/<name>.md
```

It will create a page with predefined Front-Matter:

```markdown
+++
title = "{{ replace .TranslationBaseName "-" " " | title }}"
date =  {{ .Date }}
weight = 5
+++

Lorem Ipsum.
```