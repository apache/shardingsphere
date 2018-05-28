#!/bin/bash
cd `dirname $0`

rm -rf target
mkdir target

cp -rf homepage/. target

mkdir -p target/document/legacy/1.x
mkdir -p target/document/legacy/2.x

cp -rf document/legacy/2.x/cn target/document/legacy/2.x/cn
cp -rf document/legacy/2.x/en target/document/legacy/2.x/en
cp -rf document/legacy/1.x/cn target/document/legacy/1.x/cn

cd document/current
hugo

find ./ -name '*.html' -exec sed -i -e 's|[[:space:]]*<option id="\([a-zA-Z]\+\)" value="|<option id="\1" value="/document/current|g' {} \;

cd public/en
sed -i -e 's/cn/current\/en/g' index.html
cd ../..

cd public/cn
sed -i -e 's/cn/current\/cn/g' index.html
cd ../..

cd ..
cd ..
mv document/current/public target/document/current

mkdir target/community

cd community/cn
hugo
cd ..
cd ..
mv community/cn/public target/community/cn

cd community/en
hugo
cd ..
cd ..
mv community/en/public target/community/en

