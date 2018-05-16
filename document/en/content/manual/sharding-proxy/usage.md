+++
pre = "<b>4.2.1. </b>"
toc = true
title = "User Manual"
weight = 1
+++

1. Download the latest version of Sharding-Proxy from https://github.com/sharding-sphere/sharding-sphere-doc/raw/master/dist/sharding-proxy-3.0.0.M1.tar.gz
1. Modify the conf/sharding-config.yaml file after decompression, and configure the sharding rule. Please reference [Configuration Manual](/manual/sharding-proxy/configuration/).
1. Run `bin/start.sh` on Linux, or `bin/start.bat` on Windows to start Sharding-Proxy.
1. Connect to it by means of any client tools, e.g. `mysql -u root -h 127.0.0.1 -P3307`

## Notices

1. The default port of Sharding-Proxy is 3307, user can change it by passing the port number on startup script, e.g. `bin/start.sh 3308`.
1. There is only one logical data source in Sharding-Proxy, named `sharding_db`.
1. Currently, Sharding-Proxy does not support authorization, therefore you can login with any username and password to connected.
