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

package org.apache.shardingsphere.encrypt.merge.dql;

import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.context.EncryptContext;
import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.DerivedProjection;
import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class EncryptAlgorithmMetaDataTest {
    
    @Mock
    private ShardingSphereSchema schema;
    
    @Mock
    private EncryptRule encryptRule;
    
    @Mock
    private SelectStatementContext selectStatementContext;
    
    @Mock
    private TablesContext tablesContext;
    
    @Mock
    private ColumnProjection columnProjection;
    
    @Mock
    private ProjectionsContext projectionsContext;
    
    private EncryptAlgorithm encryptAlgorithm;
    
    @Before
    public void setUp() {
        when(selectStatementContext.getProjectionsContext()).thenReturn(projectionsContext);
        List<Projection> columnProjectionList = Collections.singletonList(columnProjection);
        when(projectionsContext.getExpandProjections()).thenReturn(columnProjectionList);
        when(columnProjection.getName()).thenReturn("id");
        when(columnProjection.getExpression()).thenReturn("id");
        when(selectStatementContext.getTablesContext()).thenReturn(tablesContext);
        encryptAlgorithm = ShardingSphereAlgorithmFactory.createAlgorithm(new ShardingSphereAlgorithmConfiguration("Md5", new Properties()), EncryptAlgorithm.class);
    }
    
    @Test
    public void assertFindEncryptContextByMetaData() {
        Map<String, String> columnTableNames = new HashMap<>();
        columnTableNames.put(columnProjection.getExpression(), "t_order");
        when(tablesContext.findTableName(Collections.singletonList(columnProjection), schema)).thenReturn(columnTableNames);
        EncryptAlgorithmMetaData encryptAlgorithmMetaData = new EncryptAlgorithmMetaData(DefaultSchema.LOGIC_NAME, schema, encryptRule, selectStatementContext);
        Optional<EncryptContext> actual = encryptAlgorithmMetaData.findEncryptContext(1);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getSchemaName(), is(DefaultSchema.LOGIC_NAME));
        assertThat(actual.get().getTableName(), is("t_order"));
        assertThat(actual.get().getColumnName(), is("id"));
    }
    
    @Test
    public void assertFindEncryptContextByStatementContext() {
        when(tablesContext.findTableName(Collections.singletonList(columnProjection), schema)).thenReturn(Collections.emptyMap());
        when(tablesContext.getTableNames()).thenReturn(Arrays.asList("t_user", "t_user_item", "t_order_item"));
        when(encryptRule.findEncryptor("t_order_item", "id")).thenReturn(Optional.of(encryptAlgorithm));
        EncryptAlgorithmMetaData encryptAlgorithmMetaData = new EncryptAlgorithmMetaData(DefaultSchema.LOGIC_NAME, schema, encryptRule, selectStatementContext);
        Optional<EncryptContext> actual = encryptAlgorithmMetaData.findEncryptContext(1);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getSchemaName(), is(DefaultSchema.LOGIC_NAME));
        assertThat(actual.get().getTableName(), is("t_order_item"));
        assertThat(actual.get().getColumnName(), is("id"));
    }
    
    @Test
    public void assertFindEncryptContextWhenColumnProjectionIsNotExist() {
        when(projectionsContext.getExpandProjections()).thenReturn(Collections.singletonList(mock(DerivedProjection.class)));
        EncryptAlgorithmMetaData encryptAlgorithmMetaData = new EncryptAlgorithmMetaData(DefaultSchema.LOGIC_NAME, schema, encryptRule, selectStatementContext);
        Optional<EncryptContext> actual = encryptAlgorithmMetaData.findEncryptContext(1);
        assertFalse(actual.isPresent());
    }
    
    @Test
    public void assertFindEncryptor() {
        Map<String, String> columnTableNames = new HashMap<>();
        columnTableNames.put(columnProjection.getExpression(), "t_order");
        when(encryptRule.findEncryptor("t_order", "id")).thenReturn(Optional.of(encryptAlgorithm));
        EncryptAlgorithmMetaData encryptAlgorithmMetaData = new EncryptAlgorithmMetaData(DefaultSchema.LOGIC_NAME, schema, encryptRule, selectStatementContext);
        Optional<EncryptAlgorithm> actualEncryptor = encryptAlgorithmMetaData.findEncryptor("t_order", "id");
        assertTrue(actualEncryptor.isPresent());
        assertThat(actualEncryptor.get().getType(), is("MD5"));
    }
    
    @Test
    public void assertIsQueryWithCipherColumn() {
        Map<String, String> columnTableNames = new HashMap<>();
        columnTableNames.put(columnProjection.getExpression(), "t_order");
        when(encryptRule.isQueryWithCipherColumn("t_order")).thenReturn(true);
        EncryptAlgorithmMetaData encryptAlgorithmMetaData = new EncryptAlgorithmMetaData(DefaultSchema.LOGIC_NAME, schema, encryptRule, selectStatementContext);
        assertTrue(encryptAlgorithmMetaData.isQueryWithCipherColumn("t_order"));
    }
}
