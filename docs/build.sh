#!/bin/bash
cd `dirname $0`

rm -rf target

mkdir -p target/document/master
cd document
hugo --cleanDestinationDir
find ../document/public/ -name '*.html' -exec sed -i -e 's|[[:space:]]*<option id="\([a-zA-Z]\+\)" value="|<option id="\1" value="/document/master|g' {} \;
cd public/en
sed -i -e 's/cn/en/g' index.html
cd ../..
cd ..
mv document/public/* target/document/master/

mkdir target/community
cd community
hugo --cleanDestinationDir
find ../community/public/ -name '*.html' -exec sed -i -e 's|[[:space:]]*<option id="\([a-zA-Z]\+\)" value="|<option id="\1" value="/community|g' {} \;
cd public/en
sed -i -e 's/cn/en/g' index.html
cd ../..
cd ..
mv community/public/* target/community/

mkdir target/blog
cd blog
hugo --cleanDestinationDir
find ../blog/public/ -name '*.html' -exec sed -i -e 's|[[:space:]]*<option id="\([a-zA-Z]\+\)" value="|<option id="\1" value="/blog|g' {} \;
cd public/en
sed -i -e 's/cn/en/g' index.html
cd ../..
cd ..
mv blog/public/* target/blog/
