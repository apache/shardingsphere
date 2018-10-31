/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.antler.sql.ddl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.shardingsphere.core.metadata.table.ColumnMetaData;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.metadata.table.TableMetaData;
import io.shardingsphere.core.parsing.parser.sql.ddl.DDLStatement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class AlterTableStatement extends DDLStatement {
    private final List<String> dropColumns = new ArrayList<>();

    private final Map<String, ColumnDefinition> updateColumns = new LinkedHashMap<>();

    private final List<ColumnDefinition> addColumns = new ArrayList<>();

    private boolean dropPrimaryKey;

    private String newTableName;
    
    private ShardingTableMetaData tableMetaDataMap;
    
    private TableMetaData tableMetaData;
    
    /**Get column definition.
     * @param columnName column name
     * @return column definition
     */
    public ColumnDefinition getColumnDefinitionByName(final String columnName) {
        ColumnDefinition columnDefinition = getExistColumn(columnName);
        if(null == columnDefinition) {
            columnDefinition = getFromAddColumn(columnName);
        }
        
        return columnDefinition;
    }
    
    /** Get exist column definition.
     * @param columnName column name
     * @return column definition
     */
    public ColumnDefinition getExistColumn(final String columnName) {
        TableMetaData tableMeta = tableMetaDataMap.get(this.getTables().getSingleTableName());
        if(null == tableMeta) {
            return null;
        }
        
        for(ColumnMetaData each : tableMeta.getColumnMetaData()) {
            if(columnName.equalsIgnoreCase(each.getColumnName())) {
                return new ColumnDefinition(columnName, each.getColumnType(), null, each.isPrimaryKey());
            }
        }
        
        return null;
    }
    
    /** Get column definition from current add clause.
     * @param columnName column name
     * @return column definition
     */
    private ColumnDefinition getFromAddColumn(final String columnName) {
        for(ColumnDefinition addColumn : addColumns) {
            if(addColumn.getName().equalsIgnoreCase(columnName)) {
                return addColumn;
            }
        }
        
        return null;
    }
    
}
