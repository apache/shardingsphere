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

package org.apache.shardingsphere.sqlfederation.compiler.metadata.schema;

import org.apache.calcite.DataContext;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.linq4j.QueryProvider;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.plan.volcano.VolcanoPlanner;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.TableModify;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sqlfederation.compiler.implementor.ScanImplementor;
import org.apache.shardingsphere.sqlfederation.compiler.implementor.ScanImplementorContext;
import org.apache.shardingsphere.sqlfederation.compiler.implementor.enumerator.EmptyDataRowEnumerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SQLFederationTableTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private final ShardingSphereTable table = createTable();
    
    private ShardingSphereTable createTable() {
        ShardingSphereColumn column = new ShardingSphereColumn("id", Types.INTEGER, true, false, false, true, false, true);
        Collection<ShardingSphereColumn> columns = Collections.singletonList(column);
        return new ShardingSphereTable("foo_tbl", columns, Collections.emptyList(), Collections.emptyList());
    }
    
    @AfterEach
    void tearDown() {
        new SQLFederationTable(table, databaseType).clearScanImplementor();
    }
    
    @Test
    void assertGetRowType() {
        RelDataTypeFactory typeFactory = new JavaTypeFactoryImpl();
        SQLFederationTable actual = new SQLFederationTable(table, databaseType);
        RelDataType rowType = actual.getRowType(typeFactory);
        assertThat(rowType.getFieldList().size(), is(1));
    }
    
    @Test
    void assertGetExpression() {
        assertNotNull(new SQLFederationTable(table, databaseType).getExpression(mock(), "foo_tbl", Object.class));
    }
    
    @Test
    void assertGetElementType() {
        assertThat(new SQLFederationTable(table, databaseType).getElementType(), is(Object[].class));
    }
    
    @Test
    void assertAsQueryableThrowsException() {
        assertThrows(UnsupportedSQLOperationException.class, () -> new SQLFederationTable(table, databaseType).asQueryable(mock(QueryProvider.class), mock(SchemaPlus.class), "foo_tbl"));
    }
    
    @Test
    void assertToRelBuildsLogicalTableScan() {
        RelOptTable.ToRelContext context = mock(RelOptTable.ToRelContext.class);
        when(context.getCluster()).thenReturn(RelOptCluster.create(new VolcanoPlanner(), new RexBuilder(new JavaTypeFactoryImpl())));
        assertThat(new SQLFederationTable(table, databaseType).toRel(context, mock()), isA(RelNode.class));
    }
    
    @Test
    void assertImplementWithoutScanImplementorReturnsEmptyEnumerator() {
        Enumerable<Object> actual = new SQLFederationTable(table, databaseType).implement(mock(DataContext.class), "SELECT 1", new int[0]);
        Enumerator<Object> enumerator = actual.enumerator();
        assertThat(enumerator, isA(EmptyDataRowEnumerator.class));
        assertFalse(enumerator.moveNext());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertImplementUsesScanImplementor() {
        SQLFederationTable federationTable = new SQLFederationTable(table, databaseType);
        Enumerable<Object> expectedEnumerable = mock(Enumerable.class);
        ScanImplementor scanImplementor = mock(ScanImplementor.class);
        when(scanImplementor.implement(any(ShardingSphereTable.class), any(ScanImplementorContext.class))).thenReturn(expectedEnumerable);
        federationTable.setScanImplementor(scanImplementor);
        Enumerable<Object> actual = federationTable.implement(mock(DataContext.class), "SELECT 1", new int[0]);
        assertThat(actual, is(expectedEnumerable));
        verify(scanImplementor).implement(any(ShardingSphereTable.class), any(ScanImplementorContext.class));
    }
    
    @Test
    void assertGetModifiableCollectionThrowsException() {
        assertThrows(UnsupportedOperationException.class, () -> new SQLFederationTable(table, databaseType).getModifiableCollection());
    }
    
    @Test
    void assertToModificationRelBuildsLogicalTableModify() {
        RelOptCluster relOptCluster = RelOptCluster.create(new VolcanoPlanner(), new RexBuilder(new JavaTypeFactoryImpl()));
        RelNode relNode = mock(RelNode.class);
        when(relNode.getCluster()).thenReturn(relOptCluster);
        assertThrows(IllegalArgumentException.class, () -> new SQLFederationTable(table, databaseType).toModificationRel(relOptCluster, mock(), mock(), relNode,
                TableModify.Operation.UPDATE, Collections.singletonList("id"), Collections.emptyList(), false));
    }
    
    @Test
    void assertToStringValue() {
        assertThat(new SQLFederationTable(table, databaseType).toString(), is("SQLFederationTable"));
    }
}
