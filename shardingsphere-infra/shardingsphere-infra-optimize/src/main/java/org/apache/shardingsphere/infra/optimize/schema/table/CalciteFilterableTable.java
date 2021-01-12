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

package org.apache.shardingsphere.infra.optimize.schema.table;

import org.apache.calcite.DataContext;
import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.rel.type.RelProtoDataType;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.schema.ProjectableFilterableTable;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.optimize.schema.row.CalciteRowEnumerator;
import org.apache.shardingsphere.infra.optimize.schema.row.CalciteRowExecutor;
import org.apache.shardingsphere.infra.optimize.schema.table.execute.CalciteExecutionContextGenerator;
import org.apache.shardingsphere.infra.optimize.schema.table.execute.CalciteExecutionSQLGenerator;

import java.util.List;

/**
 * Calcite filterable Table.
 *
 */
public final class CalciteFilterableTable extends AbstractCalciteTable implements ProjectableFilterableTable {
    
    public CalciteFilterableTable(final String name, final TableMetaData tableMetaData, final RelProtoDataType relProtoDataType,
                                  final CalciteRowExecutor executor) {
        super(name, tableMetaData, relProtoDataType, executor);
    }
    
    @Override
    public Enumerable<Object[]> scan(final DataContext root, final List<RexNode> filters, final int[] projects) {
        return new AbstractEnumerable<Object[]>() {

            @Override
            public Enumerator<Object[]> enumerator() {
                CalciteExecutionContextGenerator generator =
                        new CalciteExecutionContextGenerator(getName(), getExecutor().getInitialExecutionContext(), new CalciteExecutionSQLGenerator(root, filters, projects));
                return new CalciteRowEnumerator(getExecutor().execute(generator.generate()));
            }
        };
    }
}
