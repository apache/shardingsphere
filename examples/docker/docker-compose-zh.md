## Using docker-compose to config startup environment
## 使用docker-compose去初始化开始环境
before we use docker compose, please install docker and docker-compose first : https://docs.docker.com/compose/install/
在开始使用docker compose之前，根据下述参考网址安装docker和docker-compose

#### sharding-jdbc
1. access the docker folder (cd docker/sharding-jdbc/sharding)
2. launch the environment by docker compose (docker-compose up -d)
3. access mysql / etcd / zookeeper as you want
4. if there is conflict on port, just modify the corresponding port defined in docker-compose.yml and then launch docker compose again(docker-compose up -d)
5. if you want to stop these environment, use command docker-compose down

#### sharding-jdbc
1. 运行'cd docker/sharding-jdbc/sharding'，进入docker文件夹
2. 运行'docker-compose up -d'，启动docker compose环境
3. 根据需要，开启mysql / etcd / zookeeper
4. 如果有端口冲突，在docker-compose.yml中修改相应的端口，然后再次使用'docker-compose up -d'启动docker compose
5. 如果需要关闭相关程序环境，使用命令'docker-compose down'

#### sharding-proxy
1. access the docker folder (cd docker/sharding-proxy/sharding)
2. launch the environment by docker compose (docker-compose up -d)
3. access proxy by `mysql -h127.0.0.1 -P13308 -proot -uroot`
4. if there is conflict on port, just modify the corresponding port defined in docker-compose.yml and then launch docker compose again(docker-compose up -d)
5. if you want to stop these environment, use command docker-compose down

to clean the docker container , you could use docker rm `docker ps -a -q` (be careful)

#### sharding-proxy
1. 运行'cd docker/sharding-jdbc/sharding'，进入docker文件夹
2. 运行'docker-compose up -d'，启动docker compose环境
3. 运行 `mysql -h127.0.0.1 -P13308 -proot -uroot`登录代理
4. 如果有端口冲突，在docker-compose.yml中修改相应的端口，然后再次使用'docker-compose up -d'启动docker compose
5. 如果需要关闭相关程序环境，使用命令'docker-compose down'

谨慎使用docker删除指令`docker ps -a -q`去删除docker容器
