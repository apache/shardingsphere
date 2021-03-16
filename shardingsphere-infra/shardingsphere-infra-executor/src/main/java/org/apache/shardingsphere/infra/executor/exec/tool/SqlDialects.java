package org.apache.shardingsphere.infra.executor.exec.tool;

import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.dialect.AnsiSqlDialect;
import org.apache.calcite.sql.dialect.MysqlSqlDialect;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;

public class SqlDialects {
    
    public static SqlDialect toSqlDialect(DatabaseType databaseType) {
        if(databaseType instanceof MySQLDatabaseType) {
            return MysqlSqlDialect.DEFAULT;
        } else {
            return AnsiSqlDialect.DEFAULT;
        }
    }
}
