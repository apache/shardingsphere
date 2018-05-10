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

