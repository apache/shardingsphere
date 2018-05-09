#!/bin/bash
cd `dirname $0`

rm -rf target
mkdir target

cp -rf homepage/. target

mkdir target/document

mkdir target/document/docs_2.x
cp -rf docs_cn target/document/docs_2.x/cn
cp -rf docs_en target/document/docs_2.x/en

cp -rf docs_1.x target/document/docs_1.x

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

