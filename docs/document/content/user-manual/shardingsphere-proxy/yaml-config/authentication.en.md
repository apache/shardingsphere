+++
title = "Authority"
weight = 1
+++

It is used to set up initial user to login compute node, and authority data of storage node.

## Configuration Item Explanation

```yaml
rules:
  - !AUTHORITY
    users:
      - # Username, authorized host and password for compute node. Format: <username>@<hostname>:<password>, hostname is % or empty string means do not care about authorized host
    provider:
      type: # authority provider for storage node
```

## Example

```yaml
rules:
  - !AUTHORITY
    users:
      - root@localhost:root
      - my_user@pwd
    provider:
      type: FOO_AUTHORITY_PROVIDER
```

Refer to [Authority Provider](/en/dev-manual/proxy) for more implementations.
