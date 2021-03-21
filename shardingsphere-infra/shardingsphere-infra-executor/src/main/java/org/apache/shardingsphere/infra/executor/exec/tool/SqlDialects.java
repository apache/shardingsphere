package org.apache.shardingsphere.infra.executor.exec.tool;

import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.dialect.AnsiSqlDialect;
import org.apache.calcite.sql.dialect.MysqlSqlDialect;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;

public class SqlDialects {
    
    /**
     * Convert <code>DatabaseType</code> to Calcite <code>SqlDialect</code>.
     * @param databaseType databaseType
     * @return <code>SqlDialect</code>
     */
    public static SqlDialect toSqlDialect(final DatabaseType databaseType) {
        if (databaseType instanceof MySQLDatabaseType) {
            return MysqlSqlDialect.DEFAULT;
        } else {
            return AnsiSqlDialect.DEFAULT;
        }
    }
}
