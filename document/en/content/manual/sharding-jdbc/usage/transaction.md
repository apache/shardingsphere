+++
toc = true
title = "B.A.S.E"
weight = 5
+++

## Best effort delivery

### Add maven dependency

```xml
<dependency>
    <groupId>io.shardingjdbc</groupId>
    <artifactId>sharding-jdbc-transaction</artifactId>
    <version>${sharding-jdbc.version}</version>
</dependency>
```

### Example

```java
    // 1. To configure SoftTransactionConfiguration
    SoftTransactionConfiguration transactionConfig = new SoftTransactionConfiguration(dataSource);
    transactionConfig.setXXX();
    
    // 2. To initialize SoftTransactionManager
    SoftTransactionManager transactionManager = new SoftTransactionManager(transactionConfig);
    transactionManager.init();
    
    // 3. To get BEDSoftTransaction
    BEDSoftTransaction transaction = (BEDSoftTransaction) transactionManager.getTransaction(SoftTransactionType.BestEffortsDelivery);
    
    // 4. To start a transaction
    transaction.begin(connection);
    
    // 5. To execute JDBC
    /* 
        codes here
    */
    
    // 6. To close the connection
    transaction.end();
```

## TCC

TODO
