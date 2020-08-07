targetDir="content"
localDir="source"
mkdir $localDir
# 默认的语言
lang="$1"
if [[ "$lang" == "en" ]] ;then
    sed -i "s/language = 'zh_CN'/language = 'en_US'/" conf.py 
    echo "printing English version PDF"
else
    sed -i "s/language = 'en_US'/language = 'zh_CN'/" conf.py 
    echo "printing Chinese version PDF"
fi

cp conf.py ${localDir}/
# 复制所有源文件的内容
cp -r ${targetDir}/* ${localDir}/
cd $localDir
# faq去除编号
sed -i 's/##[ ][0-9]*./##/g' faq/*

# 建立主目录的index.rst
echo -e ".. toctree::\n   :maxdepth: 1\n   :titlesonly:\n" >> index.rst

# 遍历所有的md
for f in `find . -type f -name "*${lang}.md"`; do
    title=`grep -oP '^title = "\K[^"]*' $f` # 文件的标题
    weight=`grep -oP '^weight = \K.*' $f` # 文件的权重（代表文件排版的顺序）
    sed -i -n '/+++/,/+++/!p' $f # 删除front matter
    fileName=${f##*/} # 文件名
    path=${f%/*} # 文件路径
    lastpath=${path%/*} # 上一级路径
    foldername=${path##*/} # 当前文件夹名
    # 如果是index文件则
    if [[ "${fileName}" == "_index.${lang}.md" ]]
    then
    # 将文件的权重写到上一级别的filelist文件中
    echo -e "${weight}\t${foldername}/index" >> "${lastpath}/filelist.txt"
    # 当前目录下新建一个index.rst
    echo "============================" >> "${path}/index.rst"
    echo $title >> "${path}/index.rst"
    echo "============================" >> "${path}/index.rst"
    echo -e ".. toctree::\n   :maxdepth: 1\n   :titlesonly:\n\n   _index.${lang}.md" >> "${path}/index.rst"
    else
    # 不是的话添加标题行
    sed -i "1i # ${title}" $f
    # 将序号和文件名保存到filelist文件
    echo -e "${weight}\t${fileName}" >> "${path}/filelist.txt"
    fi
done


# 所有的filelist进行排序后输出到index.rst中
for f in `find . -type f -name "*list.txt"`; do
    path=${f%/*}
    sort -nk 1  $f | awk '{print $2}' | sed 's/^/   /g' >> "${path}/index.rst"
    rm $f
done

# 转换成rst
for f in `find . -type f -name "*${lang}.md"`; do
    sed -i -n '/+++/,/+++/!p' $f
    # 删除所有的图标行因为不能排版
    sed -i /http.*svg/d $f
    sed -i /http.*codacy/d $f
    pandoc $f -o "${f}.rst"
    rm $f
done

cd ..
make latexpdf
cp _build/latex/*.pdf static/pdf/shadingsphere_docs_${lang}.pdf
echo "shadingsphere_docs_${lang}.pdf"
make clean
rm -rf {_build,source}