+++
toc = true
title = "柔性事务"
weight = 4
+++

### 操作指南

```java
    // 1. 配置SoftTransactionConfiguration
    SoftTransactionConfiguration transactionConfig = new SoftTransactionConfiguration(dataSource);
    transactionConfig.setXXX();
    
    // 2. 初始化SoftTransactionManager
    SoftTransactionManager transactionManager = new SoftTransactionManager(transactionConfig);
    transactionManager.init();
    
    // 3. 获取BEDSoftTransaction
    BEDSoftTransaction transaction = (BEDSoftTransaction) transactionManager.getTransaction(SoftTransactionType.BestEffortsDelivery);
    
    // 4. 开启事务
    transaction.begin(connection);
    
    // 5. 执行JDBC
    /* 
        codes here
    */
    * 
    // 6.关闭事务
    transaction.end();
```
