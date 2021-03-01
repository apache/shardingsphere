## Using docker-compose to config startup environment

before we use docker compose, please install docker and docker-compose first : https://docs.docker.com/compose/install/

#### ShardingSphere-JDBC

1. access the docker folder (cd docker/shardingsphere-jdbc/sharding)
2. launch the environment by docker compose (docker-compose up -d)
3. access mysql / etcd / zookeeper as you want
4. if there is conflict on port, just modify the corresponding port defined in docker-compose.yml and then launch docker compose again(docker-compose up -d)
5. if you want to stop the environment, use command docker-compose down

#### ShardingSphere-Proxy

1. access the docker folder (cd docker/shardingsphere-proxy/sharding)
2. launch the environment by docker compose (docker-compose up -d)
3. access proxy by `mysql -h127.0.0.1 -P13308 -proot -uroot`
4. if there is conflict on port, just modify the corresponding port defined in docker-compose.yml and then launch docker compose again(docker-compose up -d)
5. if you want to stop the environment, use command docker-compose down

to clean the docker container , you could use docker rm `docker ps -a -q` (be careful)
