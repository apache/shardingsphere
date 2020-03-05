## Using docker-compose to config startup environment
before we use docker compose, please install docker and docker-compose first : https://docs.docker.com/compose/install/

#### sharding-jdbc
1. access the docker folder (cd docker/sharding-jdbc/sharding)
2. launch the environment by docker compose (docker-compose up -d)
3. access mysql / etcd / zookeeper as you want
4. if there is conflict on port, just modify the corresponding port defined in docker-compose.yml and then launch docker compose again(docker-compose up -d)
5. if you want to stop these environment, use command docker-compose down

#### sharding-proxy
1. access the docker folder (cd docker/sharding-proxy/sharding)
2. launch the environment by docker compose (docker-compose up -d)
3. access proxy by `mysql -h127.0.0.1 -P13308 -proot -uroot`
4. if there is conflict on port, just modify the corresponding port defined in docker-compose.yml and then launch docker compose again(docker-compose up -d)
5. if you want to stop these environment, use command docker-compose down

to clean the docker container , you could use docker rm `docker ps -a -q` (be careful)
