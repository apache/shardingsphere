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

package org.apache.shardingsphere.proxy.frontend.mysql.command;

import lombok.SneakyThrows;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.MySQLCommandPacketType;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.admin.initdb.MySQLComInitDbPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.close.MySQLComStmtClosePacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.execute.MySQLComStmtExecutePacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.prepare.MySQLComStmtPreparePacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.reset.MySQLComStmtResetPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.text.fieldlist.MySQLComFieldListPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.text.query.MySQLComQueryPacket;
import org.apache.shardingsphere.db.protocol.packet.CommandPacket;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.SchemaContext;
import org.apache.shardingsphere.infra.context.impl.StandardSchemaContexts;
import org.apache.shardingsphere.infra.context.runtime.RuntimeContext;
import org.apache.shardingsphere.infra.context.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.schema.ProxySchemaContexts;
import org.apache.shardingsphere.proxy.frontend.mysql.command.admin.initdb.MySQLComInitDbExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.admin.ping.MySQLComPingExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.admin.quit.MySQLComQuitExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.close.MySQLComStmtCloseExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.execute.MySQLComStmtExecuteExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.prepare.MySQLComStmtPrepareExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.reset.MySQLComStmtResetExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.text.fieldlist.MySQLComFieldListPacketExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.text.query.MySQLComQueryPacketExecutor;
import org.apache.shardingsphere.rdl.parser.engine.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class MySQLCommandExecutorFactoryTest {
    
    @Test
    @SneakyThrows(ReflectiveOperationException.class)
    public void assertNewInstance() {
        Field schemaContexts = ProxySchemaContexts.getInstance().getClass().getDeclaredField("schemaContexts");
        schemaContexts.setAccessible(true);
        schemaContexts.set(ProxySchemaContexts.getInstance(),
                new StandardSchemaContexts(getSchemaContextMap(), new Authentication(), new ConfigurationProperties(new Properties()), new MySQLDatabaseType()));
        BackendConnection backendConnection = mock(BackendConnection.class);
        when(backendConnection.getSchema()).thenReturn("schema");
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_QUIT,
            mock(CommandPacket.class), backendConnection), instanceOf(MySQLComQuitExecutor.class));
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_INIT_DB,
            mock(MySQLComInitDbPacket.class), backendConnection), instanceOf(MySQLComInitDbExecutor.class));
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_FIELD_LIST,
            mock(MySQLComFieldListPacket.class), backendConnection), instanceOf(MySQLComFieldListPacketExecutor.class));
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_QUERY,
            mock(MySQLComQueryPacket.class), backendConnection), instanceOf(MySQLComQueryPacketExecutor.class));
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_STMT_PREPARE,
            mock(MySQLComStmtPreparePacket.class), backendConnection), instanceOf(MySQLComStmtPrepareExecutor.class));
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_STMT_EXECUTE,
            mock(MySQLComStmtExecutePacket.class), backendConnection), instanceOf(MySQLComStmtExecuteExecutor.class));
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_STMT_RESET,
            mock(MySQLComStmtResetPacket.class), backendConnection), instanceOf(MySQLComStmtResetExecutor.class));
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_STMT_CLOSE,
            mock(MySQLComStmtClosePacket.class), backendConnection), instanceOf(MySQLComStmtCloseExecutor.class));
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_PING,
            mock(CommandPacket.class), backendConnection), instanceOf(MySQLComPingExecutor.class));
    }
    
    private Map<String, SchemaContext> getSchemaContextMap() {
        SchemaContext result = mock(SchemaContext.class);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        RuntimeContext runtimeContext = mock(RuntimeContext.class);
        ShardingSphereSQLParserEngine sqlParserEngine = mock(ShardingSphereSQLParserEngine.class);
        when(sqlParserEngine.parse(anyString(), anyBoolean())).thenReturn(mock(SQLStatement.class));
        when(runtimeContext.getSqlParserEngine()).thenReturn(sqlParserEngine);
        when(schema.getRules()).thenReturn(Collections.emptyList());
        when(result.getSchema()).thenReturn(schema);
        when(result.getRuntimeContext()).thenReturn(runtimeContext);
        return Collections.singletonMap("schema", result);
    }
}
