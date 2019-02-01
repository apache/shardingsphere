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

package io.shardingsphere.shardingproxy.backend;

import com.google.common.base.Optional;
import io.shardingsphere.core.metadata.table.TableMetaData;
import io.shardingsphere.core.metadata.table.TableMetaDataFactory;
import io.shardingsphere.core.parsing.antlr.sql.statement.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antlr.sql.statement.ddl.CreateTableStatement;
import io.shardingsphere.core.parsing.antlr.sql.statement.ddl.DropTableStatement;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.shardingproxy.runtime.schema.LogicSchema;
import io.shardingsphere.shardingproxy.transport.mysql.constant.ServerErrorCode;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.shardingproxy.transport.mysql.packet.generic.ErrPacket;

import java.sql.SQLException;

/**
 * Abstract backend handler.
 *
 * @author zhangliang
 */
public abstract class AbstractBackendHandler implements BackendHandler {
    
    /**
     * default execute implement for adapter.
     *
     * @return command response packets
     */
    @Override
    public CommandResponsePackets execute() {
        try {
            return execute0();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            Optional<SQLException> sqlException = findSQLException(ex);
            return sqlException.isPresent()
                    ? new CommandResponsePackets(new ErrPacket(1, sqlException.get())) : new CommandResponsePackets(new ErrPacket(1, ServerErrorCode.ER_STD_UNKNOWN_EXCEPTION, ex.getMessage()));
        }
    }
    
    protected abstract CommandResponsePackets execute0() throws Exception;
    
    private Optional<SQLException> findSQLException(final Exception exception) {
        if (exception instanceof SQLException) {
            return Optional.of((SQLException) exception);
        }
        if (null == exception.getCause()) {
            return Optional.absent();
        }
        if (exception.getCause() instanceof SQLException) {
            return Optional.of((SQLException) exception.getCause());
        }
        if (null == exception.getCause().getCause()) {
            return Optional.absent();
        }
        if (exception.getCause().getCause() instanceof SQLException) {
            return Optional.of((SQLException) exception.getCause().getCause());
        }
        return Optional.absent();
    }
    
    @Override
    public boolean next() throws SQLException {
        return false;
    }
    
    @Override
    public ResultPacket getResultValue() throws SQLException {
        return null;
    }
    
    protected final void refreshTableMetaData(final LogicSchema logicSchema, final SQLStatement sqlStatement) {
        if (sqlStatement instanceof CreateTableStatement) {
            refreshTableMetaData(logicSchema, (CreateTableStatement) sqlStatement);
        } else if (sqlStatement instanceof AlterTableStatement) {
            refreshTableMetaData(logicSchema, (AlterTableStatement) sqlStatement);
        } else if (sqlStatement instanceof DropTableStatement) {
            refreshTableMetaData(logicSchema, (DropTableStatement) sqlStatement);
        }
    }
    
    private void refreshTableMetaData(final LogicSchema logicSchema, final CreateTableStatement createTableStatement) {
        logicSchema.getMetaData().getTable().put(createTableStatement.getTables().getSingleTableName(), TableMetaDataFactory.newInstance(createTableStatement));
    }
    
    private void refreshTableMetaData(final LogicSchema logicSchema, final AlterTableStatement alterTableStatement) {
        String logicTableName = alterTableStatement.getTables().getSingleTableName();
        TableMetaData newTableMetaData = TableMetaDataFactory.newInstance(alterTableStatement, logicSchema.getMetaData().getTable().get(logicTableName));
        Optional<String> newTableName = alterTableStatement.getNewTableName();
        if (newTableName.isPresent()) {
            logicSchema.getMetaData().getTable().put(newTableName.get(), newTableMetaData);
            logicSchema.getMetaData().getTable().remove(logicTableName);
        } else {
            logicSchema.getMetaData().getTable().put(logicTableName, newTableMetaData);
        }
    }
    
    private void refreshTableMetaData(final LogicSchema logicSchema, final DropTableStatement dropTableStatement) {
        for (String each : dropTableStatement.getTables().getTableNames()) {
            logicSchema.getMetaData().getTable().remove(each);
        }
    }
}
