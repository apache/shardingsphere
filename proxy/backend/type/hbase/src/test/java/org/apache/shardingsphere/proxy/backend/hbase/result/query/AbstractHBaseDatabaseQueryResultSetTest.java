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

package org.apache.shardingsphere.proxy.backend.hbase.result.query;

import lombok.Getter;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Table;
import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.proxy.backend.hbase.context.HBaseContext;
import org.apache.shardingsphere.proxy.backend.hbase.props.HBaseProperties;
import org.apache.shardingsphere.proxy.backend.hbase.props.HBasePropertyKey;
import org.apache.shardingsphere.proxy.backend.hbase.result.HBaseSupportedSQLStatement;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Getter
public abstract class AbstractHBaseDatabaseQueryResultSetTest {
    
    private final Table table = mock(Table.class, RETURNS_DEEP_STUBS);
    
    private final Admin admin = mock(HBaseAdmin.class, RETURNS_DEEP_STUBS);
    
    private final Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
    
    private final TablesContext tablesContext = mock(TablesContext.class, RETURNS_DEEP_STUBS);
    
    private Collection<String> tableNames;
    
    @Before
    public void setUp() throws IOException {
        Properties props = createProperties();
        HBaseProperties hBaseProperties = new HBaseProperties(props);
        HBaseContext.getInstance().setProps(hBaseProperties);
        tableNames = new ArrayList<>();
        tableNames.add("t_test_table");
        when(tablesContext.getTableNames()).thenReturn(tableNames);
        HTableDescriptor[] tableDescriptors = createHTableDescriptors();
        when(admin.tableExists(any())).thenReturn(true);
        when(admin.getTableDescriptor(any())).thenReturn(tableDescriptors[0]);
        when(admin.listTables()).thenReturn(tableDescriptors);
        when(connection.getAdmin()).thenReturn(admin);
        when(connection.getTable(any())).thenReturn(table);
        HBaseContext.getInstance().init(Collections.singletonMap("cluster_lj", connection));
    }
    
    private Properties createProperties() {
        return PropertiesBuilder.build(
                new Property(HBasePropertyKey.WARM_UP_THREAD_NUM.getKey(), String.valueOf(1)));
    }
    
    private HTableDescriptor[] createHTableDescriptors() {
        HTableDescriptor descriptor = mock(HTableDescriptor.class);
        when(descriptor.getNameAsString()).thenReturn(HBaseSupportedSQLStatement.HBASE_DATABASE_TABLE_NAME);
        when(descriptor.toStringTableAttributes()).thenReturn("{attributes}");
        when(descriptor.getFlushPolicyClassName()).thenReturn("");
        when(descriptor.getMaxFileSize()).thenReturn(-1L);
        when(descriptor.getMemStoreFlushSize()).thenReturn(-1L);
        when(descriptor.getPriority()).thenReturn(0);
        when(descriptor.getRegionReplication()).thenReturn(1);
        when(descriptor.getRegionSplitPolicyClassName()).thenReturn(null);
        when(descriptor.toStringCustomizedValues()).thenReturn("");
        when(descriptor.getFamilies()).thenReturn(Collections.singletonList(mock(HColumnDescriptor.class)));
        return new HTableDescriptor[]{descriptor};
    }
    
    @After
    public void tearDown() {
        HBaseContext.getInstance().close();
    }
}
