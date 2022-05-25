package org.apache.shardingsphere.data.pipeline.spi.fixture;

import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.shardingsphere.data.pipeline.spi.ddlgenerator.DialectDDLGenerator;

public class DialectDDLGeneratorFixTrue implements DialectDDLGenerator {
    private static final String SHOW_CREATE_SQL = "SHOW CREATE TABLE %s";

    @Override
    public String generateDDLSQL(final String tableName, final String schemaName, final DataSource dataSource) throws SQLException {

        StringBuilder sb = new StringBuilder();
        sb.append(String.format(SHOW_CREATE_SQL, tableName));
        return sb.toString();
    }

    @Override
    public String getType() {
        return "MySQL";
    }
}
