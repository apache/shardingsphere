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

package org.apache.shardingsphere.sqlfederation.compiler.rel.operator.physical;

import com.google.common.collect.ImmutableList;
import org.apache.calcite.adapter.enumerable.EnumerableRel;
import org.apache.calcite.adapter.enumerable.EnumerableRelImplementor;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.linq4j.tree.Expressions;
import org.apache.calcite.plan.ConventionTraitDef;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptSchema;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.plan.volcano.VolcanoPlanner;
import org.apache.calcite.prepare.Prepare;
import org.apache.calcite.prepare.RelOptTableImpl;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.TableModify;
import org.apache.calcite.rel.logical.LogicalTableScan;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.Statistics;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.shardingsphere.sqlfederation.compiler.metadata.schema.SQLFederationTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EnumerableModifyTest {
    
    private RelOptCluster cluster;
    
    private RelOptTable relOptTable;
    
    private RelNode input;
    
    private Prepare.CatalogReader catalogReader;
    
    @BeforeEach
    void setUp() {
        RelDataTypeFactory typeFactory = new JavaTypeFactoryImpl();
        RexBuilder rexBuilder = new RexBuilder(typeFactory);
        VolcanoPlanner planner = new VolcanoPlanner();
        planner.addRelTraitDef(ConventionTraitDef.INSTANCE);
        cluster = RelOptCluster.create(planner, rexBuilder);
        RelDataType rowType = typeFactory.builder().add("col", SqlTypeName.INTEGER).build();
        SQLFederationTable federationTable = mock(SQLFederationTable.class);
        when(federationTable.getRowType(typeFactory)).thenReturn(rowType);
        when(federationTable.toRel(org.mockito.ArgumentMatchers.any(RelOptTable.ToRelContext.class), org.mockito.ArgumentMatchers.any(RelOptTable.class))).thenAnswer(invocation ->
                LogicalTableScan.create(invocation.getArgument(0, RelOptTable.ToRelContext.class).getCluster(), invocation.getArgument(1, RelOptTable.class), Collections.emptyList()));
        when(federationTable.getStatistic()).thenReturn(Statistics.of(1D, ImmutableList.of()));
        when(federationTable.getJdbcTableType()).thenReturn(Schema.TableType.TABLE);
        RelOptSchema schema = mock(RelOptSchema.class);
        when(schema.getTypeFactory()).thenReturn(typeFactory);
        relOptTable = RelOptTableImpl.create(schema, rowType, ImmutableList.of("test", "tbl"), federationTable, each -> Expressions.constant(federationTable));
        input = LogicalTableScan.create(cluster, relOptTable, Collections.emptyList());
        catalogReader = mock(Prepare.CatalogReader.class);
    }
    
    @Test
    void assertCopy() {
        EnumerableModify enumerableModify = new EnumerableModify(cluster, cluster.traitSet(), relOptTable, catalogReader, input, TableModify.Operation.INSERT, null, null, false);
        EnumerableModify copied = (EnumerableModify) enumerableModify.copy(cluster.traitSet(), Collections.singletonList(input));
        assertThat(copied.getTable(), is(relOptTable));
        assertThat(copied.getInput(), is(input));
        assertThat(copied.getCluster(), is(cluster));
    }
    
    @Test
    void assertImplement() {
        EnumerableModify enumerableModify = new EnumerableModify(cluster, cluster.traitSet(), relOptTable, catalogReader, input, TableModify.Operation.DELETE, null, null, false);
        assertThrows(UnsupportedOperationException.class, () -> enumerableModify.implement(mock(EnumerableRelImplementor.class), EnumerableRel.Prefer.ARRAY));
    }
}
