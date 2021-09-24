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

package org.apache.shardingsphere.infra.executor.sql.federate.filterable.table;

import org.apache.calcite.DataContext;
import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.schema.ProjectableFilterableTable;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.federate.filterable.FilterableTableScanContext;
import org.apache.shardingsphere.infra.executor.sql.federate.filterable.row.FilterableRowEnumerator;
import org.apache.shardingsphere.infra.executor.sql.federate.AbstractFederationTable;
import org.apache.shardingsphere.infra.optimize.metadata.FederationTableMetaData;

import java.util.Collection;
import java.util.List;

/**
 * Filterable Table.
 */
public final class FilterableTable extends AbstractFederationTable implements ProjectableFilterableTable {
    
    private final FilterableTableScanExecutor executor;
    
    public FilterableTable(final FederationTableMetaData metadata, final FilterableTableScanExecutor executor) {
        super(metadata);
        this.executor = executor;
    }
    
    @Override
    public Enumerable<Object[]> scan(final DataContext root, final List<RexNode> filters, final int[] projects) {
        Collection<QueryResult> queryResults = executor.execute(getMetaData(), new FilterableTableScanContext(root, filters, projects));
        return new AbstractEnumerable<Object[]>() {
            
            @Override
            public Enumerator<Object[]> enumerator() {
                return new FilterableRowEnumerator(queryResults);
            }
        };
    }
}
