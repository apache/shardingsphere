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

package org.apache.shardingsphere.sqlfederation.optimizer.metadata.filter;

import lombok.RequiredArgsConstructor;
import org.apache.calcite.DataContext;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.schema.ProjectableFilterableTable;
import org.apache.calcite.schema.Statistic;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.sqlfederation.optimizer.executor.FilterableScanNodeExecutorContext;
import org.apache.shardingsphere.sqlfederation.optimizer.executor.TableScanExecutor;
import org.apache.shardingsphere.sqlfederation.optimizer.metadata.statistic.FederationStatistic;
import org.apache.shardingsphere.sqlfederation.optimizer.util.SQLFederationDataTypeUtil;

import java.util.List;

/**
 * Filterable table.
 */
@RequiredArgsConstructor
public final class FilterableTable extends AbstractTable implements ProjectableFilterableTable {
    
    private final ShardingSphereTable table;
    
    private final TableScanExecutor executor;
    
    private final FederationStatistic statistic;
    
    private final DatabaseType protocolType;
    
    @Override
    public RelDataType getRowType(final RelDataTypeFactory typeFactory) {
        return SQLFederationDataTypeUtil.createRelDataType(table, protocolType, typeFactory);
    }
    
    @Override
    public Enumerable<Object[]> scan(final DataContext root, final List<RexNode> filters, final int[] projects) {
        return executor.execute(table, new FilterableScanNodeExecutorContext(root, filters, projects));
    }
    
    @Override
    public Statistic getStatistic() {
        return statistic;
    }
}
