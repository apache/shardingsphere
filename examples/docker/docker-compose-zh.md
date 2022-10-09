## 使用docker-compose初始化开始环境

在开始使用docker compose之前，根据下述参考网址安装docker和docker-compose：https://docs.docker.com/compose/install/

#### ShardingSphere-JDBC

1. 运行 'cd docker/shardingsphere-jdbc/sharding'，进入 docker 文件夹
2. 运行 'docker-compose up -d'，启动 docker compose 环境
3. 根据需要，开启 mysql/etcd/zookeeper
4. 如果有端口冲突，在 docker-compose.yml 中修改相应的端口，然后再次使用 'docker-compose up -d' 启动 docker compose
5. 如果需要关闭程序，请使用命令 'docker-compose down'

#### ShardingSphere-Proxy

1. 运行 'cd docker/shardingsphere-proxy/sharding'，进入 docker 文件夹
2. 运行 'docker-compose up -d'，启动 docker compose 环境
3. 运行 `psql -d sharding_db -h 127.0.0.1 -U root -p 3308 -W` 登录代理, 示例的默认密码：root
4. 如果有端口冲突，在docker-compose.yml中修改相应的端口，然后再次使用 'docker-compose up -d'启动docker compose
5. 如果需要关闭程序，请使用命令 'docker-compose down'

需要注意，请谨慎使用 docker 删除指令`docker ps -a -q`去删除docker容器。
