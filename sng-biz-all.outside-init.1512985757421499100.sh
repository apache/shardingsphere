
#!/bin/bash
set -e

ampregistry_ip=$(ping -q -c 1 -t 1 ampregistry | grep PING | sed -e "s/).*//" | sed -e "s/.*(//")
if [ -z $ampregistry_ip ];then
	echo ">> 未能获取ampregistry地址，请检查/etc/hosts中是否设置ampregistry"
else
	echo ">> 本机ampregistry的地址是$ampregistry_ip, 将会在从这台机器获取image"
	if [ "10.201.102.123" != $ampregistry_ip ]; then
		echo ">> 检测到本机ampregistry地址($ampregistry_ip)与devops-manager所配置的ampregistry(10.201.102.123)不一致，是否继续(继续可能造成image内容与预期不符)"
		read -p "yes|no:" continue
		if [ $continue = "yes" ] || [ $continue = "y" ]; then
			echo ">> 在$ampregistry_ip 上继续做image pull操作"
		else
			echo ">> 中断退出"
			exit 0
		fi
	fi
fi

echo ">> 将要对一下image进行操作："

echo ampregistry:5000/sng-biz-openfire-init-1:1.0.171208



docker pull ampregistry:5000/sng-biz-openfire-init-1:1.0.171208



docker save -o sng-biz-all.outside-init-1512985757421499100.tar  ampregistry:5000/sng-biz-openfire-init-1:1.0.171208 
echo ">> 生成完毕，文件是:sng-biz-all.outside-init-1512985757421499100.tar"
