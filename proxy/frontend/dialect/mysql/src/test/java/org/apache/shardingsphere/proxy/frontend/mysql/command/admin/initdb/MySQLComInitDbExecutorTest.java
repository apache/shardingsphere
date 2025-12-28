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

package org.apache.shardingsphere.proxy.frontend.mysql.command.admin.initdb;

import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLStatusFlag;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.admin.initdb.MySQLComInitDbPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MySQLComInitDbExecutorTest {
    
    private ContextManager originalContextManager;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        originalContextManager = (ContextManager) Plugins.getMemberAccessor().get(ProxyContext.class.getDeclaredField("contextManager"), ProxyContext.getInstance());
    }
    
    @AfterEach
    void tearDown() throws ReflectiveOperationException {
        Plugins.getMemberAccessor().set(ProxyContext.class.getDeclaredField("contextManager"), ProxyContext.getInstance(), originalContextManager);
    }
    
    @Test
    void assertExecuteWithAuthorizedDatabase() throws ReflectiveOperationException {
        MySQLComInitDbPacket packet = mock(MySQLComInitDbPacket.class);
        when(packet.getSchema()).thenReturn("`foo_db`");
        ConnectionSession connectionSession = mock(ConnectionSession.class, RETURNS_DEEP_STUBS);
        when(connectionSession.getConnectionContext()).thenReturn(new ConnectionContext(Collections::emptyList));
        when(connectionSession.isAutoCommit()).thenReturn(true);
        Plugins.getMemberAccessor().set(ProxyContext.class.getDeclaredField("contextManager"), ProxyContext.getInstance(), mockContextManager("foo_db", mock(AuthorityRule.class), true));
        MySQLComInitDbExecutor executor = new MySQLComInitDbExecutor(packet, connectionSession);
        Collection<DatabasePacket> actual = executor.execute();
        assertThat(actual.size(), is(1));
        MySQLOKPacket okPacket = (MySQLOKPacket) actual.iterator().next();
        assertThat(okPacket.getStatusFlag(), is(MySQLStatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue()));
        verify(connectionSession).setCurrentDatabaseName("`foo_db`");
    }
    
    @Test
    void assertExecuteWithAbsentDatabase() throws ReflectiveOperationException {
        MySQLComInitDbPacket packet = mock(MySQLComInitDbPacket.class);
        when(packet.getSchema()).thenReturn("absent_db");
        Plugins.getMemberAccessor().set(ProxyContext.class.getDeclaredField("contextManager"), ProxyContext.getInstance(), mockContextManager("absent_db", mock(AuthorityRule.class), false));
        assertThrows(UnknownDatabaseException.class, new MySQLComInitDbExecutor(packet, mock(ConnectionSession.class, RETURNS_DEEP_STUBS))::execute);
    }
    
    private ContextManager mockContextManager(final String databaseName, final AuthorityRule authorityRule, final boolean containsDatabase) {
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getMetaData().getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(authorityRule)));
        when(metaDataContexts.getMetaData().containsDatabase(databaseName)).thenReturn(containsDatabase);
        ContextManager result = mock(ContextManager.class);
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        return result;
    }
}
