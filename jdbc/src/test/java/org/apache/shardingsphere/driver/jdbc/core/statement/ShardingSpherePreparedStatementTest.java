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

package org.apache.shardingsphere.driver.jdbc.core.statement;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.resultset.GeneratedKeysResultSet;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;
import org.apache.shardingsphere.sqlfederation.rule.builder.DefaultSQLFederationRuleConfigurationBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingSpherePreparedStatementTest {
    
    @SuppressWarnings("unchecked")
    @Test
    void assertClearBatchResetsCachedGeneratedKeysResultSet() throws SQLException, ReflectiveOperationException {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "SQL92"));
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.emptyList()));
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.getDatabase("foo_db")).thenReturn(database);
        when(metaData.getGlobalRuleMetaData()).thenReturn(new RuleMetaData(Arrays.asList(
                new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build()),
                new SQLFederationRule(new DefaultSQLFederationRuleConfigurationBuilder().build(), Collections.emptyList()))));
        when(metaData.getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        ShardingSphereConnection connection = mock(ShardingSphereConnection.class, RETURNS_DEEP_STUBS);
        when(connection.getContextManager().getMetaDataContexts().getMetaData()).thenReturn(metaData);
        when(connection.getCurrentDatabaseName()).thenReturn("foo_db");
        ShardingSpherePreparedStatement preparedStatement = new ShardingSpherePreparedStatement(connection, "SELECT 1", Statement.RETURN_GENERATED_KEYS);
        ResultSet cachedResultSet = new GeneratedKeysResultSet();
        Plugins.getMemberAccessor().set(ShardingSpherePreparedStatement.class.getDeclaredField("currentBatchGeneratedKeysResultSet"), preparedStatement, cachedResultSet);
        ((Collection<Comparable<?>>) Plugins.getMemberAccessor().get(ShardingSpherePreparedStatement.class.getDeclaredField("generatedValues"), preparedStatement)).add(1L);
        preparedStatement.clearBatch();
        ResultSet actual = preparedStatement.getGeneratedKeys();
        assertThat(actual, not(cachedResultSet));
        assertFalse(actual.isClosed());
    }
}
