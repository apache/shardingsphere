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

package org.apache.shardingsphere.proxy.backend.handler;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.parser.SQLParserEngine;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.EmptyStatement;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class ProxySQLComQueryParserTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Test
    void assertParseEmptySQL() {
        when(connectionSession.getUsedDatabaseName()).thenReturn(null);
        SQLStatement actual = ProxySQLComQueryParser.parse("   ", databaseType, connectionSession);
        assertThat(actual, isA(EmptyStatement.class));
        assertThat(actual.getDatabaseType(), is(databaseType));
    }
    
    @Test
    void assertParseWithMissingDatabaseUseDefaultType() {
        when(connectionSession.getUsedDatabaseName()).thenReturn("missing_db");
        SQLParserEngine parserEngine = mock(SQLParserEngine.class);
        SQLStatement expected = new SQLStatement(databaseType);
        SQLParserRule parserRule = mockParserRule(parserEngine);
        mockProxyContext(parserRule, false, null);
        when(parserEngine.parse(anyString(), eq(false))).thenReturn(expected);
        assertThat(ProxySQLComQueryParser.parse("select 1", databaseType, connectionSession), is(expected));
    }
    
    @Test
    void assertParseWithExistingDatabaseUseDatabaseProtocolType() {
        DatabaseType protocolType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        when(connectionSession.getUsedDatabaseName()).thenReturn("logic_db");
        SQLParserEngine parserEngine = mock(SQLParserEngine.class);
        SQLStatement expected = new SQLStatement(protocolType);
        SQLParserRule parserRule = mockParserRule(parserEngine);
        mockProxyContext(parserRule, true, protocolType);
        when(parserEngine.parse(anyString(), eq(false))).thenReturn(expected);
        assertThat(ProxySQLComQueryParser.parse("select * from t_order", databaseType, connectionSession), is(expected));
    }
    
    private SQLParserRule mockParserRule(final SQLParserEngine parserEngine) {
        SQLParserRule result = mock(SQLParserRule.class);
        when(result.getSQLParserEngine(any(DatabaseType.class))).thenReturn(parserEngine);
        return result;
    }
    
    private void mockProxyContext(final SQLParserRule parserRule, final boolean containsDatabase, final DatabaseType protocolType) {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(parserRule)));
        lenient().when(contextManager.getMetaDataContexts().getMetaData().containsDatabase("logic_db")).thenReturn(containsDatabase);
        if (containsDatabase) {
            ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
            when(contextManager.getDatabase(anyString())).thenReturn(database);
            when(database.getProtocolType()).thenReturn(protocolType);
        }
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
    }
}
