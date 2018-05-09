#!/bin/bash
cd `dirname $0`

rm -rf target
mkdir target

cp -rf homepage/. target

mkdir -p target/document/legacy/1.x
mkdir -p target/document/legacy/2.x

cp -rf docs_2.x_cn target/document/legacy/2.x/cn
cp -rf docs_2.x_en target/document/legacy/2.x/en
cp -rf docs_1.x target/document/legacy/1.x

cd document/cn
hugo
cd ..
cd ..
mv document/cn/public target/document/cn

cd document/en
hugo
cd ..
cd ..
mv document/en/public target/document/en

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

