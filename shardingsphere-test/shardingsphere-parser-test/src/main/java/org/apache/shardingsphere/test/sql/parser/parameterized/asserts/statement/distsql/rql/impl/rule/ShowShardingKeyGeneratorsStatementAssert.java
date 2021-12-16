package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.statement.distsql.rql.impl.rule;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingKeyGeneratorsStatement;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.distsql.SchemaAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.distsql.rql.ShowShardingKeyGeneratorsStatementTestCase;

import static org.junit.Assert.assertTrue;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShowShardingKeyGeneratorsStatementAssert {

    public static void assertIs(final SQLCaseAssertContext assertContext, final ShowShardingKeyGeneratorsStatement actual,
                                final ShowShardingKeyGeneratorsStatementTestCase expected){
        assertTrue(assertContext.getText("Actual schema should exist."), actual.getSchema().isPresent());
        SchemaAssert.assertIs(assertContext,actual.getSchema().get(),expected.getSchema());
    }

}
