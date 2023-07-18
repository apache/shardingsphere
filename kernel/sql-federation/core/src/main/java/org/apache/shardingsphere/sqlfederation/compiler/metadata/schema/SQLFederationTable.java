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

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.calcite.DataContext;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.QueryProvider;
import org.apache.calcite.linq4j.Queryable;
import org.apache.calcite.linq4j.tree.Expression;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.plan.RelOptTable.ToRelContext;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.logical.LogicalTableScan;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.QueryableTable;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.Schemas;
import org.apache.calcite.schema.Statistic;
import org.apache.calcite.schema.TranslatableTable;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.sqlfederation.compiler.metadata.util.SQLFederationDataTypeUtils;
import org.apache.shardingsphere.sqlfederation.compiler.statistic.SQLFederationStatistic;
import org.apache.shardingsphere.sqlfederation.executor.enumerable.EnumerableScanExecutor;
import org.apache.shardingsphere.sqlfederation.executor.enumerable.EnumerableScanExecutorContext;

import java.lang.reflect.Type;
import java.util.Collections;

/**
 * SQL federation table.
 */
@RequiredArgsConstructor
public final class SQLFederationTable extends AbstractTable implements QueryableTable, TranslatableTable {
    
    private final ShardingSphereTable table;
    
    private final SQLFederationStatistic statistic;
    
    private final DatabaseType protocolType;
    
    @Setter
    private EnumerableScanExecutor scanExecutor;
    
    @Override
    public RelDataType getRowType(final RelDataTypeFactory typeFactory) {
        return SQLFederationDataTypeUtils.createRelDataType(table, protocolType, typeFactory);
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
     * Execute.
     *
     * @param root data context
     * @param sql sql
     * @param paramIndexes param indexes
     * @return enumerable result
     */
    public Enumerable<Object> execute(final DataContext root, final String sql, final int[] paramIndexes) {
        return scanExecutor.execute(table, new EnumerableScanExecutorContext(root, sql, paramIndexes));
    }
    
    @Override
    public String toString() {
        return "SQLFederationTable";
    }
    
    @Override
    public Statistic getStatistic() {
        return statistic;
    }
}
