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

package org.apache.shardingsphere.sqlfederation.optimizer.metadata.translatable;

import lombok.RequiredArgsConstructor;
import org.apache.calcite.DataContext;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.QueryProvider;
import org.apache.calcite.linq4j.Queryable;
import org.apache.calcite.linq4j.tree.Expression;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.plan.RelOptTable.ToRelContext;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.QueryableTable;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.Schemas;
import org.apache.calcite.schema.Statistic;
import org.apache.calcite.schema.TranslatableTable;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.sqlfederation.optimizer.executor.TableScanExecutor;
import org.apache.shardingsphere.sqlfederation.optimizer.executor.TranslatableScanNodeExecutorContext;
import org.apache.shardingsphere.sqlfederation.optimizer.metadata.statistic.FederationStatistic;
import org.apache.shardingsphere.sqlfederation.optimizer.util.SQLFederationDataTypeUtil;

import java.lang.reflect.Type;

/**
 * Federation translatable table.
 */
@RequiredArgsConstructor
public final class FederationTranslatableTable extends AbstractTable implements QueryableTable, TranslatableTable {
    
    private final ShardingSphereTable table;
    
    private final TableScanExecutor executor;
    
    private final FederationStatistic statistic;
    
    @Override
    public RelDataType getRowType(final RelDataTypeFactory typeFactory) {
        return SQLFederationDataTypeUtil.createRelDataType(table, typeFactory);
    }
    
    /**
     * Execute filter and project when query the federation translatable table.
     *
     * @param root data context
     * @param filterValues right value in filter condition
     * @param projects fields to be projected
     * @return enumerable result
     */
    public Enumerable<Object[]> projectAndFilter(final DataContext root, final String[] filterValues, final int[] projects) {
        return executor.execute(table, new TranslatableScanNodeExecutorContext(root, filterValues, projects));
    }
    
    /**
     * Execute filter and project when query the federation translatable table.
     *
     * @param root data context
     * @param projects fields to be projected
     * @return enumerable result
     */
    public Enumerable<Object[]> project(final DataContext root, final int[] projects) {
        return executor.execute(table, new TranslatableScanNodeExecutorContext(root, null, projects));
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
        int[] fields = getFieldIndexes(relOptTable.getRowType().getFieldCount());
        return new TranslatableTableScan(context.getCluster(), relOptTable, this, fields);
    }
    
    @Override
    public String toString() {
        return "FederationTranslatableTable";
    }
    
    @Override
    public Statistic getStatistic() {
        return statistic;
    }
    
    private int[] getFieldIndexes(final int fieldCount) {
        int[] result = new int[fieldCount];
        for (int index = 0; index < fieldCount; index++) {
            result[index] = index;
        }
        return result;
    }
}
