+++
pre = "<b>4.2.1. </b>"
toc = true
title = "User Manual"
weight = 1
+++

1. Download the latest version of Sharding-Proxy from https://github.com/sharding-sphere/sharding-sphere-doc/raw/master/dist/sharding-proxy-3.0.0.M1.tar.gz
1. If using docker，execute command `docker pull shardingsphere/sharding-proxy` to get image. More details please reference[Docker Image](/en/manual/sharding-proxy/docker/).
1. Modify the conf/config.yaml file after decompression, and configure the sharding rule and master-slave rule. Please reference [Configuration Manual](/en/manual/sharding-proxy/configuration/). If you want
to use self-defined configuration file, 
1. Run `bin/start.sh` on Linux, or `bin/start.bat` on Windows to start Sharding-Proxy. If you want to set port and configuration file, please refer to [quick-start](/en/quick-start/sharding-proxy-quick-start/).
1. Connect to it by means of any client tools, e.g. `mysql -u root -h 127.0.0.1 -P3307`

## Notices

1. The default port of Sharding-Proxy is 3307, user can change it by passing the port number on startup script, e.g. `bin/start.sh 3308`.
1. The default configuration file is conf/config.yaml, user can can change it by passing the file name on startup script, e.g. `bin/start.sh 3308 config_master_slave.yaml`
1. There is only one logical data source in Sharding-Proxy, named `sharding_db`.
1. You can configure the username and password in conf/config.yaml for Sharding-Proxy, therefore you can login with the username and password to connected.
