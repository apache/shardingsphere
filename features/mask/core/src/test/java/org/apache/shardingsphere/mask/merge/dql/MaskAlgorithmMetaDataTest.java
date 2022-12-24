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

package org.apache.shardingsphere.mask.merge.dql;

import org.apache.shardingsphere.infra.binder.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.DerivedProjection;
import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.mask.factory.MaskAlgorithmFactory;
import org.apache.shardingsphere.mask.rule.MaskRule;
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;
import org.apache.shardingsphere.mask.spi.context.MaskContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MaskAlgorithmMetaDataTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private ShardingSphereSchema schema;
    
    @Mock
    private MaskRule maskRule;
    
    @Mock
    private SelectStatementContext selectStatementContext;
    
    @Mock
    private TablesContext tablesContext;
    
    @Mock
    private ColumnProjection columnProjection;
    
    @Mock
    private ProjectionsContext projectionsContext;
    
    private MaskAlgorithm<?, ?> maskAlgorithm;
    
    @Before
    public void setUp() {
        when(selectStatementContext.getProjectionsContext()).thenReturn(projectionsContext);
        when(projectionsContext.getExpandProjections()).thenReturn(Collections.singletonList(columnProjection));
        when(columnProjection.getName()).thenReturn("id");
        when(columnProjection.getExpression()).thenReturn("id");
        when(selectStatementContext.getTablesContext()).thenReturn(tablesContext);
        when(selectStatementContext.getDatabaseType()).thenReturn(new MySQLDatabaseType());
        when(database.getName()).thenReturn(DefaultDatabase.LOGIC_NAME);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(schema);
        maskAlgorithm = (MaskAlgorithm<?, ?>) MaskAlgorithmFactory.newInstance(new AlgorithmConfiguration("MD5", new Properties()));
    }
    
    @Test
    public void assertFindMaskContextByMetaData() {
        Map<String, String> columnTableNames = new HashMap<>();
        columnTableNames.put(columnProjection.getExpression(), "t_order");
        when(tablesContext.findTableNamesByColumnProjection(Collections.singletonList(columnProjection), schema)).thenReturn(columnTableNames);
        MaskAlgorithmMetaData maskAlgorithmMetaData = new MaskAlgorithmMetaData(database, maskRule, selectStatementContext);
        Optional<MaskContext> actual = maskAlgorithmMetaData.findMaskContext(1);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getDatabaseName(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(actual.get().getTableName(), is("t_order"));
        assertThat(actual.get().getColumnName(), is("id"));
    }
    
    @Test
    public void assertFindMaskContextByStatementContext() {
        when(tablesContext.findTableNamesByColumnProjection(Collections.singletonList(columnProjection), schema)).thenReturn(Collections.emptyMap());
        when(tablesContext.getTableNames()).thenReturn(Arrays.asList("t_user", "t_user_item", "t_order_item"));
        when(maskRule.findMaskAlgorithm("t_order_item", "id")).thenReturn(Optional.of(maskAlgorithm));
        MaskAlgorithmMetaData maskAlgorithmMetaData = new MaskAlgorithmMetaData(database, maskRule, selectStatementContext);
        Optional<MaskContext> actual = maskAlgorithmMetaData.findMaskContext(1);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getDatabaseName(), is(DefaultDatabase.LOGIC_NAME));
        assertThat(actual.get().getTableName(), is("t_order_item"));
        assertThat(actual.get().getColumnName(), is("id"));
    }
    
    @Test
    public void assertFindMaskContextWhenColumnProjectionIsNotExist() {
        when(projectionsContext.getExpandProjections()).thenReturn(Collections.singletonList(mock(DerivedProjection.class)));
        MaskAlgorithmMetaData maskAlgorithmMetaData = new MaskAlgorithmMetaData(database, maskRule, selectStatementContext);
        Optional<MaskContext> actual = maskAlgorithmMetaData.findMaskContext(1);
        assertFalse(actual.isPresent());
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    public void assertFindMaskAlgorithm() {
        when(maskRule.findMaskAlgorithm("t_order", "id")).thenReturn(Optional.of(maskAlgorithm));
        MaskAlgorithmMetaData maskAlgorithmMetaData = new MaskAlgorithmMetaData(database, maskRule, selectStatementContext);
        Optional<MaskAlgorithm> actual = maskAlgorithmMetaData.findMaskAlgorithm("t_order", "id");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getType(), is("MD5"));
    }
}
