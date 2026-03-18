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

package org.apache.shardingsphere.driver.jdbc.unsupported;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.statement.ShardingSphereStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;
import org.apache.shardingsphere.sqlfederation.rule.builder.DefaultSQLFederationRuleConfigurationBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLFeatureNotSupportedException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UnsupportedOperationStatementTest {
    
    private ShardingSphereStatement shardingSphereStatement;
    
    @BeforeEach
    void setUp() {
        ShardingSphereConnection connection = mock(ShardingSphereConnection.class, RETURNS_DEEP_STUBS);
        when(connection.getCurrentDatabaseName()).thenReturn("db");
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
        when(connection.getContextManager().getMetaDataContexts().getMetaData().getDatabase("db").getProtocolType()).thenReturn(databaseType);
        when(connection.getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(new RuleMetaData(
                Arrays.asList(
                        new SQLFederationRule(new DefaultSQLFederationRuleConfigurationBuilder().build(), Collections.emptyList()),
                        new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build()))));
        when(connection.getContextManager().getMetaDataContexts().getMetaData().getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        shardingSphereStatement = new ShardingSphereStatement(connection);
    }
    
    @Test
    void assertSetCursorName() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSphereStatement.setCursorName("cursorName"));
    }
}
