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
import com.google.common.collect.Lists;
import io.shardingsphere.core.parsing.antlr.sql.segment.definition.column.ColumnDefinitionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.definition.column.position.ColumnAfterPositionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.definition.column.position.ColumnFirstPositionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.definition.column.position.ColumnPositionSegment;
import io.shardingsphere.core.parsing.antlr.sql.statement.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antlr.sql.statement.ddl.CreateTableStatement;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
        List<ColumnMetaData> columnMetaDataList = createNewColumnMetaDataList(alterTableStatement, oldTableMetaData);
        fillAddedColumnDefinitions(alterTableStatement, columnMetaDataList);
        changeColumnDefinitionPositions(alterTableStatement, columnMetaDataList);
        dropColumnDefinitions(alterTableStatement, columnMetaDataList);
        return new TableMetaData(columnMetaDataList);
    }
    
    private static List<ColumnMetaData> createNewColumnMetaDataList(final AlterTableStatement alterTableStatement, final TableMetaData oldTableMetaData) {
        List<ColumnMetaData> result = new LinkedList<>();
        for (ColumnMetaData each : oldTableMetaData.getColumnMetaDataList()) {
            String columnName;
            String dataType;
            boolean primaryKey;
            if (alterTableStatement.getModifiedColumnDefinitions().containsKey(each.getColumnName())) {
                ColumnDefinitionSegment modifiedColumnDefinition = alterTableStatement.getModifiedColumnDefinitions().get(each.getColumnName());
                columnName = modifiedColumnDefinition.getColumnName();
                dataType = modifiedColumnDefinition.getDataType();
                primaryKey = !alterTableStatement.isDropPrimaryKey() && modifiedColumnDefinition.isPrimaryKey();
            } else {
                columnName = each.getColumnName();
                dataType = each.getDataType();
                primaryKey = !alterTableStatement.isDropPrimaryKey() && each.isPrimaryKey();
            }
            result.add(new ColumnMetaData(columnName, dataType, primaryKey));
        }
        return result;
    }
    
    private static void fillAddedColumnDefinitions(final AlterTableStatement alterTableStatement, final List<ColumnMetaData> columnMetaDataList) {
        for (ColumnDefinitionSegment each : alterTableStatement.getAddedColumnDefinitions()) {
            columnMetaDataList.add(new ColumnMetaData(each.getColumnName(), each.getDataType(), !alterTableStatement.isDropPrimaryKey() && each.isPrimaryKey()));
        }
    }
    
    private static void changeColumnDefinitionPositions(final AlterTableStatement alterTableStatement, final List<ColumnMetaData> columnMetaDataList) {
        for (ColumnPositionSegment each : alterTableStatement.getChangedPositionColumns()) {
            if (each instanceof ColumnFirstPositionSegment) {
                adjustFirst(columnMetaDataList, (ColumnFirstPositionSegment) each);
            } else {
                adjustAfter(columnMetaDataList, (ColumnAfterPositionSegment) each);
            }
        }
    }
    
    private static void adjustFirst(final List<ColumnMetaData> columnMetaDataList, final ColumnFirstPositionSegment columnFirstPositionSegment) {
        ColumnMetaData firstColumnMetaData = null;
        Iterator<ColumnMetaData> iterator = columnMetaDataList.iterator();
        while (iterator.hasNext()) {
            ColumnMetaData each = iterator.next();
            if (each.getColumnName().equals(columnFirstPositionSegment.getColumnName())) {
                firstColumnMetaData = each;
                iterator.remove();
                break;
            }
        }
        if (null != firstColumnMetaData) {
            columnMetaDataList.add(0, firstColumnMetaData);
        }
    }
    
    private static void adjustAfter(final List<ColumnMetaData> columnMetaDataList, final ColumnAfterPositionSegment columnAfterPositionSegment) {
        int afterIndex = -1;
        int adjustColumnIndex = -1;
        for (int i = 0; i < columnMetaDataList.size(); i++) {
            if (columnMetaDataList.get(i).getColumnName().equals(columnAfterPositionSegment.getColumnName())) {
                adjustColumnIndex = i;
            }
            if (columnMetaDataList.get(i).getColumnName().equals(columnAfterPositionSegment.getAfterColumnName())) {
                afterIndex = i;
            }
            if (adjustColumnIndex >= 0 && afterIndex >= 0) {
                break;
            }
        }
        if (adjustColumnIndex >= 0 && afterIndex >= 0 && adjustColumnIndex != afterIndex + 1) {
            ColumnMetaData adjustColumnMetaData = columnMetaDataList.remove(adjustColumnIndex);
            if (afterIndex < adjustColumnIndex) {
                afterIndex = afterIndex + 1;
            }
            columnMetaDataList.add(afterIndex, adjustColumnMetaData);
        }
    }
    
    private static void dropColumnDefinitions(final AlterTableStatement alterTableStatement, final List<ColumnMetaData> newColumnMetaData) {
        Iterator<ColumnMetaData> iterator = newColumnMetaData.iterator();
        while (iterator.hasNext()) {
            ColumnMetaData each = iterator.next();
            if (alterTableStatement.getDropColumnNames().contains(each.getColumnName())) {
                iterator.remove();
            }
        }
    }
}
