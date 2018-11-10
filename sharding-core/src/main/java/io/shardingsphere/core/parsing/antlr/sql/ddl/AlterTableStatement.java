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

package io.shardingsphere.core.parsing.antlr.sql.ddl;

import com.google.common.base.Optional;
import io.shardingsphere.core.metadata.table.ColumnMetaData;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.metadata.table.TableMetaData;
import io.shardingsphere.core.parsing.parser.sql.ddl.DDLStatement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Alter table statement.
 * 
 * @author duhongjun
 */
@Getter
@Setter
@ToString(callSuper = true)
public class AlterTableStatement extends DDLStatement {
    
    private final List<ColumnDefinition> addColumns = new LinkedList<>();
    
    private final List<String> dropColumns = new LinkedList<>();
    
    private final Map<String, ColumnDefinition> updateColumns = new LinkedHashMap<>();
    
    private boolean dropPrimaryKey;
    
    private String newTableName;
    
    private ShardingTableMetaData tableMetaDataMap;
    
    private TableMetaData tableMetaData;
    
    /**
     * Get column definition.
     *
     * @param columnName column name
     * @return column definition
     */
    public Optional<ColumnDefinition> getColumnDefinitionByName(final String columnName) {
        Optional<ColumnDefinition> result = findColumnDefinition(columnName);
        return result.isPresent() ? result : findColumnDefinitionFromCurrentAddClause(columnName);
    }
    
    /**
     * Find column definition.
     *
     * @param columnName column name
     * @return column definition
     */
    public Optional<ColumnDefinition> findColumnDefinition(final String columnName) {
        if (!tableMetaDataMap.containsTable(getTables().getSingleTableName())) {
            return Optional.absent();
        }
        for (ColumnMetaData each : tableMetaDataMap.get(getTables().getSingleTableName()).getColumnMetaData()) {
            if (columnName.equalsIgnoreCase(each.getColumnName())) {
                return Optional.of(new ColumnDefinition(columnName, each.getColumnType(), null, each.isPrimaryKey()));
            }
        }
        return Optional.absent();
    }
    
    private Optional<ColumnDefinition> findColumnDefinitionFromCurrentAddClause(final String columnName) {
        for (ColumnDefinition each : addColumns) {
            if (each.getName().equalsIgnoreCase(columnName)) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
}
