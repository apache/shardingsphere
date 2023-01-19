package org.apache.shardingsphere.test.e2e.transaction.cases.settype;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.e2e.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.test.e2e.transaction.engine.base.TransactionBaseE2EIT;
import org.apache.shardingsphere.test.e2e.transaction.engine.base.TransactionTestCase;
import org.apache.shardingsphere.test.e2e.transaction.engine.constants.TransactionTestConstants;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Transaction type holder test case.
 */
@Slf4j
@TransactionTestCase(adapters = TransactionTestConstants.JDBC, transactionTypes = TransactionType.XA)
public final class TransactionTypeHolderTestCase extends BaseTransactionTestCase {
    
    public TransactionTypeHolderTestCase(final TransactionBaseE2EIT baseTransactionITCase, final DataSource dataSource) {
        super(baseTransactionITCase, dataSource);
    }
    
    @Override
    protected void executeTest() throws SQLException {
        assertThat(TransactionTypeHolder.get(), is(TransactionType.XA));
        try (Connection connection = getDataSource().getConnection()) {
            TransactionTypeHolder.set(TransactionType.LOCAL);
            connection.setAutoCommit(false);
            connection.rollback();
        } finally {
            TransactionTypeHolder.set(TransactionType.XA);
            assertThat(TransactionTypeHolder.get(), is(TransactionType.XA));
        }
    }
}
