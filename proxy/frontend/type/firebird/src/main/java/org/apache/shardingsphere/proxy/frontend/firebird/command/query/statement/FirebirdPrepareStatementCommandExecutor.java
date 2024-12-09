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
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.info.type.common.FirebirdCommonInfoPacketType;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoPacketType;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoReturnValue;
import org.apache.shardingsphere.db.protocol.firebird.packet.command.query.statement.FirebirdPrepareStatementPacket;
import org.apache.shardingsphere.db.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
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
import java.util.Collection;
import java.util.Collections;

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
        return createResponse(sqlStatementContext);
    }

    private int getStatementId() {
        if (packet.isValidStatementHandle()) {
            return packet.getStatementId();
        }
        int transactionId = FirebirdTransactionIdGenerator.getInstance().getTransactionId(connectionSession.getConnectionId());
        return FirebirdStatementIdGenerator.getInstance().getStatementId(transactionId);
    }
    
    private Collection<DatabasePacket> createResponse(final SQLStatementContext sqlStatementContext) {
        ByteBuf data = buffer(packet.getMaxLength());
        for (int i = 0; i < packet.getInfoItems().size(); i++) {
            switch (packet.getInfoItems().get(i)) {
                case STMT_TYPE:
                    data.writeByte(FirebirdSQLInfoPacketType.STMT_TYPE.getCode());
                    data.writeShortLE(4);
                    data.writeIntLE(getFirebirdStatementType(sqlStatementContext.getSqlStatement()));
                    break;
                case SELECT:
                    data.writeByte(FirebirdSQLInfoPacketType.SELECT.getCode());
                    i = processDescribe(sqlStatementContext, i, data);
                    break;
                case BIND:
                    data.writeByte(FirebirdSQLInfoPacketType.BIND.getCode());
                    i = processDescribe(sqlStatementContext, i, data);
                    break;
                default:
                    throw new FirebirdProtocolException("Unknown statement info request type %d", packet.getInfoItems().get(i));
            }
        }
        data.writeByte(FirebirdCommonInfoPacketType.END.getCode());
    return Collections.singleton(new FirebirdGenericResponsePacket().setData(data.capacity(data.writerIndex()).array()));
    }

    private int getFirebirdStatementType(final SQLStatement statement) {
        if (statement instanceof SelectStatement) {
            return FirebirdSQLInfoReturnValue.SELECT.getCode();
        }
        if (statement instanceof InsertStatement) {
            return FirebirdSQLInfoReturnValue.INSERT.getCode();
        }
        if (statement instanceof UpdateStatement) {
            return FirebirdSQLInfoReturnValue.UPDATE.getCode();
        }
        if (statement instanceof DeleteStatement) {
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

    private int processDescribe(SQLStatementContext sqlStatementContext, int idx, ByteBuf buffer) {
        for (int i = ++idx; i < packet.getInfoItems().size(); i++) {
            switch (packet.getInfoItems().get(i)) {
                case DESCRIBE_VARS:
                    buffer.writeByte(FirebirdSQLInfoPacketType.DESCRIBE_VARS.getCode());
                    break;
                case SQLDA_SEQ:
                case TYPE:
                case SUB_TYPE:
                case SCALE:
                case LENGTH:
                case FIELD:
                case ALIAS:
                case RELATION:
                case RELATION_ALIAS:
                case OWNER:
                    //TODO process describe types
                    break;
                case DESCRIBE_END:
                    buffer.writeShortLE(4);
                    buffer.writeIntLE(0);
                    return i;
                default:
                    throw new FirebirdProtocolException("Unknown statement info request type %d", packet.getInfoItems().get(i));
            }
        }
        return 0;
    }
}
