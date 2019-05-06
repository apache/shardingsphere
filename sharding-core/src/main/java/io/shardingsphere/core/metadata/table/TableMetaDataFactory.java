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

package io.shardingsphere.core.metadata.table;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import io.shardingsphere.core.parsing.antlr.sql.segment.definition.column.ColumnDefinitionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.definition.column.position.ColumnAfterPositionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.definition.column.position.ColumnFirstPositionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.definition.column.position.ColumnPositionSegment;
import io.shardingsphere.core.parsing.antlr.sql.statement.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antlr.sql.statement.ddl.CreateTableStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Table meta data factory.
 *
 * @author zhangliang
 */
public final class TableMetaDataFactory {
    
    /**
     * New instance of table meta data.
     * 
     * @param createTableStatement create table statement
     * @return instance of table meta data
     */
    public static TableMetaData newInstance(final CreateTableStatement createTableStatement) {
        return new TableMetaData(Lists.transform(createTableStatement.getColumnDefinitions(), new Function<ColumnDefinitionSegment, ColumnMetaData>() {
            
            @Override
            public ColumnMetaData apply(final ColumnDefinitionSegment input) {
                return new ColumnMetaData(input.getColumnName(), input.getDataType(), input.isPrimaryKey());
            }
        }));
    }
    
    /**
     * New instance of table meta data.
     *
     * @param alterTableStatement alter table statement
     * @param oldTableMetaData old table meta data
     * @return instance of table meta data
     */
    public static TableMetaData newInstance(final AlterTableStatement alterTableStatement, final TableMetaData oldTableMetaData) {
        List<ColumnMetaData> result = createColumnMetaDataList(alterTableStatement.getModifiedColumnDefinitions(), alterTableStatement.isDropPrimaryKey(), oldTableMetaData);
        result.addAll(createColumnMetaDataList(alterTableStatement.getAddedColumnDefinitions(), alterTableStatement.isDropPrimaryKey()));
        changeColumnDefinitionPositions(alterTableStatement.getChangedPositionColumns(), result);
        dropColumnDefinitions(alterTableStatement.getDroppedColumnNames(), result);
        return new TableMetaData(result);
    }
    
    private static List<ColumnMetaData> createColumnMetaDataList(
            final Map<String, ColumnDefinitionSegment> modifiedColumnDefinitions, final boolean dropPrimaryKey, final TableMetaData oldTableMetaData) {
        List<ColumnMetaData> result = new LinkedList<>();
        for (ColumnMetaData each : oldTableMetaData.getColumns().values()) {
            ColumnMetaData columnMetaData;
            if (modifiedColumnDefinitions.containsKey(each.getColumnName())) {
                ColumnDefinitionSegment columnDefinition = modifiedColumnDefinitions.get(each.getColumnName());
                columnMetaData = new ColumnMetaData(columnDefinition.getColumnName(), columnDefinition.getDataType(), !dropPrimaryKey && columnDefinition.isPrimaryKey());
            } else {
                columnMetaData = new ColumnMetaData(each.getColumnName(), each.getDataType(), !dropPrimaryKey && each.isPrimaryKey());
            }
            result.add(columnMetaData);
        }
        return result;
    }
    
    private static List<ColumnMetaData> createColumnMetaDataList(final Collection<ColumnDefinitionSegment> addedColumnDefinitions, final boolean dropPrimaryKey) {
        List<ColumnMetaData> result = new LinkedList<>();
        for (ColumnDefinitionSegment each : addedColumnDefinitions) {
            result.add(new ColumnMetaData(each.getColumnName(), each.getDataType(), !dropPrimaryKey && each.isPrimaryKey()));
        }
        return result;
    }
    
    private static void changeColumnDefinitionPositions(final Collection<ColumnPositionSegment> changedPositionColumns, final List<ColumnMetaData> columnMetaDataList) {
        for (ColumnPositionSegment each : changedPositionColumns) {
            if (each instanceof ColumnFirstPositionSegment) {
                changeColumnDefinitionPosition((ColumnFirstPositionSegment) each, columnMetaDataList);
            } else {
                changeColumnDefinitionPosition((ColumnAfterPositionSegment) each, columnMetaDataList);
            }
        }
    }
    
    private static void changeColumnDefinitionPosition(final ColumnFirstPositionSegment columnFirstPositionSegment, final List<ColumnMetaData> columnMetaDataList) {
        Optional<ColumnMetaData> columnMetaData = find(columnFirstPositionSegment.getColumnName(), columnMetaDataList);
        if (columnMetaData.isPresent()) {
            columnMetaDataList.remove(columnMetaData.get());
            columnMetaDataList.add(0, columnMetaData.get());
        }
    }
    
    private static void changeColumnDefinitionPosition(final ColumnAfterPositionSegment columnAfterPositionSegment, final List<ColumnMetaData> columnMetaDataList) {
        Optional<ColumnMetaData> columnMetaData = find(columnAfterPositionSegment.getColumnName(), columnMetaDataList);
        Optional<ColumnMetaData> afterColumnMetaData = find(columnAfterPositionSegment.getAfterColumnName(), columnMetaDataList);
        if (columnMetaData.isPresent() && afterColumnMetaData.isPresent()) {
            columnMetaDataList.remove(columnMetaData.get());
            columnMetaDataList.add(columnMetaDataList.indexOf(afterColumnMetaData.get()) + 1, columnMetaData.get());
        }
    }
    
    private static Optional<ColumnMetaData> find(final String columnName, final List<ColumnMetaData> columnMetaDataList) {
        for (ColumnMetaData each : columnMetaDataList) {
            if (columnName.equals(each.getColumnName())) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
    
    private static void dropColumnDefinitions(final Collection<String> droppedColumnNames, final List<ColumnMetaData> columnMetaDataList) {
        List<ColumnMetaData> droppedColumnMetaDataList = new LinkedList<>();
        for (ColumnMetaData each : columnMetaDataList) {
            if (droppedColumnNames.contains(each.getColumnName())) {
                droppedColumnMetaDataList.add(each);
            }
        }
        columnMetaDataList.removeAll(droppedColumnMetaDataList);
    }
}
