# ShardingSphere Documents

## HOW to build ShardingSphere website?

This directory contains the document, community and blog content and article media.
Maintain Markdown files under each site's `content` directory and article media under its existing `static` paths.
Keep the existing language suffixes, front matter and shortcode calls when editing content.

The [shardingsphere-doc](https://github.com/apache/shardingsphere-doc.git) repository owns the website's templates, styles, fonts, Hugo configuration and build scripts.
Its `.github/scripts/build-shardingsphere-docs.sh` entry assembles the content with `.github/website/shardingsphere` presentation sources and fixed Learn theme commits in a temporary directory.
The build uses Hugo `0.83.1` extended and preserves the `document/current`, `community` and `blog` publishing paths.
Rebuilding a historical content version uses the current presentation sources; existing published historical pages are not automatically rebuilt.
Changes to presentation or theme versions belong in `shardingsphere-doc`, including updates to the document version selector.

## HOW to insert a video of Bilibili or YouTube?

1. First please make sure you get the correct url of the video you want to add.

2. - DO REMEMBER use the `bvid argument` of bilibili url:

     for example : https://www.bilibili.com/video/BV1aE411X7kQ?from=search&seid=5978719442257399675 ( bvid argument means `BV1aE411X7kQ` , or you can simply recognize the argument with `Capital Letter` )

   - YouTube url is much easier to recognize : https://www.youtube.com/watch?v=2MsN8gpT6jY , the argument you needed is `2MsN8gpT6jY` at the end of the url.

3. Use the shortcodes at wherever you want to place:

   - `{{< bilibili BV1aE411X7kQ >}}`

   - `{{< youtube 2MsN8gpT6jY >}}`
