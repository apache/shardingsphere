package org.apache.shardingsphere.sql.parser.sql.dialect.statement.clickhouse;

import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

/**
 * @author zzypersonally@gmail.com
 * @description ClickHouseStatement
 * @since 2024/5/11 11:38
 */
public interface ClickHouseStatement extends SQLStatement {



    @Override
    default DatabaseType getDatabaseType() {
        return TypedSPILoader.getService(DatabaseType.class, "ClickHouse");
    }
}
