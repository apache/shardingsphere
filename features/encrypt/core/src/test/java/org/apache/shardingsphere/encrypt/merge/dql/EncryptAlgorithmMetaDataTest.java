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

import org.apache.shardingsphere.encrypt.api.context.EncryptContext;
import org.apache.shardingsphere.encrypt.api.encrypt.standard.StandardEncryptAlgorithm;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.binder.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.DerivedProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.SubqueryProjection;
import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EncryptAlgorithmMetaDataTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
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
    
    private StandardEncryptAlgorithm<?, ?> encryptAlgorithm;
    
    @BeforeEach
    void setUp() {
        when(selectStatementContext.getProjectionsContext()).thenReturn(projectionsContext);
        when(projectionsContext.getExpandProjections()).thenReturn(Collections.singletonList(columnProjection));
        when(columnProjection.getName()).thenReturn("id");
        when(columnProjection.getExpression()).thenReturn("id");
        when(selectStatementContext.getTablesContext()).thenReturn(tablesContext);
        when(selectStatementContext.getDatabaseType()).thenReturn(new MySQLDatabaseType());
        when(database.getName()).thenReturn(DefaultDatabase.LOGIC_NAME);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(schema);
        encryptAlgorithm =
                (StandardEncryptAlgorithm<?, ?>) TypedSPILoader.getService(EncryptAlgorithm.class, "AES", PropertiesBuilder.build(new PropertiesBuilder.Property("aes-key-value", "123456abc")));
    }
    
    @Test
    void assertFindEncryptContextByMetaData() {
        Map<String, String> columnTableNames = new HashMap<>();
        columnTableNames.put(columnProjection.getExpression(), "t_order");
        when(tablesContext.findTableNamesByColumnProjection(Collections.singletonList(columnProjection), schema)).thenReturn(columnTableNames);
        EncryptAlgorithmMetaData encryptAlgorithmMetaData = new EncryptAlgorithmMetaData(database, encryptRule, selectStatementContext);
        Optional<EncryptContext> actual = encryptAlgorithmMetaData.findEncryptContext(1);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getDatabaseName(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(actual.get().getTableName(), is("t_order"));
        assertThat(actual.get().getColumnName(), is("id"));
    }
    
    @Test
    void assertFindEncryptContextWhenSubqueryContainsEncryptColumn() {
        ColumnProjection columnProjection = new ColumnProjection(null, "user_name", null);
        Map<String, String> columnTableNames = new HashMap<>();
        columnTableNames.put(columnProjection.getExpression(), "t_user");
        when(projectionsContext.getExpandProjections())
                .thenReturn(Collections.singletonList(new SubqueryProjection("(SELECT user_name FROM t_user)", columnProjection, null, new MySQLDatabaseType())));
        when(tablesContext.findTableNamesByColumnProjection(Collections.singletonList(columnProjection), schema)).thenReturn(columnTableNames);
        EncryptAlgorithmMetaData encryptAlgorithmMetaData = new EncryptAlgorithmMetaData(database, encryptRule, selectStatementContext);
        Optional<EncryptContext> actual = encryptAlgorithmMetaData.findEncryptContext(1);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getDatabaseName(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(actual.get().getTableName(), is("t_user"));
        assertThat(actual.get().getColumnName(), is("user_name"));
    }
    
    @Test
    void assertFindEncryptContextByStatementContext() {
        when(tablesContext.findTableNamesByColumnProjection(Collections.singletonList(columnProjection), schema)).thenReturn(Collections.emptyMap());
        when(tablesContext.getTableNames()).thenReturn(Arrays.asList("t_user", "t_user_item", "t_order_item"));
        when(encryptRule.findStandardEncryptor("t_order_item", "id")).thenReturn(Optional.of(encryptAlgorithm));
        EncryptAlgorithmMetaData encryptAlgorithmMetaData = new EncryptAlgorithmMetaData(database, encryptRule, selectStatementContext);
        Optional<EncryptContext> actual = encryptAlgorithmMetaData.findEncryptContext(1);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getDatabaseName(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(actual.get().getTableName(), is("t_order_item"));
        assertThat(actual.get().getColumnName(), is("id"));
    }
    
    @Test
    void assertFindEncryptContextWhenColumnProjectionIsNotExist() {
        when(projectionsContext.getExpandProjections()).thenReturn(Collections.singletonList(mock(DerivedProjection.class)));
        EncryptAlgorithmMetaData encryptAlgorithmMetaData = new EncryptAlgorithmMetaData(database, encryptRule, selectStatementContext);
        Optional<EncryptContext> actual = encryptAlgorithmMetaData.findEncryptContext(1);
        assertFalse(actual.isPresent());
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    void assertFindStandardEncryptor() {
        when(encryptRule.findStandardEncryptor("t_order", "id")).thenReturn(Optional.of(encryptAlgorithm));
        EncryptAlgorithmMetaData encryptAlgorithmMetaData = new EncryptAlgorithmMetaData(database, encryptRule, selectStatementContext);
        Optional<StandardEncryptAlgorithm> actualEncryptor = encryptAlgorithmMetaData.findStandardEncryptor("t_order", "id");
        assertTrue(actualEncryptor.isPresent());
        assertThat(actualEncryptor.get().getType(), is("AES"));
    }
}
