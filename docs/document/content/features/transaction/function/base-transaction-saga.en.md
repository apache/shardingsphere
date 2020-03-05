+++
pre = "<b>3.4.2.3 </b>"
toc = true
title = "BASE Transaction saga"
weight = 3
+++

## Function

* Fully support cross-database transactions.
* Re-try failed SQL and try to deliver it.
* Support reverted SQL, update snapshot auto-generation and auto-compensation.
* Use relational databases to take snapshot and persist transaction log; support SPI to load other persistent data.

## Unsupported

* Not support resource isolation for now.
* Not support automatic recovery of commit and rollback after the service is down for now.

## Support

Our BASE transaction has implemented [Saga](https://www.cs.cornell.edu/andru/cs711/2002fa/reading/sagas.pdf) transaction through third party SPI and uses [Servicecomb-Saga](https://github.com/apache/servicecomb-saga-actuator) as Saga engine.

#### Notice

- Reverted SQL requires a **primary key**, please make sure it is defined in table structure.
- For `INSERT` statements, **primary key value** needs to be shown in SQL, such as `INSERT INTO ${table_name} (id, value, ...) VALUES (11111, '', ....) (id means table primary key)`.
- ShardingSphere distributed primary key can be used to automatically generate the primary key.