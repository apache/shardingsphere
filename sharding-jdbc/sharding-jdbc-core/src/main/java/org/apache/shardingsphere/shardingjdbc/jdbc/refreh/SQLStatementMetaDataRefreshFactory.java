package org.apache.shardingsphere.shardingjdbc.jdbc.refreh;

import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.ddl.AlterTableStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.ddl.CreateIndexStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.ddl.DropIndexStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.ddl.DropTableStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The type Sql statement meta data refresh factory.
 */
public final class SQLStatementMetaDataRefreshFactory {
    
    private static final Map<Class<?>, SQLStatementMetaDataRefresh<? extends SQLStatement>> REFRESH_MAP = new HashMap<>();
    
    static {
        REFRESH_MAP.put(CreateTableStatementContext.class, new CreateTableStatementMetaDataRefresh());
        REFRESH_MAP.put(AlterTableStatementContext.class, new AlterTableStatementMetaDataRefresh());
        REFRESH_MAP.put(DropTableStatementContext.class, new DropTableStatementMetaDataRefresh());
        REFRESH_MAP.put(CreateIndexStatementContext.class, new CreateIndexStatementMetaDataRefresh());
        REFRESH_MAP.put(DropIndexStatementContext.class, new DropIndexStatementMetaDataRefresh());
    }
    
    /**
     * New instance sql statement meta data refresh.
     *
     * @param sqlStatementContext the sql statement context
     * @return the sql statement meta data refresh
     */
    public static Optional<SQLStatementMetaDataRefresh<? extends SQLStatement>> newInstance(final SQLStatementContext sqlStatementContext) {
        return Optional.ofNullable(REFRESH_MAP.get(sqlStatementContext.getClass()));
    }
}
