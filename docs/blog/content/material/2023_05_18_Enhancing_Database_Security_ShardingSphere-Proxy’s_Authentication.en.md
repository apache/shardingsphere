+++
title = "Enhancing Database Security: ShardingSphere-Proxy’s Authentication"
weight = 99
chapter = true 
+++

ShardingSphere-Proxy is a transparent database proxy that supports interaction with any client using MySQL, PostgreSQL, or openGauss protocols.

Proxy provides user authentication and can adapt to different authentication modes for various database protocols. However, there is a question that has been rarely addressed and has almost no corresponding issue even in the Github community:

“How does ShardingSphere-Proxy authenticate its clients?”


**Note:** this post only discusses password authentication, and does not include non-password authentication methods such as UDS (Unix Domain Socket).

![img](https://shardingsphere.apache.org/blog/img/2023_05_18_Enhancing_Database_Security_ShardingSphere-Proxy’s_Authentication.en.md1.jpeg)


# Background Info

To begin, let’s take a look at how several common databases authenticate their users.

# MySQL

Taking `MySQL 5.7` as an example, you can easily understand the authentication interaction between the client and the server as follows:

- The client and the server achieve protocol handshake, including negotiating which authentication protocol to use. The default value is `mysql_native_password`.

- The server generates 20-byte random data and sends it to the client.

- Based on the random data, the client encrypts passwords entered by users, then sends the encrypted information to the server for password verification. [1]

![img](https://shardingsphere.apache.org/blog/img/2023_05_18_Enhancing_Database_Security_ShardingSphere-Proxy’s_Authentication.en.md2.jpeg)

MySQL Native Authentication Process

Above is a brief description of MySQL Native Authentication. It can help us understand what happens once users enter passwords.

To adapt to different scenarios, MySQL provides multiple authentication protocols in a plugin form [2].

- `mysql_native_password`: native authentication, used as the default before version 8.0.

- `caching_sha2_password`: SHA-256-based cache authentication, used as the default after version 8.0.

- `mysql_clear_password`: clear text password authentication, suitable for certain scenarios.

MySQL enterprise versions also provide authentication plugins such as:

- `authentication_windows`: Windows service-based authentication.

- `authentication_ldap_simple`: LDAP-based authentication.

Now let’s take a look at the authentication mechanisms of PostgreSQL and openGauss.

**PostgreSQL**
Common authentication methods for PostgreSQL [3] include:

- `scram-sha-256`: SHA-256 authentication based on SCRAM (Salted Challenge Response Authentication Mechanism).
- `md5`: using MD5 encryption.
- `password`: using clear text passwords.

**openGauss**
Common authentication methods for openGauss [4] include：

- `scram-sha-256`: SHA-256 authentication based on SCRAM.
- `md5`: using MD5 encryption.
- `sm3`: using SM3 encryption.


# Overview

As a powerful database proxy, ShardingSphere-Proxy supports multiple database protocols and provides user authentication through its AuthenticationEngine.

![img](https://shardingsphere.apache.org/blog/img/2023_05_18_Enhancing_Database_Security_ShardingSphere-Proxy’s_Authentication.en.md3.jpeg)

The goal of AuthenticationEngine is to achieve protocol handshake and identity authentication.

ShardingSphere-Proxy supports handshake and authentication protocols for MySQL, PostgreSQL and openGauss, and provides multiple authentication algorithms, including:

## MySQL

- `mysql_native_password`
- `mysql_clear_password`

## PostgreSQL

- `md5`
- `password`

## openGauss

- `md5`
- `scram-sha-256`


Please note that in Proxy, the default authentication algorithms for MySQL, PostgreSQL and openGauss are `mysql_native_password`, `md5` and `scram-sha-256`, respectively.

# ShardingSphere-Proxy Authentication Configuration

In version 5.3.2, ShardingSphere added authenticator-related configuration items to allow users to specify authentication algorithms as needed when using Proxy. The format is as follows:

```
authority:
  users:
    - user: # Combination of the authorized host and the username used to log in to the computer node. Format：<username>@<hostname>. When hostname is % an empty string, it indicates no limit to the authorized host. 
      password: # User password.
      authenticationMethodName: # Optional, used to specify the password authentication method for users.
  authenticators: # Optional, no configuration is required by default. Proxy is automatically selected based on the front-end protocol type.
    authenticatorName:
      type: # Password authentication type.
  defaultAuthenticator: # It is optional that you can specify an authenticatorName as the default password authentication method.
  privilege:
    type: # Type of Authority provider, with a default value of ALL_PERMITTED.
```

`AuthenticationMethodName`, `authenticators` and `defaultAuthenticator` are all optional and only configured when needed.

Proxy also supports user-level authentication configuration, where users can use different authentication algorithms.

Now, let’s take `openGauss` as an example to explain how to use the newly-added MD5 authentication algorithm to log in to psql.

# Preparation

Before making specific configurations, we compare the performance of `gsql` and `psql` connecting to Proxy.

## server.yaml

```
authority:
  users:
    - user: root@%
      password: root
    - user: sharding
      password: sharding

props:
  proxy-frontend-database-protocol-type: openGauss
```

**Note:**

- We specify `openGaussas` the front-end protocol.

- No specified authentication type. Proxy adopts the default value of `scram-sha-256`.

## config-sharding.yaml

```
databaseName: sharding_db

dataSources:
  ds_0:
    url: jdbc:opengauss://127.0.0.1:15432/demo_ds
    username: username
    password: password
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 10
    minPoolSize: 1
```


## gsql login
Connection succeeded.

![img](https://shardingsphere.apache.org/blog/img/2023_05_18_Enhancing_Database_Security_ShardingSphere-Proxy’s_Authentication.en.md4.jpeg)

Here we use gsql in opengauss:3.1.0 to access ShardingSphere-Proxy.

## psql login
Connection failed.

![img](https://shardingsphere.apache.org/blog/img/2023_05_18_Enhancing_Database_Security_ShardingSphere-Proxy’s_Authentication.en.md5.jpeg)

We see that it’s actually due to the authentication protocol.

The psql client requires md5 protocol authentication by default, but because Proxy requires the scram-sha-256 under the openGuass protocol, the negotiation fails and an exception is thrown.

## Following Steps

Now we specify MD5 as the authentication method for sharding users, while retaining the default setting of using `scram-sha-256` to support different users and clients.

**server.yaml**

```
authority:
  users:
    - user: root@%
      password: root
    - user: sharding
      password: sharding
      authenticationMethodName: md5
  authenticators:
    md5:
      type: MD5
    scram_sha256:
      type: SCRAM_SHA256 # SCRAM_SHA256 is the SPI name that provides scram-sha-256 authentication alogorithm
  defaultAuthenticator: scram_sha256

props:
  proxy-frontend-database-protocol-type: openGauss
```

**Notes：**

- We specify `openGaussas` the front-end protocol.
- We specify `MD5` as the authentication algorithm for sharding users.
- The specified default authentication remains `scram-sha-256`, which means root users require `scram-sha-256` authentication.

## config-sharding.yaml
Remains unchanged.

## gsql login
Connection succeeded.

![img](https://shardingsphere.apache.org/blog/img/2023_05_18_Enhancing_Database_Security_ShardingSphere-Proxy’s_Authentication.en.md6.jpeg)

## psql login as root user
Connection failed.

![img](https://shardingsphere.apache.org/blog/img/2023_05_18_Enhancing_Database_Security_ShardingSphere-Proxy’s_Authentication.en.md7.jpeg)

Psql fails to connect because no `scram-sha-256` authentication mechanism is supported. Then what about the sharding user?


## psql login as sharding user
Connection succeeded.

![img](https://shardingsphere.apache.org/blog/img/2023_05_18_Enhancing_Database_Security_ShardingSphere-Proxy’s_Authentication.en.md8.jpeg)

Now we see that `psql` has successfully connected to ShardingSphere-Proxy under the openGuass protocol.

![img](https://shardingsphere.apache.org/blog/img/2023_05_18_Enhancing_Database_Security_ShardingSphere-Proxy’s_Authentication.en.md9.jpeg)

# Future Plans

ShardingSphere has already implemented a framework in Proxy for different database protocols and authentication algorithms. We have also provided optional authentication algorithms for several database protocols. In the future, we aim to expand our support for authentication algorithms across a wider range of database protocols.

We believe that our community is the key to making ShardingSphere better. We welcome more people to join us and contribute to the development of the project.

# Summary

In this post, we discussed the configuration of authentication protocols for ShardingSphere-Proxy. For more information on this topic, please refer to the official documentation on our website [5].

If you have any questions or suggestions about Apache ShardingSphere, please feel free to raise them in the GitHub issue list [6], or visit our Slack community [7] for further discussion.


# Useful Links

[1] [MySQL Native Authentication](https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_connection_phase_authentication_methods_native_password_authentication.html)

[2] [MySQL Pluggable Authentication](https://dev.mysql.com/doc/refman/8.0/en/pluggable-authentication.html)

[3] [PostgreSQL Password Authentication](https://www.postgresql.org/docs/15/auth-password.html)

[4] [openGauss Configuration File Reference](https://docs.opengauss.org/en/docs/3.1.1/docs/Developerguide/configuration-file-reference.html)

[5] [ShardingSphere-Proxy Authentication and Authorization](https://shardingsphere.apache.org/document/5.3.2/en/user-manual/shardingsphere-proxy/yaml-config/authority/)

[6] [GitHub Issue List](https://github.com/apache/shardingsphere/issues)

[7] [Slack Community](https://apacheshardingsphere.slack.com/?redir=%2Fssb%2Fredirect)









