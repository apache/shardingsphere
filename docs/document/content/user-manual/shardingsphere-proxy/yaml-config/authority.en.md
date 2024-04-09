+++
title = "Authentication & Authorization"
weight = 1
+++

## Background

In ShardingSphere-Proxy, user authentication and authorization information is configured through `authority`.

Thanks to ShardingSphere's pluggable architecture, Proxy provides two levels of privilege providers, namely: 

- `ALL_PERMITTED`: each user has all privileges without special authorization.
- `DATABASE_PERMITTED`: grants the user privileges on the specified logical databases, defined by `user-database-mappings`.

The administrator can choose which privilege provider to use as needed when configuring `authority`. 

## Parameters

```yaml
authority:
  users:
    - user: # Specify the username, and authorized host for logging in to the compute node. Format: <username>@<hostname>. When the hostname is % or an empty string, it indicates that the authorized host is not limited, username and hostname are case-insensitive
      password: # Password
      admin: # Optional, administrator identity. If true, the user has the highest authority. The default value is false
      authenticationMethodName: # Optional, used to specify the password authentication method for the user
  authenticators: # Optional, not required by default, Proxy will automatically choose the authentication method according to the frontend protocol type
    authenticatorName:
      type: # Authentication method type
  defaultAuthenticator: # Optional, specify an authenticator as the default password authentication method
  privilege:
    type: # Privilege provider type. The default value is ALL_PERMITTED
```

## Sample

### Minimalist configuration

```yaml
authority:
   users:
     - user: root@%
       password: root
     - user: sharding
       password: sharding
```

Explanation:
- Two users are defined: `root@%` and `sharding`;
- `authenticationMethodName` is not specified for `root@127.0.0.1`, Proxy will automatically choose the authentication method according to the frontend protocol;
- Privilege provider is not specified, the default `ALL_PERMITTED` will be used;


### Authentication configuration

The custom authentication configuration allows users to greater leeway to set their own custom configurations according to their scenarios. 
Taking `openGauss` as the frontend protocol type as an example, its default authentication method is `scram-sha-256`.
If the user `sharding` needs to use an old version of the psql client (which does not support `scram-sha-256`) to connect to the Proxy, the administrator may allow sharding to use the `md5` method for password authentication.
The configuration is as follows:

```yaml
authority:
   users:
     - user: root@127.0.0.1
       password: root
     - user: sharding
       password: sharding
       authenticationMethodName: md5
   authenticators:
     md5:
       type: MD5
   privilege:
     type: ALL_PERMITTED
```

Explanation:
- Two users are defined: `root@127.0.0.1` and `sharding`;
- Use `MD5` method for password authentication for `sharding`;
- Authentication method is not specified for `root@127.0.0.1`, Proxy will automatically choose one according to the frontend protocol;
- The privilege provider `ALL_PERMITTED` is specified.

### Authorization configuration

#### ALL_PERMITTED

```yaml
authority:
  users:
    - user: root@127.0.0.1
      password: root
    - user: sharding
      password: sharding
  privilege:
    type: ALL_PERMITTED
```

Explanation:
- Two users are defined: `root@127.0.0.1` and `sharding`;
- `authenticators` and `authenticationMethodName` are not defined, Proxy will automatically choose the authentication method according to the frontend protocol;
- The privilege provider `ALL_PERMITTED` is specified.

#### DATABASE_PERMITTED

```yaml
authority:
  users:
    - user: root@127.0.0.1
      password: root
    - user: sharding
      password: sharding
  privilege:
    type: DATABASE_PERMITTED
    props:
      user-database-mappings: root@127.0.0.1=*, sharding@%=test_db, sharding@%=sharding_db
```

Explanation:
- Two users are defined: `root@127.0.0.1` and `sharding`;
- `authenticators` and `authenticationMethodName` are not defined, Proxy will automatically choose the authentication method according to the frontend protocol;
- The privilege provider `DATABASE_PERMITTED` is specified, authorize `root@127.0.0.1` to access all logical databases (`*`), and user `sharding` can only access `test_db` and `sharding_db`.

## Related References

Please refer to [Authority Provider](/en/user-manual/shardingsphere-proxy/yaml-config/authority/) for the specific implementation of authority provider.
