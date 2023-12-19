package org.apache.shardingsphere.test.it.sql.parser.it.sqlserver.internal;

import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.sql.parser.api.SQLStatementVisitorEngine;
import org.apache.shardingsphere.sql.parser.core.ParseASTNode;
import org.apache.shardingsphere.sql.parser.core.database.parser.SQLParserExecutor;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.jupiter.api.Test;

/**
 * Example generator entrance.
 */
public class SQLServerParserTest {


    /**
     * Main entrance.
     *
     */
    @Test
    public void testExecuteSQL() {
        String sql = "INSERT INTO iris_rx_data (\"Sepal.Length\", \"Sepal.Width\", \"Petal.Length\", \"Petal.Width\" , \"Species\")\n" +
                "EXECUTE sp_execute_external_script\n" +
                "  @language = N'R'\n" +
                "  , @script = N'iris_data <- iris'";
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "SQLServer");
        SQLParserExecutor sqlParserExecutor = new SQLParserExecutor(databaseType);
        ParseASTNode parse = sqlParserExecutor.parse(sql);

        SQLStatement visit = new SQLStatementVisitorEngine(databaseType, true).visit(parse);

        System.out.println(JsonUtils.toJsonString(visit));
    }
}
