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

package org.apache.shardingsphere.infra.optimize.schema.generator;

import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelDataTypeImpl;
import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.rel.type.RelProtoDataType;
import org.apache.calcite.schema.Table;
import org.apache.calcite.sql.type.SqlTypeFactoryImpl;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.schema.builder.loader.TableMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.optimize.schema.row.CalciteRowExecutor;
import org.apache.shardingsphere.infra.optimize.schema.table.CalciteFilterableTable;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Calcite logic table generator.
 */
public final class CalciteLogicTableGenerator {
    
    private final String name;
    
    private final TableMetaData tableMetaData;
    
    private final RelProtoDataType relProtoDataType;
    
    public CalciteLogicTableGenerator(final String name, final Map<String, DataSource> dataSources, final Map<String, Collection<String>> dataSourceRules,
                                      final Collection<DataNode> tableDataNodes, final DatabaseType databaseType) throws SQLException {
        this.name = name;
        tableMetaData = createTableMetaData(dataSources, dataSourceRules, tableDataNodes, databaseType);
        relProtoDataType = createRelDataType();
    }
    
    private TableMetaData createTableMetaData(final Map<String, DataSource> dataSources, final Map<String, Collection<String>> dataSourceRules,
                                              final Collection<DataNode> tableDataNodes, final DatabaseType databaseType) throws SQLException {
        DataNode dataNode = tableDataNodes.iterator().next();
        Optional<TableMetaData> tableMetaData =
                TableMetaDataLoader.load(getActualDataSource(dataSources, dataSourceRules, dataNode.getDataSourceName()), dataNode.getTableName(), databaseType);
        return tableMetaData.orElseGet(TableMetaData::new);
    }
    
    private RelProtoDataType createRelDataType() {
        RelDataTypeFactory typeFactory = new SqlTypeFactoryImpl(RelDataTypeSystem.DEFAULT);
        RelDataTypeFactory.Builder fieldInfo = typeFactory.builder();
        for (Map.Entry<String, ColumnMetaData> entry : tableMetaData.getColumns().entrySet()) {
            SqlTypeName sqlTypeName = SqlTypeName.getNameForJdbcType(entry.getValue().getDataType());
            fieldInfo.add(entry.getKey(), null == sqlTypeName ? typeFactory.createUnknownType() : typeFactory.createTypeWithNullability(typeFactory.createSqlType(sqlTypeName), true));
        }
        return RelDataTypeImpl.proto(fieldInfo.build());
    }
    
    private DataSource getActualDataSource(final Map<String, DataSource> dataSources,
                                                   final Map<String, Collection<String>> dataSourceRules, final String logicDataSource) {
        String result = logicDataSource;
        if (dataSourceRules.containsKey(logicDataSource)) {
            result = dataSourceRules.get(logicDataSource).iterator().next();
        }
        return dataSources.get(result);
    }
    
    /**
     * Create.
     *
     * @param executor executor
     * @return table
     */
    public Table create(final CalciteRowExecutor executor) {
        return new CalciteFilterableTable(name, tableMetaData, relProtoDataType, executor);
    }
}
