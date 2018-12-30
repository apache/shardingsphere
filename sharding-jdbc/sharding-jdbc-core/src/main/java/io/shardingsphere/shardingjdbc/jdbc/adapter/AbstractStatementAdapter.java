/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingjdbc.jdbc.adapter;

import com.google.common.base.Optional;
import io.shardingsphere.core.metadata.table.TableMetaData;
import io.shardingsphere.core.metadata.table.TableMetaDataFactory;
import io.shardingsphere.core.parsing.antlr.sql.statement.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antlr.sql.statement.ddl.CreateTableStatement;
import io.shardingsphere.core.parsing.antlr.sql.statement.ddl.DropTableStatement;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.shardingjdbc.jdbc.adapter.executor.ForceExecuteCallback;
import io.shardingsphere.shardingjdbc.jdbc.adapter.executor.ForceExecuteTemplate;
import io.shardingsphere.shardingjdbc.jdbc.core.ShardingContext;
import io.shardingsphere.shardingjdbc.jdbc.unsupported.AbstractUnsupportedOperationStatement;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Collection;

/**
 * Adapter for {@code Statement}.
 * 
 * @author zhangliang
 * @author gaohongtao
 * @author yangyi
 */
@RequiredArgsConstructor
public abstract class AbstractStatementAdapter extends AbstractUnsupportedOperationStatement {
    
    private final Class<? extends Statement> targetClass;
    
    private boolean closed;
    
    private boolean poolable;
    
    private int fetchSize;
    
    private final ForceExecuteTemplate<Statement> forceExecuteTemplate = new ForceExecuteTemplate<>();
    
    @SuppressWarnings("unchecked")
    @Override
    public final void close() throws SQLException {
        closed = true;
        try {
            forceExecuteTemplate.execute((Collection) getRoutedStatements(), new ForceExecuteCallback<Statement>() {
        
                @Override
                public void execute(final Statement statement) throws SQLException {
                    statement.close();
                }
            });
        } finally {
            getRoutedStatements().clear();
        }
    }
    
    @Override
    public final boolean isClosed() {
        return closed;
    }
    
    @Override
    public final boolean isPoolable() {
        return poolable;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public final void setPoolable(final boolean poolable) throws SQLException {
        this.poolable = poolable;
        recordMethodInvocation(targetClass, "setPoolable", new Class[] {boolean.class}, new Object[] {poolable});
        forceExecuteTemplate.execute((Collection) getRoutedStatements(), new ForceExecuteCallback<Statement>() {
            
            @Override
            public void execute(final Statement statement) throws SQLException {
                statement.setPoolable(poolable);
            }
        });
    }
    
    @Override
    public final int getFetchSize() {
        return fetchSize;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public final void setFetchSize(final int rows) throws SQLException {
        this.fetchSize = rows;
        recordMethodInvocation(targetClass, "setFetchSize", new Class[] {int.class}, new Object[] {rows});
        forceExecuteTemplate.execute((Collection) getRoutedStatements(), new ForceExecuteCallback<Statement>() {
            
            @Override
            public void execute(final Statement statement) throws SQLException {
                statement.setFetchSize(rows);
            }
        });
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public final void setEscapeProcessing(final boolean enable) throws SQLException {
        recordMethodInvocation(targetClass, "setEscapeProcessing", new Class[] {boolean.class}, new Object[] {enable});
        forceExecuteTemplate.execute((Collection) getRoutedStatements(), new ForceExecuteCallback<Statement>() {
            
            @Override
            public void execute(final Statement statement) throws SQLException {
                statement.setEscapeProcessing(enable);
            }
        });
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public final void cancel() throws SQLException {
        forceExecuteTemplate.execute((Collection) getRoutedStatements(), new ForceExecuteCallback<Statement>() {
        
            @Override
            public void execute(final Statement statement) throws SQLException {
                statement.cancel();
            }
        });
    }
    
    @Override
    public final int getUpdateCount() throws SQLException {
        long result = 0;
        boolean hasResult = false;
        for (Statement each : getRoutedStatements()) {
            int updateCount = each.getUpdateCount();
            if (updateCount > -1) {
                hasResult = true;
            }
            result += updateCount;
        }
        if (result > Integer.MAX_VALUE) {
            result = Integer.MAX_VALUE;
        }
        return hasResult ? Long.valueOf(result).intValue() : -1;
    }
    
    @Override
    public final SQLWarning getWarnings() {
        return null;
    }
    
    @Override
    public final void clearWarnings() {
    }
    
    @Override
    public final boolean getMoreResults() throws SQLException {
        boolean result = false;
        for (Statement each : getRoutedStatements()) {
            result = each.getMoreResults();
        }
        return result;
    }
    
    @Override
    public final boolean getMoreResults(final int current) {
        return false;
    }
    
    @Override
    public final int getMaxFieldSize() throws SQLException {
        return getRoutedStatements().isEmpty() ? 0 : getRoutedStatements().iterator().next().getMaxFieldSize();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public final void setMaxFieldSize(final int max) throws SQLException {
        recordMethodInvocation(targetClass, "setMaxFieldSize", new Class[] {int.class}, new Object[] {max});
        forceExecuteTemplate.execute((Collection) getRoutedStatements(), new ForceExecuteCallback<Statement>() {
            
            @Override
            public void execute(final Statement statement) throws SQLException {
                statement.setMaxFieldSize(max);
            }
        });
    }
    
    // TODO Confirm MaxRows for multiple databases is need special handle. eg: 10 statements maybe MaxRows / 10
    @Override
    public final int getMaxRows() throws SQLException {
        return getRoutedStatements().isEmpty() ? -1 : getRoutedStatements().iterator().next().getMaxRows();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public final void setMaxRows(final int max) throws SQLException {
        recordMethodInvocation(targetClass, "setMaxRows", new Class[] {int.class}, new Object[] {max});
        forceExecuteTemplate.execute((Collection) getRoutedStatements(), new ForceExecuteCallback<Statement>() {
            
            @Override
            public void execute(final Statement statement) throws SQLException {
                statement.setMaxRows(max);
            }
        });
    }
    
    @Override
    public final int getQueryTimeout() throws SQLException {
        return getRoutedStatements().isEmpty() ? 0 : getRoutedStatements().iterator().next().getQueryTimeout();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public final void setQueryTimeout(final int seconds) throws SQLException {
        recordMethodInvocation(targetClass, "setQueryTimeout", new Class[] {int.class}, new Object[] {seconds});
        forceExecuteTemplate.execute((Collection) getRoutedStatements(), new ForceExecuteCallback<Statement>() {
        
            @Override
            public void execute(final Statement statement) throws SQLException {
                statement.setQueryTimeout(seconds);
            }
        });
    }
    
    protected abstract Collection<? extends Statement> getRoutedStatements();
    
    protected final void refreshTableMetaData(final ShardingContext shardingContext, final SQLStatement sqlStatement) {
        if (sqlStatement instanceof CreateTableStatement) {
            refreshTableMetaData(shardingContext, (CreateTableStatement) sqlStatement);
        } else if (sqlStatement instanceof AlterTableStatement) {
            refreshTableMetaData(shardingContext, (AlterTableStatement) sqlStatement);
        } else if (sqlStatement instanceof DropTableStatement) {
            refreshTableMetaData(shardingContext, (DropTableStatement) sqlStatement);
        }
    }
    
    private void refreshTableMetaData(final ShardingContext shardingContext, final CreateTableStatement createTableStatement) {
        shardingContext.getMetaData().getTable().put(createTableStatement.getTables().getSingleTableName(), TableMetaDataFactory.newInstance(createTableStatement));
    }
    
    private void refreshTableMetaData(final ShardingContext shardingContext, final AlterTableStatement alterTableStatement) {
        String logicTableName = alterTableStatement.getTables().getSingleTableName();
        TableMetaData newTableMetaData = TableMetaDataFactory.newInstance(alterTableStatement, shardingContext.getMetaData().getTable().get(logicTableName));
        Optional<String> newTableName = alterTableStatement.getNewTableName();
        if (newTableName.isPresent()) {
            shardingContext.getMetaData().getTable().put(newTableName.get(), newTableMetaData);
            shardingContext.getMetaData().getTable().remove(logicTableName);
        } else {
            shardingContext.getMetaData().getTable().put(logicTableName, newTableMetaData);
        }
    }
    
    private void refreshTableMetaData(final ShardingContext shardingContext, final DropTableStatement dropTableStatement) {
        for (String each : dropTableStatement.getTables().getTableNames()) {
            shardingContext.getMetaData().getTable().remove(each);
        }
    }
}
