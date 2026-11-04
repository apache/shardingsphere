# ShardingSphere Documents

## HOW to build ShardingSphere website?

To build ShardingSphere website by means of [hugo](http://gohugo.io/overview/introduction/) and [hugo theme learn](https://github.com/matcornic/hugo-theme-learn).

Follow the steps below to deploy ShardingSphere website, 

1. Install Hugo `0.83.1` extended.
2. Clone [shardingsphere-doc](https://github.com/apache/shardingsphere-doc.git) beside this repository and check out its `asf-site` branch.
3. From this repository root, run `bash ../shardingsphere-doc/.github/scripts/prepare-learn-themes.sh docs`, then `bash docs/build.sh` to generate `html` files at `docs/target/`.
4. Overwrite `document/current` with `docs/target/document/current`, `community` with `docs/target/community` and `blog` with `docs/target/blog`.
5. Commit changes and raise a PR for [shardingsphere-doc](https://github.com/apache/shardingsphere-doc.git).

### Note:

1. If you modify `docs/build.sh`, please test it locally.

### Learn themes

`shardingsphere-doc` prepares the Learn themes before building the website.
[`themes.lock`](themes.lock) pins each site's upstream commit and archive SHA-256 from [matcornic/hugo-theme-learn](https://github.com/matcornic/hugo-theme-learn).
The downloaded `document/themes/hugo-theme-learn` and `community/themes/hugo-theme-learn` directories are generated dependencies and are ignored by Git.
Running the preparation script replaces these directories with the complete, unmodified upstream snapshots.
Historical releases without `themes.lock` keep using their bundled themes.

Maintain project templates in each site's `layouts`, static resources in `static`, and archetypes in `archetypes`.
Hugo loads these project files before the matching theme files, so customizations must not be made in the downloaded theme directories.
The overrides preserve the existing navigation, version and language selectors, theme switching, search, diagrams, and styling.
Existing font files are retained byte for byte in `static/fonts` and `static/webfonts` where they differ from upstream; font repair and Hugo upgrades are separate changes.
When upgrading a pinned theme, review the overrides against the new upstream snapshot and compare the complete generated website before updating the lock file.
The Learn license for retained theme-derived files is in [`learn-theme-LICENSE`](learn-theme-LICENSE).
The blog's Tale theme remains bundled.

## HOW to insert a video of Bilibili or YouTube?

1. First please make sure you get the correct url of the video you want to add.

2. - DO REMEMBER use the `bvid argument` of bilibili url:

     for example : https://www.bilibili.com/video/BV1aE411X7kQ?from=search&seid=5978719442257399675 ( bvid argument means `BV1aE411X7kQ` , or you can simply recognize the argument with `Capital Letter` )

   - YouTube url is much easier to recognize : https://www.youtube.com/watch?v=2MsN8gpT6jY , the argument you needed is `2MsN8gpT6jY` at the end of the url.

3. Use the shortcodes at wherever you want to place:

   - `{{< bilibili BV1aE411X7kQ >}}`

   - `{{< youtube 2MsN8gpT6jY >}}`
