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

package org.apache.shardingsphere.sqlfederation.compiler.rel.operator.logical;

import com.google.common.collect.ImmutableList;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.linq4j.tree.Expressions;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptSchema;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.plan.volcano.VolcanoPlanner;
import org.apache.calcite.prepare.RelOptTableImpl;
import org.apache.calcite.rel.AbstractRelNode;
import org.apache.calcite.rel.RelWriter;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.rel.externalize.RelWriterImpl;
import org.apache.calcite.rel.hint.RelHint;
import org.apache.calcite.rel.logical.LogicalFilter;
import org.apache.calcite.rel.logical.LogicalProject;
import org.apache.calcite.rel.logical.LogicalTableScan;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.Statistics;
import org.apache.calcite.schema.ModifiableTable;
import org.apache.calcite.schema.TranslatableTable;
import org.apache.calcite.sql.SqlExplainLevel;
import org.apache.calcite.sql.type.SqlTypeName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.plugins.MemberAccessor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class LogicalScanTest {
    
    private RelDataType rowType;
    
    private RelOptCluster cluster;
    
    private LogicalTableScan tableScan;
    
    private RexBuilder rexBuilder;
    
    @BeforeEach
    void setUp() {
        RelDataTypeFactory typeFactory = new JavaTypeFactoryImpl();
        rexBuilder = new RexBuilder(typeFactory);
        cluster = RelOptCluster.create(new VolcanoPlanner(), rexBuilder);
        rowType = typeFactory.builder().add("col", SqlTypeName.INTEGER).build();
        TranslatableTable federationTable = mock(TranslatableTable.class, withSettings().extraInterfaces(ModifiableTable.class));
        when(federationTable.getRowType(typeFactory)).thenReturn(rowType);
        when(federationTable.getStatistic()).thenReturn(Statistics.of(1D, ImmutableList.of()));
        when(federationTable.getJdbcTableType()).thenReturn(Schema.TableType.TABLE);
        when(federationTable.toRel(any(RelOptTable.ToRelContext.class), any(RelOptTable.class))).thenAnswer(invocation -> LogicalTableScan.create(
                invocation.getArgument(0, RelOptTable.ToRelContext.class).getCluster(), invocation.getArgument(1, RelOptTable.class), ImmutableList.of()));
        RelOptSchema relOptSchema = mock(RelOptSchema.class);
        when(relOptSchema.getTypeFactory()).thenReturn(typeFactory);
        RelOptTable relOptTable = RelOptTableImpl.create(relOptSchema, rowType, ImmutableList.of("test", "tbl"), federationTable, each -> Expressions.constant(federationTable));
        when(relOptSchema.getTableForMember(anyList())).thenReturn(relOptTable);
        tableScan = LogicalTableScan.create(cluster, relOptTable, Collections.emptyList());
    }
    
    @Test
    void assertPushDownFilter() {
        LogicalFilter logicalFilter = LogicalFilter.create(tableScan, rexBuilder.makeLiteral(true));
        LogicalScan logicalScan = new LogicalScan(tableScan, "MySQL");
        logicalScan.pushDown(logicalFilter);
        assertThat(logicalScan.getRowType(), is(logicalFilter.getRowType()));
    }
    
    @Test
    void assertPushDownProject() {
        LogicalProject logicalProject = LogicalProject.create(
                tableScan, Collections.emptyList(), Collections.singletonList(rexBuilder.makeInputRef(rowType, 0)), Collections.singletonList("col"), Collections.emptySet());
        LogicalScan logicalScan = new LogicalScan(tableScan, "MySQL");
        logicalScan.pushDown(logicalProject);
        assertThat(logicalScan.getRowType(), is(logicalProject.getRowType()));
    }
    
    @Test
    void assertPeek() {
        assertNotNull(new LogicalScan(tableScan, "MySQL").peek());
    }
    
    @Test
    void assertExplainTerms() {
        LogicalScan logicalScan = new LogicalScan(tableScan, "MySQL");
        StringWriter writer = new StringWriter();
        RelWriter relWriter = new RelWriterImpl(new PrintWriter(writer), SqlExplainLevel.EXPPLAN_ATTRIBUTES, false);
        logicalScan.explainTerms(relWriter);
        relWriter.done(logicalScan);
        assertTrue(writer.toString().contains("pushDownRelBuilder"));
    }
    
    @Test
    void assertDeepEqualsSelf() {
        LogicalScan logicalScan = new LogicalScan(tableScan, "MySQL");
        assertTrue(logicalScan.deepEquals(logicalScan));
    }
    
    @Test
    void assertDeepEqualsWithNull() {
        assertFalse(new LogicalScan(tableScan, "MySQL").deepEquals(null));
    }
    
    @Test
    void assertDeepEqualsWithDifferentInstanceWithoutSharedBuilder() {
        assertFalse(new LogicalScan(tableScan, "MySQL").deepEquals(new LogicalScan(tableScan, "MySQL")));
    }
    
    @Test
    void assertDeepEqualsWithSharedBuilder() throws ReflectiveOperationException {
        LogicalScan logicalScan = new LogicalScan(tableScan, "MySQL");
        LogicalScan sharedBuilder = new LogicalScan(tableScan, "MySQL");
        MemberAccessor memberAccessor = Plugins.getMemberAccessor();
        Field builderField = LogicalScan.class.getDeclaredField("pushDownRelBuilder");
        Object pushDownRelBuilder = memberAccessor.get(builderField, logicalScan);
        memberAccessor.set(builderField, sharedBuilder, pushDownRelBuilder);
        assertTrue(logicalScan.deepEquals(sharedBuilder));
    }
    
    @Test
    void assertDeepEqualsWithDifferentClass() {
        assertFalse(new LogicalScan(tableScan, "MySQL").deepEquals(new Object()));
    }
    
    @Test
    void assertDeepEqualsWithDifferentDatabase() throws ReflectiveOperationException {
        LogicalScan logicalScan = new LogicalScan(tableScan, "MySQL");
        LogicalScan differentDatabase = new LogicalScan(tableScan, "PostgreSQL");
        MemberAccessor memberAccessor = Plugins.getMemberAccessor();
        Field builderField = LogicalScan.class.getDeclaredField("pushDownRelBuilder");
        memberAccessor.set(builderField, differentDatabase, memberAccessor.get(builderField, logicalScan));
        assertFalse(logicalScan.deepEquals(differentDatabase));
    }
    
    @Test
    void assertDeepEqualsWithDifferentHints() throws ReflectiveOperationException {
        LogicalScan logicalScan = new LogicalScan(tableScan, "MySQL");
        LogicalScan differentHints = new LogicalScan(tableScan, "MySQL");
        MemberAccessor memberAccessor = Plugins.getMemberAccessor();
        Field builderField = LogicalScan.class.getDeclaredField("pushDownRelBuilder");
        memberAccessor.set(builderField, differentHints, memberAccessor.get(builderField, logicalScan));
        memberAccessor.set(TableScan.class.getDeclaredField("hints"), differentHints, ImmutableList.of(RelHint.builder("h").build()));
        assertFalse(logicalScan.deepEquals(differentHints));
    }
    
    @Test
    void assertDeepEqualsWithDifferentRowType() throws ReflectiveOperationException {
        LogicalScan logicalScan = new LogicalScan(tableScan, "MySQL");
        LogicalScan differentRowType = new LogicalScan(tableScan, "MySQL");
        MemberAccessor memberAccessor = Plugins.getMemberAccessor();
        Field builderField = LogicalScan.class.getDeclaredField("pushDownRelBuilder");
        memberAccessor.set(builderField, differentRowType, memberAccessor.get(builderField, logicalScan));
        RelDataTypeFactory typeFactory = cluster.getTypeFactory();
        RelDataType anotherRowType = typeFactory.builder().add("col", SqlTypeName.BIGINT).build();
        memberAccessor.set(AbstractRelNode.class.getDeclaredField("rowType"), differentRowType, anotherRowType);
        assertFalse(logicalScan.deepEquals(differentRowType));
    }
    
    @Test
    void assertDeepHashCode() {
        LogicalScan logicalScan = new LogicalScan(tableScan, "MySQL");
        int expectedHashCode = Objects.hash(logicalScan.getTraitSet(), "MySQL", logicalScan.getPushDownRelBuilder(), logicalScan.getHints(), logicalScan.getRowType());
        assertThat(logicalScan.deepHashCode(), is(expectedHashCode));
    }
}
