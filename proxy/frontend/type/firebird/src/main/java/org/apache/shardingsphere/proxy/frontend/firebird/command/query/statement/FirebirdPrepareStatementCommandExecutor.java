/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement;

import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.db.protocol.firebird.exception.FirebirdProtocolException;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.info.FirebirdInfoPacketType;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.info.type.common.FirebirdCommonInfoPacketType;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoPacketType;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoReturnValue;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.statement.FirebirdPrepareStatementPacket;
import org.apache.shardingsphere.db.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.TableAvailable;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.reflection.ReflectionUtils;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.FirebirdServerPreparedStatement;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdTransactionIdGenerator;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.tcl.RollbackStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.tcl.SavepointStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.tcl.StartTransactionStatement;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.netty.buffer.Unpooled.buffer;

/**
 * Firebird prepare transaction command executor
 */
@RequiredArgsConstructor
public final class FirebirdPrepareStatementCommandExecutor implements CommandExecutor {

    private final FirebirdPrepareStatementPacket packet;
    private final ConnectionSession connectionSession;

    @Override
    public Collection<DatabasePacket> execute() throws SQLException {
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        SQLParserRule sqlParserRule = metaDataContexts.getMetaData().getGlobalRuleMetaData().getSingleRule(SQLParserRule.class);
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "Firebird");
        SQLStatement sqlStatement = sqlParserRule.getSQLParserEngine(databaseType).parse(packet.getSQL(), true);
        SQLStatementContext sqlStatementContext = new SQLBindEngine(metaDataContexts.getMetaData(),
                connectionSession.getCurrentDatabaseName(), packet.getHintValueContext()).bind(sqlStatement, Collections.emptyList());
        int statementId = getStatementId();
        FirebirdServerPreparedStatement serverPreparedStatement = new FirebirdServerPreparedStatement(packet.getSQL(), sqlStatementContext, packet.getHintValueContext());
        connectionSession.getServerPreparedStatementRegistry().addPreparedStatement(statementId, serverPreparedStatement);
        return createResponse(sqlStatementContext, metaDataContexts);
    }

    private int getStatementId() {
        if (packet.isValidStatementHandle()) {
            return packet.getStatementId();
        }
        int transactionId = FirebirdTransactionIdGenerator.getInstance().getTransactionId(connectionSession.getConnectionId());
        return FirebirdStatementIdGenerator.getInstance().getStatementId(transactionId);
    }
    
    private Collection<DatabasePacket> createResponse(final SQLStatementContext sqlStatementContext, MetaDataContexts metaDataContexts) {
        ByteBuf data = buffer(packet.getMaxLength());
        int statementType = getFirebirdStatementType(sqlStatementContext.getSqlStatement());
        while (packet.nextItem()) {
            switch (packet.getCurrentItem()) {
                case STMT_TYPE:
                    writeInt(FirebirdSQLInfoPacketType.STMT_TYPE, statementType, data);
                    break;
                case SELECT:
                    writeCode(FirebirdSQLInfoPacketType.SELECT, data);
                    if (FirebirdSQLInfoReturnValue.isSelectDescribable(statementType)) {
                        processDescribe(sqlStatementContext, metaDataContexts, data, true);
                    } else {
                        skipDescribe(data);
                    }
                    break;
                case BIND:
                    writeCode(FirebirdSQLInfoPacketType.BIND, data);
                    if (FirebirdSQLInfoReturnValue.isBindDescribable(statementType)) {
                        processDescribe(sqlStatementContext, metaDataContexts, data, false);
                    } else {
                        skipDescribe(data);
                    }
                    break;
                default:
                    throw new FirebirdProtocolException("Unknown statement info request type %d", packet.getCurrentItem());
            }
        }
        writeCode(FirebirdCommonInfoPacketType.END, data);
    return Collections.singleton(new FirebirdGenericResponsePacket().setData(data.capacity(data.writerIndex()).array()));
    }
    
    private void writeCode(FirebirdInfoPacketType code, ByteBuf buffer) {
        buffer.writeByte(code.getCode());
    }
    
    private void writeInt(FirebirdInfoPacketType code, int value, ByteBuf buffer) {
        buffer.writeByte(code.getCode());
        buffer.writeShortLE(4);
        buffer.writeIntLE(value);
    }
    
    private void writeString(FirebirdInfoPacketType code, String value, ByteBuf buffer) {
        buffer.writeByte(code.getCode());
        byte[] valueBytes = value.getBytes(packet.getCharset());
        buffer.writeShortLE(valueBytes.length);
        buffer.writeBytes(valueBytes);
    }

    private int getFirebirdStatementType(final SQLStatement statement) {
        if (statement instanceof SelectStatement) {
            return FirebirdSQLInfoReturnValue.SELECT.getCode();
        }
        if (statement instanceof InsertStatement) {
            if (((InsertStatement) statement).getReturningSegment().isPresent()) {
                return FirebirdSQLInfoReturnValue.EXEC_PROCEDURE.getCode();
            }
            return FirebirdSQLInfoReturnValue.INSERT.getCode();
        }
        if (statement instanceof UpdateStatement) {
            if (((UpdateStatement) statement).getReturningSegment().isPresent()) {
                return FirebirdSQLInfoReturnValue.EXEC_PROCEDURE.getCode();
            }
            return FirebirdSQLInfoReturnValue.UPDATE.getCode();
        }
        if (statement instanceof DeleteStatement) {
            if (((DeleteStatement) statement).getReturningSegment().isPresent()) {
                return FirebirdSQLInfoReturnValue.EXEC_PROCEDURE.getCode();
            }
            return FirebirdSQLInfoReturnValue.DELETE.getCode();
        }
        if (statement instanceof DDLStatement) {
            return FirebirdSQLInfoReturnValue.DDL.getCode();
        }
        if (statement instanceof StartTransactionStatement) {
            return FirebirdSQLInfoReturnValue.START_TRANS.getCode();
        }
        if (statement instanceof CommitStatement) {
            return FirebirdSQLInfoReturnValue.COMMIT.getCode();
        }
        if (statement instanceof RollbackStatement) {
            return FirebirdSQLInfoReturnValue.ROLLBACK.getCode();
        }
        if (statement instanceof SavepointStatement) {
            return FirebirdSQLInfoReturnValue.SAVEPOINT.getCode();
        }
        return 0;
    }
    
    private void skipDescribe(ByteBuf buffer) {
        while (packet.getCurrentItem() != FirebirdSQLInfoPacketType.DESCRIBE_END) {
            packet.nextItem();
        }
        writeInt(FirebirdSQLInfoPacketType.DESCRIBE_VARS, 0, buffer);
    }

    private void processDescribe(SQLStatementContext sqlStatementContext, MetaDataContexts metaDataContexts, ByteBuf buffer, boolean returnAll) {
        //TODO add exception if the first item is not DESCRIBE_VARS
        packet.nextItem();
        List<FirebirdSQLInfoPacketType> requestedItems = new ArrayList<>(11);
        while (packet.nextItem()) {
            requestedItems.add(packet.getCurrentItem());
            if (packet.getCurrentItem() == FirebirdSQLInfoPacketType.DESCRIBE_END) {
                ByteBuf describeBuffer = buffer(packet.getMaxLength());
                int count = processInfoItems(sqlStatementContext, metaDataContexts, describeBuffer, returnAll, requestedItems);
                writeInt(FirebirdSQLInfoPacketType.DESCRIBE_VARS, count, buffer);
                buffer.writeBytes(describeBuffer, describeBuffer.readableBytes());
                return;
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private int processInfoItems(SQLStatementContext sqlStatementContext, MetaDataContexts metaDataContexts, ByteBuf buffer, boolean returnAll, List<FirebirdSQLInfoPacketType> requestedItems) {
//        if (!(sqlStatementContext.getSqlStatement() instanceof InsertStatement)) {
//            return 0;
//        }
        String databaseName = connectionSession.getCurrentDatabaseName();
        String schemaName = new DatabaseTypeRegistry(sqlStatementContext.getDatabaseType()).getDefaultSchemaName(databaseName);
        Collection<String> tableNames = ((TableAvailable) sqlStatementContext).getTablesContext().getTableNames();
        Optional<Object> columnNames = ReflectionUtils.getFieldValueByGetMethod(sqlStatementContext, "getColumnNames");
        List<String> affectedColumns = columnNames.map(columns -> (List<String>) columns).orElseGet(() -> new ArrayList<>(0));
        int columnCount = 0;
        for (String tableName : tableNames) {
            ShardingSphereTable table = metaDataContexts.getMetaData().getDatabase(databaseName).getSchema(schemaName).getTable(tableName);
            for (ShardingSphereColumn column : table.getAllColumns()) {
                if (!returnAll && !affectedColumns.contains(column.getName().toLowerCase())) {
                    continue;
                }
                columnCount++;
                processColumn(buffer, requestedItems, table, column, columnCount);
            }
        }
        return columnCount;
    }
    
    private void processColumn(ByteBuf buffer, List<FirebirdSQLInfoPacketType> requestedItems, ShardingSphereTable table, ShardingSphereColumn column, int idx) {
        //SQLDA_SEQ uses 1-based index
        for (FirebirdSQLInfoPacketType requestedItem : requestedItems) {
            switch (requestedItem) {
                case SQLDA_SEQ:
                    writeInt(FirebirdSQLInfoPacketType.SQLDA_SEQ, idx, buffer);
                    break;
                case TYPE:
                    writeInt(FirebirdSQLInfoPacketType.TYPE, FirebirdBinaryColumnType.valueOfJDBCType(column.getDataType()).getValue() + 1, buffer);
                    break;
                case SUB_TYPE:
                    writeInt(FirebirdSQLInfoPacketType.SUB_TYPE, 0, buffer);
                    break;
                case SCALE:
                    writeInt(FirebirdSQLInfoPacketType.SCALE, 0, buffer);
                    break;
                case LENGTH:
                    writeInt(FirebirdSQLInfoPacketType.LENGTH, FirebirdBinaryColumnType.valueOfJDBCType(column.getDataType()).getLength(), buffer);
                    break;
                case FIELD:
                    writeString(FirebirdSQLInfoPacketType.FIELD, column.getName(), buffer);
                    break;
                case ALIAS:
                    writeString(FirebirdSQLInfoPacketType.ALIAS, column.getName(), buffer);
                    break;
                case RELATION:
                    writeString(FirebirdSQLInfoPacketType.RELATION, table.getName(), buffer);
                    break;
                case RELATION_ALIAS:
                    writeString(FirebirdSQLInfoPacketType.RELATION_ALIAS, "", buffer);
                    break;
                case OWNER:
                    writeString(FirebirdSQLInfoPacketType.OWNER, connectionSession.getConnectionContext().getGrantee().getUsername(), buffer);
                    break;
                case DESCRIBE_END:
                    writeCode(FirebirdSQLInfoPacketType.DESCRIBE_END, buffer);
                    break;
                default:
                    throw new FirebirdProtocolException("Unknown statement info request type %d", requestedItem);
            }
        }
    }
}
