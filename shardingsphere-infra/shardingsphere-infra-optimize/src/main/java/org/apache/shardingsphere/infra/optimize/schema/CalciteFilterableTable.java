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

package org.apache.shardingsphere.infra.optimize.schema;

import com.google.common.base.Joiner;
import org.apache.calcite.DataContext;
import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.schema.ProjectableFilterableTable;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Calcite filterable Table.
 *
 */
public final class CalciteFilterableTable extends AbstractCalciteTable implements ProjectableFilterableTable {
    
    private final Map<DataNode, String> tableQuerySQLs = new LinkedMap<>();
    
    public CalciteFilterableTable(final Map<String, DataSource> dataSources, final Collection<DataNode> dataNodes, final DatabaseType databaseType) throws SQLException {
        super(dataSources, dataNodes, databaseType);
        String columns = Joiner.on(",").join(getTableMetaData().getColumns().keySet());
        for (DataNode each : dataNodes) {
            tableQuerySQLs.put(each, String.format("SELECT %s FROM %s", columns, each.getTableName()));
        }
    }
    
    @Override
    public Enumerable<Object[]> scan(final DataContext root, final List<RexNode> filters, final int[] projects) {
        // TODO : use projects and filters
        return new AbstractEnumerable<Object[]>() {

            @Override
            public Enumerator<Object[]> enumerator() {
                return new CalciteRowEnumerator(getResultSets());
            }
        };
    }
    
    private Collection<ResultSet> getResultSets() {
        Collection<ResultSet> resultSets = new LinkedList<>();
        for (Entry<DataNode, String> entry : tableQuerySQLs.entrySet()) {
            resultSets.add(getResultSet(entry));
        }
        return resultSets;
    }
    
    private ResultSet getResultSet(final Entry<DataNode, String> tableSQL) {
        try {
            Statement statement = getDataSources().get(tableSQL.getKey().getDataSourceName()).getConnection().createStatement();
            return statement.executeQuery(tableSQL.getValue());
        } catch (final SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
}
