---
title: Icons and logos
weight: 27
---

The Learn theme for Hugo loads the [**Font Awesome**](https://fontawesome.com) library, allowing you to easily display any icon or logo available in the Font Awesome free collection.

## Finding an icon

Browse through the available icons in the [Font Awesome Gallery](https://fontawesome.com/icons?d=gallery&m=free). Notice that the **free** filter is enabled, as only the free icons are available by default.

Once on the Font Awesome page for a specific icon, for example the page for the [heart](https://fontawesome.com/icons/heart?style=solid), copy the HTML reference and paste into the markdown content.

The HTML to include the heart icon is:

```
<i class="fas fa-heart"></i>
```

## Including in markdown

Paste the `<i>` HTML into markup and Font Awesome will load the relevant icon.

```
Built with <i class="fas fa-heart"></i> from Grav and Hugo
```

Which appears as

Built with <i class="fas fa-heart"></i> from Grav and Hugo

## Customising icons

Font Awesome provides many ways to modify the icon

* Change colour (by default the icon will inherit the parent colour)
* Increase or decrease size
* Rotate
* Combine with other icons

Check the full documentation on [web fonts with CSS](https://fontawesome.com/how-to-use/web-fonts-with-css) for more.
