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

package org.apache.shardingsphere.infra.optimizer.schema.table;

import lombok.Getter;
import lombok.Setter;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.plan.RelOptTable.ToRelContext;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.logical.LogicalTableScan;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.TranslatableTable;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public final class ShardingSphereCalciteTable extends AbstractTable
        implements TranslatableTable {

    private String tableName;

    private TableMetaData tableMetaData;

    public ShardingSphereCalciteTable(final String tableName, final TableMetaData tableMetaData) {
        this.tableName = tableName;
        this.tableMetaData = tableMetaData;
    }

    @Override
    public RelNode toRel(final ToRelContext context, final RelOptTable relOptTable) {
        return LogicalTableScan.create(context.getCluster(), relOptTable, Collections.emptyList());
    }

    @Override
    public RelDataType getRowType(final RelDataTypeFactory typeFactory) {
        Map<String, ColumnMetaData> map = tableMetaData.getColumns();
        List<Map.Entry<String, RelDataType>> columnDataTypes = new ArrayList<>();
        for (Map.Entry<String, ColumnMetaData> entry : map.entrySet()) {
            ColumnMetaData columnMetaData = entry.getValue();
            SqlTypeName calciteSqlType = SqlTypeName.getNameForJdbcType(columnMetaData.getDataType());
            // TODO other extension type
            if (calciteSqlType == null) {
                throw new RuntimeException("unsupported column type");
            }
            // TODO to be replaced by RelDataTypeFactory.createSqlType(org.apache.calcite.sql.type.SqlTypeName, int, int)
            RelDataType calciteType = typeFactory.createSqlType(calciteSqlType);
            // TODO to add Collation and nullable typeFactory.createTypeWithNullability()
            columnDataTypes.add(new SimpleEntry<>(entry.getKey(), calciteType));
        }
        
        return typeFactory.createStructType(columnDataTypes);
    }
}
