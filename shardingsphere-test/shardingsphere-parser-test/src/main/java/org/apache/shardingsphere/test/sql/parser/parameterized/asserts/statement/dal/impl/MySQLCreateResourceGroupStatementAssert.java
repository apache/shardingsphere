package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.dal.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLCreateResourceGroupStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dal.CreateResourceGroupStatementTestCase;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Create resource group statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLCreateResourceGroupStatementAssert {

    /**
     * Assert create resource group statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual create resource group statement
     * @param expected expected create resource group statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final MySQLCreateResourceGroupStatement actual, final CreateResourceGroupStatementTestCase expected) {
        assertNotNull("expected create resource group should be not null", expected.getGroup());
        assertThat(actual.getName(), is(expected.getGroup().getName()));
    }
}
