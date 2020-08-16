package org.apache.shardingsphere.encrypt.merge.dql.fixture;

import org.apache.shardingsphere.sql.parser.binder.segment.table.TablesContext;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.type.TableAvailable;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;

import java.util.Collection;

/**
 * @author Nianjun Sun
 */
public class TableAvailableAndSqlStatementContextFixture implements TableAvailable, SQLStatementContext {
    
    @Override
    public SQLStatement getSqlStatement() {
        return null;
    }

    @Override
    public Collection<SimpleTableSegment> getAllTables() {
        return null;
    }

    @Override
    public TablesContext getTablesContext() {
        return null;
    }
}
