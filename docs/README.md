# ShardingSphere Documents

To build ShardingSphere website by means of [hugo](http://gohugo.io/overview/introduction/) and [hugo theme learn](https://github.com/matcornic/hugo-theme-learn).

Follow the steps below to deploy ShardingSphere website, 

1. Execute `docs/build.sh` to generate `html` files at the directory of `docs/target/`.
2. Clone [shardingsphere-doc](https://github.com/apache/shardingsphere-doc.git).
3. Checkout to `asf-site` branch.
3. Overwrite `document/current` with `docs/target/document/current` and `community` with `docs/target/community`.
4. Commit changes and raise a PR for [shardingsphere-doc](https://github.com/apache/shardingsphere-doc.git).
