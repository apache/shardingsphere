#!/bin/bash
cd `dirname $0`

rm -rf target
mkdir target

cp -rf homepage/. target

cp -rf docs_cn/public target/docs_cn

cp -rf docs_en/public target/docs_en

cp -rf docs_1.x/public target/docs_1.x

mkdir target/document

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

