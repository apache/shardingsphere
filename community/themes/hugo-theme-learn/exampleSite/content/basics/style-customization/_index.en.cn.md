---
date: 2016-04-09T16:50:16+02:00
title: Style customization
weight: 25
---

**Hugo-theme-learn** has been built to be as configurable as possible by defining multiple [partials](https://gohugo.io/templates/partials/)

In `themes/hugo-theme-learn/layouts/partials/`, you will find all the partials defined for this theme. If you need to overwrite something, don't change the code directly. Instead [follow this page](https://gohugo.io/themes/customizing/). You'd create a new partial in the `layouts/partials` folder of your local project. This partial will have the priority.

This theme defines the following partials :

- *header*: the header of the content page (contains the breadcrumbs). _Not meant to be overwritten_
- *custom-header*: custom headers in page. Meant to be overwritten when adding CSS imports. Don't forget to include `style` HTML tag directive in your file
- *footer*: the footer of the content page (contains the arrows). _Not meant to be overwritten_
- *custom-footer*:  custom footer in page. Meant to be overwritten when adding Javacript. Don't forget to include `javascript` HTML tag directive in your file
- *favicon*: the favicon
- *logo*: the logo, on top left hand corner.
- *meta*: HTML meta tags, if you want to change default behavior
- *menu*: left menu. _Not meant to be overwritten_
- *menu-footer*: footer of the the left menu
- *search*: search box
- *toc*: table of contents

## Change the logo

Create a new file in `layouts/partials/` named `logo.html`. Then write any HTML you want.
You could use an `img` HTML tag and reference an image created under the *static* folder, or you could paste a SVG definition !

{{% notice note %}}
The size of the logo will adapt automatically
{{% /notice %}}

## Change the favicon

If your favicon is a png, just drop off your image in your local `static/images/` folder and names it `favicon.png`

If you need to change this default behavior, create a new file in `layouts/partials/` named `favicon.html`. Then write something like this:

```html
<link rel="shortcut icon" href="/images/favicon.png" type="image/x-icon" />
```

## Change default colors {#theme-variant}

**Hugo Learn theme** let you choose between 3 native color scheme variants, but feel free to add one yourself ! Default color scheme is based on [Grav Learn Theme](https://learn.getgrav.org/).

### Red variant

```toml
[params]
  # Change default color scheme with a variant one. Can be "red", "blue", "green".
  themeVariant = "red"
```

![Red variant](/basics/style-customization/images/red-variant.png?width=60pc)

### Blue variant

```toml
[params]
  # Change default color scheme with a variant one. Can be "red", "blue", "green".
  themeVariant = "blue"
```

![Blue variant](/basics/style-customization/images/blue-variant.png?width=60pc)

### Green variant

```toml
[params]
  # Change default color scheme with a variant one. Can be "red", "blue", "green".
  themeVariant = "green"
```

![Green variant](/basics/style-customization/images/green-variant.png?width=60pc)

### 'Yours‘ variant

First, create a new CSS file in your local `static/css` folder prefixed by `theme` (e.g. with _mine_ theme `static/css/theme-mine.css`). Copy the following content and modify colors in CSS variables.

```css

:root{
    
    --MAIN-TEXT-color:#323232; /* Color of text by default */
    --MAIN-TITLES-TEXT-color: #5e5e5e; /* Color of titles h2-h3-h4-h5 */
    --MAIN-LINK-color:#1C90F3; /* Color of links */
    --MAIN-LINK-HOVER-color:#167ad0; /* Color of hovered links */
    --MAIN-ANCHOR-color: #1C90F3; /* color of anchors on titles */

    --MENU-HEADER-BG-color:#1C90F3; /* Background color of menu header */
    --MENU-HEADER-BORDER-color:#33a1ff; /*Color of menu header border */ 

    --MENU-SEARCH-BG-color:#167ad0; /* Search field background color (by default borders + icons) */
    --MENU-SEARCH-BOX-color: #33a1ff; /* Override search field border color */
    --MENU-SEARCH-BOX-ICONS-color: #a1d2fd; /* Override search field icons color */

    --MENU-SECTIONS-ACTIVE-BG-color:#20272b; /* Background color of the active section and its childs */
    --MENU-SECTIONS-BG-color:#252c31; /* Background color of other sections */
    --MENU-SECTIONS-LINK-color: #ccc; /* Color of links in menu */
    --MENU-SECTIONS-LINK-HOVER-color: #e6e6e6;  /* Color of links in menu, when hovered */
    --MENU-SECTION-ACTIVE-CATEGORY-color: #777; /* Color of active category text */
    --MENU-SECTION-ACTIVE-CATEGORY-BG-color: #fff; /* Color of background for the active category (only) */

    --MENU-VISITED-color: #33a1ff; /* Color of 'page visited' icons in menu */
    --MENU-SECTION-HR-color: #20272b; /* Color of <hr> separator in menu */
    
}

body {
    color: var(--MAIN-TEXT-color) !important;
}

textarea:focus, input[type="email"]:focus, input[type="number"]:focus, input[type="password"]:focus, input[type="search"]:focus, input[type="tel"]:focus, input[type="text"]:focus, input[type="url"]:focus, input[type="color"]:focus, input[type="date"]:focus, input[type="datetime"]:focus, input[type="datetime-local"]:focus, input[type="month"]:focus, input[type="time"]:focus, input[type="week"]:focus, select[multiple=multiple]:focus {
    border-color: none;
    box-shadow: none;
}

h2, h3, h4, h5 {
    color: var(--MAIN-TITLES-TEXT-color) !important;
}

a {
    color: var(--MAIN-LINK-color);
}

.anchor {
    color: var(--MAIN-ANCHOR-color);
}

a:hover {
    color: var(--MAIN-LINK-HOVER-color);
}

#sidebar ul li.visited > a .read-icon {
	color: var(--MENU-VISITED-color);
}

#body a.highlight:after {
    display: block;
    content: "";
    height: 1px;
    width: 0%;
    -webkit-transition: width 0.5s ease;
    -moz-transition: width 0.5s ease;
    -ms-transition: width 0.5s ease;
    transition: width 0.5s ease;
    background-color: var(--MAIN-LINK-HOVER-color);
}
#sidebar {
	background-color: var(--MENU-SECTIONS-BG-color);
}
#sidebar #header-wrapper {
    background: var(--MENU-HEADER-BG-color);
    color: var(--MENU-SEARCH-BOX-color);
    border-color: var(--MENU-HEADER-BORDER-color);
}
#sidebar .searchbox {
	border-color: var(--MENU-SEARCH-BOX-color);
    background: var(--MENU-SEARCH-BG-color);
}
#sidebar ul.topics > li.parent, #sidebar ul.topics > li.active {
    background: var(--MENU-SECTIONS-ACTIVE-BG-color);
}
#sidebar .searchbox * {
    color: var(--MENU-SEARCH-BOX-ICONS-color);
}

#sidebar a {
    color: var(--MENU-SECTIONS-LINK-color);
}

#sidebar a:hover {
    color: var(--MENU-SECTIONS-LINK-HOVER-color);
}

#sidebar ul li.active > a {
    background: var(--MENU-SECTION-ACTIVE-CATEGORY-BG-color);
    color: var(--MENU-SECTION-ACTIVE-CATEGORY-color) !important;
}

#sidebar hr {
    border-color: var(--MENU-SECTION-HR-color);
}
```

Then, set the `themeVariant` value with the name of your custom theme file. That's it !

```toml
[params]
  # Change default color scheme with a variant one. Can be "red", "blue", "green".
  themeVariant = "mine"
```
