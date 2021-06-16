package org.apache.shardingsphere.distsql.parser.test;

import org.apache.shardingsphere.test.sql.parser.parameterized.engine.SQLParserParameterizedTest;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.sql.SQLCaseType;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

@RunWith(Parameterized.class)
public class DistSQLStatementParserParameterizedTest extends SQLParserParameterizedTest {

    public DistSQLStatementParserParameterizedTest(final String sqlCaseId, final String databaseType, final SQLCaseType sqlCaseType) {
        super(sqlCaseId, databaseType, sqlCaseType);
    }

    @Parameterized.Parameters(name = "{0} ({2}) -> {1}")
    public static Collection<Object[]> getTestParameters() {
        return SQLParserParameterizedTest.getTestParameters("DistSQL");
    }
}
