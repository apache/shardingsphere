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

package org.apache.shardingsphere.infra.optimize.core.metadata;

import lombok.Getter;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelDataTypeImpl;
import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.rel.type.RelProtoDataType;
import org.apache.calcite.sql.type.SqlTypeFactoryImpl;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.schema.builder.loader.TableMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Federate table metadata.
 */
@Getter
public final class FederateTableMetadata {
    
    private final String name;
    
    private final RelProtoDataType relProtoDataType;
    
    public FederateTableMetadata(final String name, final TableMetaData tableMetaData) {
        this.name = name;
        relProtoDataType = createRelDataType(tableMetaData);
    }
    
    /**
     * Please fix me.
     * @deprecated Remove this constructor.
     */
    @Deprecated
    public FederateTableMetadata(final String name, final Map<String, DataSource> dataSources, final Map<String, Collection<String>> dataSourceRules,
                                 final Collection<DataNode> tableDataNodes, final DatabaseType databaseType) throws SQLException {
        this.name = name;
        relProtoDataType = createRelDataType(createTableMetaData(dataSources, dataSourceRules, tableDataNodes, databaseType));
    }
    
    private TableMetaData createTableMetaData(final Map<String, DataSource> dataSources, final Map<String, Collection<String>> dataSourceRules,
                                              final Collection<DataNode> tableDataNodes, final DatabaseType databaseType) throws SQLException {
        DataNode dataNode = tableDataNodes.iterator().next();
        Optional<TableMetaData> tableMetaData =
                TableMetaDataLoader.load(getActualDataSource(dataSources, dataSourceRules, dataNode.getDataSourceName()), dataNode.getTableName(), databaseType);
        return tableMetaData.orElseGet(TableMetaData::new);
    }
    
    private RelProtoDataType createRelDataType(final TableMetaData tableMetaData) {
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
}
