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

import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.statement.ShardingSpherePreparedStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;
import org.apache.shardingsphere.sqlfederation.rule.builder.DefaultSQLFederationRuleConfigurationBuilder;
import org.apache.shardingsphere.traffic.rule.TrafficRule;
import org.apache.shardingsphere.traffic.rule.builder.DefaultTrafficRuleConfigurationBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.sql.NClob;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UnsupportedOperationPreparedStatementTest {
    
    private ShardingSpherePreparedStatement shardingSpherePreparedStatement;
    
    @BeforeEach
    void setUp() throws SQLException {
        ShardingSphereConnection connection = mock(ShardingSphereConnection.class, RETURNS_DEEP_STUBS);
        when(connection.getDatabaseName()).thenReturn(DefaultDatabase.LOGIC_NAME);
        when(connection.getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Arrays.asList(
                new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build()),
                new TrafficRule(new DefaultTrafficRuleConfigurationBuilder().build()),
                new SQLFederationRule(new DefaultSQLFederationRuleConfigurationBuilder().build(), Collections.emptyMap(), mock(ConfigurationProperties.class)))));
        when(connection.getContextManager().getMetaDataContexts().getMetaData().getDatabase(connection.getDatabaseName()).getProtocolType()).thenReturn(new MySQLDatabaseType());
        when(connection.getContextManager().getMetaDataContexts().getMetaData().getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        shardingSpherePreparedStatement = new ShardingSpherePreparedStatement(connection, "SELECT 1");
    }
    
    @Test
    void assertGetMetaData() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSpherePreparedStatement.getMetaData());
    }
    
    @Test
    void assertSetNString() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSpherePreparedStatement.setNString(1, ""));
    }
    
    @Test
    void assertSetNClob() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSpherePreparedStatement.setNClob(1, (NClob) null));
    }
    
    @Test
    void assertSetNClobForReader() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSpherePreparedStatement.setNClob(1, new StringReader("")));
    }
    
    @Test
    void assertSetNClobForReaderAndLength() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSpherePreparedStatement.setNClob(1, new StringReader(""), 1));
    }
    
    @Test
    void assertSetNCharacterStream() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSpherePreparedStatement.setNCharacterStream(1, new StringReader("")));
    }
    
    @Test
    void assertSetNCharacterStreamWithLength() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSpherePreparedStatement.setNCharacterStream(1, new StringReader(""), 1));
    }
    
    @Test
    void assertSetRowId() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSpherePreparedStatement.setRowId(1, null));
    }
    
    @Test
    void assertSetRef() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> shardingSpherePreparedStatement.setRef(1, null));
    }
}
