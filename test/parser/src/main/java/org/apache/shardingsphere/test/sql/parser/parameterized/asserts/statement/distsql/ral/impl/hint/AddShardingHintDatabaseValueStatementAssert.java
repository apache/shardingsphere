package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.ral.impl.hint;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sharding.distsql.parser.statement.hint.AddShardingHintDatabaseValueStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.ral.AddShardingHintDatabaseValueStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Add sharding hint database statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AddShardingHintDatabaseValueStatementAssert {

    /**
     * Assert add sharding hint database value statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual add sharding hint database value statement
     * @param expected expected add sharding hint database value statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final AddShardingHintDatabaseValueStatement actual, final AddShardingHintDatabaseValueStatementTestCase expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual statement should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual statement should exist."), actual);
            assertThat(actual.getLogicTableName(), is(expected.getLogicTableName()));
            assertThat(actual.getShardingValue(), is(expected.getShardingValue()));
        }
    }
}
