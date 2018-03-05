#!/bin/bash
cd `dirname $0`

rm -rf target
mkdir target

cp -rf homepage/. target

cd docs_cn
hugo
cd ..
mv docs_cn/public target/docs_cn

cd docs_en
hugo
cd ..
mv docs_en/public target/docs_en

cd docs_1.x
hugo
cd ..
mv docs_1.x/public target/docs_1.x

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
