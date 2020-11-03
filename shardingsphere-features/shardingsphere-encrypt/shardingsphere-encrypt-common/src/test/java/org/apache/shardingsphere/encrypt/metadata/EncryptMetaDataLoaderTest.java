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

package org.apache.shardingsphere.encrypt.metadata;

import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNodes;
import org.apache.shardingsphere.infra.metadata.model.schema.logic.spi.LogicMetaDataLoader;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPIRegistry;
import org.apache.shardingsphere.infra.metadata.model.schema.physical.model.schema.PhysicalSchemaMetaData;
import org.apache.shardingsphere.infra.metadata.model.schema.physical.model.table.PhysicalTableMetaData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class EncryptMetaDataLoaderTest {
    
    private static final String TABLE_NAME = "t_encrypt";
    
    static {
        ShardingSphereServiceLoader.register(LogicMetaDataLoader.class);
    }
    
    @Mock
    private DatabaseType databaseType;
    
    @Mock
    private DataSource dataSource;
    
    @Mock
    private ConfigurationProperties props;
    
    @Before
    public void setUp() throws SQLException {
        ResultSet tableResultSet = createTableResultSet();
        ResultSet columnResultSet = createColumnResultSet();
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData().getTables(any(), any(), any(), eq(null))).thenReturn(tableResultSet);
        when(connection.getMetaData().getColumns(any(), any(), any(), eq("%"))).thenReturn(columnResultSet);
        when(dataSource.getConnection()).thenReturn(connection);
    }
    
    private ResultSet createTableResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        return result;
    }
    
    private ResultSet createColumnResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, true, false);
        when(result.getString("TABLE_NAME")).thenReturn(TABLE_NAME);
        when(result.getString("COLUMN_NAME")).thenReturn("id", "pwd_cipher", "pwd_plain");
        return result;
    }
    
    @Test
    public void assertLoad() throws SQLException {
        EncryptRule rule = createEncryptRule();
        EncryptMetaDataLoader loader = (EncryptMetaDataLoader) OrderedSPIRegistry.getRegisteredServices(Collections.singletonList(rule), LogicMetaDataLoader.class).get(rule);
        PhysicalSchemaMetaData actual = loader.load(
                databaseType, Collections.singletonMap("logic_db", dataSource), new DataNodes(Collections.singletonList(rule)), rule, props, Collections.emptyList());
        assertThat(actual.get(TABLE_NAME).getColumnMetaData(0).getName(), is("id"));
        assertThat(actual.get(TABLE_NAME).getColumnMetaData(1).getName(), is("pwd_cipher"));
        assertThat(actual.get(TABLE_NAME).getColumnMetaData(2).getName(), is("pwd_plain"));
    }
    
    @Test
    public void assertLoadByExistedTable() throws SQLException {
        EncryptRule rule = createEncryptRule();
        EncryptMetaDataLoader loader = (EncryptMetaDataLoader) OrderedSPIRegistry.getRegisteredServices(Collections.singletonList(rule), LogicMetaDataLoader.class).get(rule);
        Optional<PhysicalTableMetaData> actual = loader.load(
                databaseType, Collections.singletonMap("logic_db", dataSource), new DataNodes(Collections.singletonList(rule)), TABLE_NAME, rule, props);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getColumnMetaData(0).getName(), is("id"));
        assertThat(actual.get().getColumnMetaData(1).getName(), is("pwd_cipher"));
        assertThat(actual.get().getColumnMetaData(2).getName(), is("pwd_plain"));
    }
    
    @Test
    public void assertLoadByNotExistedTable() throws SQLException {
        EncryptRule rule = createEncryptRule();
        EncryptMetaDataLoader loader = new EncryptMetaDataLoader();
        Optional<PhysicalTableMetaData> actual = loader.load(
                databaseType, Collections.singletonMap("logic_db", dataSource), new DataNodes(Collections.singletonList(rule)), "not_existed_table", rule, props);
        assertFalse(actual.isPresent());
    }
    
    private EncryptRule createEncryptRule() {
        EncryptRule result = mock(EncryptRule.class);
        when(result.getEncryptTableNames()).thenReturn(Collections.singletonList(TABLE_NAME));
        when(result.findEncryptTable(TABLE_NAME)).thenReturn(Optional.of(mock(EncryptTable.class)));
        return result;
    }
}
