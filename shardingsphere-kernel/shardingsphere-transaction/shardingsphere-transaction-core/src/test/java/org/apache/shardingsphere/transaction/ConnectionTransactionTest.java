package org.apache.shardingsphere.transaction;

import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.transaction.ConnectionTransaction.DistributedTransactionOperationType;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public final class ConnectionTransactionTest {

    private ConnectionTransaction connectionTransaction;

    @Before
    public void init() {
        Map<String, ShardingSphereTransactionManagerEngine> actualEngines = Collections.singletonMap(DefaultSchema.LOGIC_NAME, new ShardingSphereTransactionManagerEngine());
        TransactionContexts transactionContexts = new TransactionContexts(actualEngines);
        connectionTransaction = new ConnectionTransaction(
                DefaultSchema.LOGIC_NAME,
                new TransactionRule(new TransactionRuleConfiguration("XA", "Atomikos")),
                transactionContexts
        );
    }

    @Test
    public void assertDistributedTransactionOperationTypeIgnore() {
        DistributedTransactionOperationType operationType = connectionTransaction.getDistributedTransactionOperationType(false);
        assertThat(operationType, equalTo(DistributedTransactionOperationType.IGNORE));
    }
}
