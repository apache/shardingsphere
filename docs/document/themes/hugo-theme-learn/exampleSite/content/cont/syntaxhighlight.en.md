---
date: 2020-06-01T13:31:12+01:00
title: Code highlighting
weight: 16
---

Learn theme uses [highlight.js](https://highlightjs.org/) to provide code syntax highlighting.

## Markdown syntax

Wrap the code block with three backticks and the name of the language. Highlight will try to auto detect the language if one is not provided.

<!-- markdownlint-disable MD046 -->
```plaintext
    ```json
    [
      {
        "title": "apples",
        "count": [12000, 20000],
        "description": {"text": "...", "sensitive": false}
      },
      {
        "title": "oranges",
        "count": [17500, null],
        "description": {"text": "...", "sensitive": false}
      }
    ]
    ```
```
<!-- markdownlint-disable MD046 -->

Renders to:

```json
[
  {
    "title": "apples",
    "count": [12000, 20000],
    "description": {"text": "...", "sensitive": false}
  },
  {
    "title": "oranges",
    "count": [17500, null],
    "description": {"text": "...", "sensitive": false}
  }
]
```

## Supported languages

Learn theme ships with its own version of highlight.js to support offline browsing. The included package supports 38 common languages, as described on the [highlight.js download page](https://highlightjs.org/download/).

## Identifying failed language detection

Highlight will write a warning to the browser console if a requested language was not found. For example, the following code block references an imaginary language `foo`. An error will be output to the console on this page.

```plaintext
    ```foo
    bar
    ```
```

```nohighlight
Could not find the language 'foo', did you forget to load/include a language module?(anonymous) @ highlight.pack.js
```

## Supporting additional languages

To support languages other than the 38 common languages included in the default highlight.js you will need to download your own version of highlight.js and add it to your site content.

### Download custom highlight.js

Visit [https://highlightjs.org/download/](https://highlightjs.org/download/) and select your desired language support. Note that more languages means greater package size.

### Add custom highlight.js to static resources

Inside the zip archive downloaded from highlight.js extract the file named `highlight.pack.js`. Move this file to the **new** location

```nohighlight
static/js/highlight.pack.js
```

**Do not** replace the existing file at `themes/hugo-theme-learn/static/js/highlight.pack.js`.

Including the file in the correct path will override the theme default highlight.pack.js and prevent issues caused in the future if the theme default file is updated.

## Further usage information

See [https://highlightjs.org/usage/](https://highlightjs.org/usage/)
