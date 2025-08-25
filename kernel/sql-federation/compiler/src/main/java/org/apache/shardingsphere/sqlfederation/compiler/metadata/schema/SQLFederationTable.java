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

import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.RequiredArgsConstructor;
import org.apache.calcite.DataContext;
import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.linq4j.QueryProvider;
import org.apache.calcite.linq4j.Queryable;
import org.apache.calcite.linq4j.tree.Expression;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.plan.RelOptTable.ToRelContext;
import org.apache.calcite.prepare.Prepare.CatalogReader;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.TableModify;
import org.apache.calcite.rel.core.TableModify.Operation;
import org.apache.calcite.rel.logical.LogicalTableModify;
import org.apache.calcite.rel.logical.LogicalTableScan;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.schema.ModifiableTable;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.Schemas;
import org.apache.calcite.schema.TranslatableTable;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.sqlfederation.compiler.implementor.ScanImplementor;
import org.apache.shardingsphere.sqlfederation.compiler.implementor.ScanImplementorContext;
import org.apache.shardingsphere.sqlfederation.compiler.implementor.enumerator.EmptyDataRowEnumerator;
import org.apache.shardingsphere.sqlfederation.compiler.sql.type.SQLFederationDataTypeBuilder;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * SQL federation table.
 */
@RequiredArgsConstructor
public final class SQLFederationTable extends AbstractTable implements ModifiableTable, TranslatableTable {
    
    private static final TransmittableThreadLocal<ScanImplementor> SCAN_IMPLEMENTOR_HOLDER = new TransmittableThreadLocal<>();
    
    private final ShardingSphereTable table;
    
    private final DatabaseType protocolType;
    
    @Override
    public RelDataType getRowType(final RelDataTypeFactory typeFactory) {
        return SQLFederationDataTypeBuilder.build(table, protocolType, typeFactory);
    }
    
    @Override
    public Expression getExpression(final SchemaPlus schema, final String tableName, final Class clazz) {
        return Schemas.tableExpression(schema, getElementType(), tableName, clazz);
    }
    
    @Override
    public Type getElementType() {
        return Object[].class;
    }
    
    @Override
    public <T> Queryable<T> asQueryable(final QueryProvider queryProvider, final SchemaPlus schema, final String tableName) {
        throw new UnsupportedSQLOperationException("asQueryable");
    }
    
    @Override
    public RelNode toRel(final ToRelContext context, final RelOptTable relOptTable) {
        return LogicalTableScan.create(context.getCluster(), relOptTable, Collections.emptyList());
    }
    
    /**
     * Implement.
     *
     * @param root data context
     * @param sql sql
     * @param paramIndexes param indexes
     * @return enumerable result
     */
    @SuppressWarnings("unused")
    public Enumerable<Object> implement(final DataContext root, final String sql, final int[] paramIndexes) {
        if (null == SCAN_IMPLEMENTOR_HOLDER.get()) {
            return createEmptyEnumerable();
        }
        return SCAN_IMPLEMENTOR_HOLDER.get().implement(table, new ScanImplementorContext(root, sql, paramIndexes));
    }
    
    private AbstractEnumerable<Object> createEmptyEnumerable() {
        return new AbstractEnumerable<Object>() {
            
            @Override
            public Enumerator<Object> enumerator() {
                return new EmptyDataRowEnumerator();
            }
        };
    }
    
    @Override
    public String toString() {
        return "SQLFederationTable";
    }
    
    @Override
    public Collection<Object[]> getModifiableCollection() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public TableModify toModificationRel(final RelOptCluster relOptCluster, final RelOptTable table, final CatalogReader schema,
                                         final RelNode relNode, final Operation operation, final List<String> updateColumnList,
                                         final List<RexNode> sourceExpressionList, final boolean flattened) {
        return LogicalTableModify.create(table, schema, relNode, operation, updateColumnList, sourceExpressionList, flattened);
    }
    
    /**
     * Set scan implementor.
     *
     * @param scanImplementor scan implementor
     */
    public void setScanImplementor(final ScanImplementor scanImplementor) {
        SCAN_IMPLEMENTOR_HOLDER.set(scanImplementor);
    }
    
    /**
     * Clear scan implementor.
     */
    public void clearScanImplementor() {
        SCAN_IMPLEMENTOR_HOLDER.remove();
    }
}
