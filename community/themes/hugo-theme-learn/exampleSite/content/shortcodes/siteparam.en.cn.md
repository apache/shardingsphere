---
title: Site param
description : "Get value of site params variables in your page."
---

`siteparam` shortcode is used to help you print values of site params. 

For instance, in this current site, the `editURL` variable is used in `config.toml`

```toml
[params]
  editURL = "https://github.com/matcornic/hugo-theme-learn/edit/master/exampleSite/content/"
```

Use the `siteparam` shortcode to display its value.

```
`editURL` Value : {{%/* siteparam "editURL" */%}}
```

is displayed as

`editURL` Value : {{% siteparam "editURL" %}}