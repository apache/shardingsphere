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

package org.apache.shardingsphere.infra.executor.sql.federate.original.table;

import lombok.RequiredArgsConstructor;
import org.apache.calcite.DataContext;
import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.schema.ProjectableFilterableTable;
import org.apache.calcite.schema.Statistic;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.federate.original.FederationTableStatistic;
import org.apache.shardingsphere.infra.executor.sql.federate.original.row.FilterableRowEnumerator;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationTableMetaData;

import java.util.Collection;
import java.util.List;

/**
 * Filterable table.
 */
@RequiredArgsConstructor
public final class FilterableTable extends AbstractTable implements ProjectableFilterableTable {
    
    private final FederationTableMetaData metaData;
    
    private final FilterableTableScanExecutor executor;
    
    private final FederationTableStatistic statistic;
    
    @Override
    public RelDataType getRowType(final RelDataTypeFactory typeFactory) {
        return metaData.getRelProtoDataType().apply(typeFactory);
    }
    
    @Override
    public Enumerable<Object[]> scan(final DataContext root, final List<RexNode> filters, final int[] projects) {
        Collection<QueryResult> queryResults = executor.execute(metaData, new FilterableTableScanContext(root, filters, projects));
        return new AbstractEnumerable<Object[]>() {
            
            @Override
            public Enumerator<Object[]> enumerator() {
                return new FilterableRowEnumerator(queryResults);
            }
        };
    }
    
    @Override
    public Statistic getStatistic() {
        return statistic;
    }
}
