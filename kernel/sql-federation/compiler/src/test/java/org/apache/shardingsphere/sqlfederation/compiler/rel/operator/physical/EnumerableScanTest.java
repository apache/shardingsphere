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
import org.apache.calcite.DataContext;
import org.apache.calcite.adapter.enumerable.EnumerableConvention;
import org.apache.calcite.adapter.enumerable.EnumerableRel;
import org.apache.calcite.adapter.enumerable.EnumerableRelImplementor;
import org.apache.calcite.adapter.enumerable.JavaRowFormat;
import org.apache.calcite.adapter.enumerable.PhysType;
import org.apache.calcite.adapter.enumerable.PhysTypeImpl;
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.linq4j.Linq4j;
import org.apache.calcite.linq4j.tree.BlockStatement;
import org.apache.calcite.linq4j.tree.Blocks;
import org.apache.calcite.linq4j.tree.Expressions;
import org.apache.calcite.plan.ConventionTraitDef;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptSchema;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.plan.volcano.VolcanoPlanner;
import org.apache.calcite.prepare.RelOptTableImpl;
import org.apache.calcite.rel.RelWriter;
import org.apache.calcite.rel.externalize.RelWriterImpl;
import org.apache.calcite.rel.logical.LogicalTableScan;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.Statistics;
import org.apache.calcite.sql.SqlExplainLevel;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.sql.util.SqlString;
import org.apache.shardingsphere.sqlfederation.compiler.metadata.schema.SQLFederationTable;
import org.apache.shardingsphere.sqlfederation.compiler.sql.dialect.SQLDialectFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EnumerableScanTest {
    
    private RelOptCluster cluster;
    
    private RelDataType rowType;
    
    private RelOptTable relOptTable;
    
    @BeforeEach
    void setUp() {
        JavaTypeFactoryImpl typeFactory = new JavaTypeFactoryImpl();
        VolcanoPlanner planner = new VolcanoPlanner();
        planner.addRelTraitDef(ConventionTraitDef.INSTANCE);
        cluster = RelOptCluster.create(planner, new RexBuilder(typeFactory));
        rowType = typeFactory.builder().add("col", SqlTypeName.INTEGER).build();
        SQLFederationTable federationTable = mock(SQLFederationTable.class);
        when(federationTable.getRowType(typeFactory)).thenReturn(rowType);
        when(federationTable.toRel(any(RelOptTable.ToRelContext.class), any(RelOptTable.class))).thenAnswer(
                invocation -> LogicalTableScan.create(invocation.getArgument(0, RelOptTable.ToRelContext.class).getCluster(), invocation.getArgument(1, RelOptTable.class), Collections.emptyList()));
        when(federationTable.getStatistic()).thenReturn(Statistics.of(1D, ImmutableList.of()));
        when(federationTable.getJdbcTableType()).thenReturn(Schema.TableType.TABLE);
        when(federationTable.implement(any(DataContext.class), any(), any(int[].class))).thenReturn(Linq4j.emptyEnumerable());
        RelOptSchema schema = mock(RelOptSchema.class);
        when(schema.getTypeFactory()).thenReturn(typeFactory);
        relOptTable = RelOptTableImpl.create(schema, rowType, ImmutableList.of("test", "tbl"), federationTable, each -> Expressions.constant(federationTable));
    }
    
    @Test
    void assertConstructorWithPushDownNodeSetsFields() {
        LogicalTableScan pushDownNode = LogicalTableScan.create(cluster, relOptTable, Collections.emptyList());
        EnumerableScan scan = new EnumerableScan(cluster, cluster.traitSet(), relOptTable, pushDownNode, "MySQL");
        assertNotNull(scan.getSqlString());
        assertThat(scan.getPushDownRowType(), is(pushDownNode.getRowType()));
        assertThat(scan.getTraitSet().getConvention(), is(EnumerableConvention.INSTANCE));
    }
    
    @Test
    void assertCopyKeepsSqlAndRowType() {
        SqlString sqlString = new SqlString(SQLDialectFactory.getSQLDialect("MySQL"), "SELECT 1");
        EnumerableScan copied = (EnumerableScan) new EnumerableScan(cluster, cluster.traitSet(), relOptTable, sqlString, rowType).copy(cluster.traitSet(), Collections.emptyList());
        assertThat(copied.getSqlString(), is(sqlString));
        assertThat(copied.getPushDownRowType(), is(rowType));
    }
    
    @Test
    void assertExplainTerms() {
        SqlString sqlString = new SqlString(SQLDialectFactory.getSQLDialect("MySQL"), "SELECT 1");
        EnumerableScan scan = new EnumerableScan(cluster, cluster.traitSet(), relOptTable, sqlString, rowType);
        StringWriter writer = new StringWriter();
        RelWriter relWriter = new RelWriterImpl(new PrintWriter(writer), SqlExplainLevel.EXPPLAN_ATTRIBUTES, false);
        scan.explainTerms(relWriter);
        relWriter.done(scan);
        assertThat(writer.toString(), containsString("sql="));
        assertThat(writer.toString(), containsString("dynamicParameters"));
    }
    
    @Test
    void assertDeriveRowType() {
        SqlString sqlString = new SqlString(SQLDialectFactory.getSQLDialect("MySQL"), "SELECT 1");
        assertThat(new EnumerableScan(cluster, cluster.traitSet(), relOptTable, sqlString, rowType).deriveRowType(), is(rowType));
    }
    
    @Test
    void assertImplementWithoutDynamicParameters() {
        EnumerableRelImplementor implementor = mock(EnumerableRelImplementor.class);
        when(implementor.getTypeFactory()).thenReturn((JavaTypeFactory) cluster.getTypeFactory());
        when(implementor.getRootExpression()).thenReturn(Expressions.parameter(DataContext.class, "root"));
        EnumerableRel.Result actual = new EnumerableRel.Result(
                Blocks.toBlock(Expressions.constant(1)), PhysTypeImpl.of((JavaTypeFactory) cluster.getTypeFactory(), rowType, JavaRowFormat.ARRAY), JavaRowFormat.ARRAY);
        when(implementor.result(any(PhysType.class), any(BlockStatement.class))).thenReturn(actual);
        EnumerableScan scan = new EnumerableScan(cluster, cluster.traitSet(), relOptTable, LogicalTableScan.create(cluster, relOptTable, Collections.emptyList()), "MySQL");
        assertNotNull(scan.implement(implementor, EnumerableRel.Prefer.ARRAY));
    }
    
    @Test
    void assertImplementWithDynamicParameters() {
        EnumerableRelImplementor implementor = mock(EnumerableRelImplementor.class);
        when(implementor.getTypeFactory()).thenReturn((JavaTypeFactory) cluster.getTypeFactory());
        when(implementor.getRootExpression()).thenReturn(Expressions.parameter(DataContext.class, "root"));
        ArgumentCaptor<BlockStatement> blockCaptor = ArgumentCaptor.forClass(BlockStatement.class);
        EnumerableRel.Result actual = new EnumerableRel.Result(
                Blocks.toBlock(Expressions.constant(1)), PhysTypeImpl.of((JavaTypeFactory) cluster.getTypeFactory(), rowType, JavaRowFormat.ARRAY), JavaRowFormat.ARRAY);
        when(implementor.result(any(PhysType.class), blockCaptor.capture())).thenReturn(actual);
        SqlString sqlString = new SqlString(SQLDialectFactory.getSQLDialect("MySQL"), "SELECT 1", ImmutableList.of(1, 3));
        assertNotNull(new EnumerableScan(cluster, cluster.traitSet(), relOptTable, sqlString, rowType).implement(implementor, EnumerableRel.Prefer.ARRAY));
        assertThat(blockCaptor.getValue().toString(), containsString("1"));
        assertThat(blockCaptor.getValue().toString(), containsString("3"));
    }
}
