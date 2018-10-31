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

package io.shardingsphere.core.parsing.antler.statement.visitor;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import io.shardingsphere.core.metadata.table.ColumnMetaData;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.metadata.table.TableMetaData;
import io.shardingsphere.core.parsing.antler.phrase.visitor.DropColumnVisitor;
import io.shardingsphere.core.parsing.antler.phrase.visitor.RenameTableVisitor;
import io.shardingsphere.core.parsing.antler.phrase.visitor.TableNamesVisitor;
import io.shardingsphere.core.parsing.antler.sql.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antler.sql.ddl.ColumnDefinition;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;

public abstract class AlterTableVisitor extends AbstractStatementVisitor {

    public AlterTableVisitor() {
        addVisitor(new TableNamesVisitor());
        addVisitor(new RenameTableVisitor());
        addVisitor(new DropColumnVisitor());
    }

    /**
     * process after visit.
     *
     * @param statement sql statement
     */
    protected void postVisit(final SQLStatement statement) {
        AlterTableStatement alterStatement = (AlterTableStatement) statement;
        TableMetaData oldTableMeta = alterStatement.getTableMetaDataMap().get(alterStatement.getTables().getSingleTableName());
        if (null == oldTableMeta) {
            return;
        }

        List<ColumnMetaData> newColumnMeta = updateColumn(alterStatement, oldTableMeta);
        addColumn(alterStatement, newColumnMeta);
        adjustColumn(alterStatement, newColumnMeta);
        dropColumn(alterStatement, newColumnMeta);
        alterStatement.setTableMetaData(new TableMetaData(newColumnMeta));
    }

    /**
     * Adjust column position.
     *
     * @param alterStatement alter table statement
     * @param newColumnMeta  table new column meta data
     */
    protected void adjustColumn(final AlterTableStatement alterStatement, final List<ColumnMetaData> newColumnMeta) {

    }

    /**
     * Update column info.
     *
     * @param alterStatement alter table statement
     * @param oldTableMeta   table meta data before update
     * @return update column info
     */
    private List<ColumnMetaData> updateColumn(final AlterTableStatement alterStatement, final TableMetaData oldTableMeta) {
        List<ColumnMetaData> newColumnMeta = new LinkedList<>();
        for (ColumnMetaData each : oldTableMeta.getColumnMetaData()) {
            ColumnDefinition columnDefinition = alterStatement.getUpdateColumns().get(each.getColumnName());
            String columnName;
            String columnType;
            boolean primaryKey = false;
            if (null == columnDefinition) {
                columnName = each.getColumnName();
                columnType = each.getColumnType();
                primaryKey = each.isPrimaryKey();
            } else {
                columnName = columnDefinition.getName();
                columnType = columnDefinition.getType();
                if (columnDefinition.isPrimaryKey()) {
                    primaryKey = columnDefinition.isPrimaryKey();
                }
            }

            if (each.isPrimaryKey() && alterStatement.isDropPrimaryKey()) {
                primaryKey = false;
            }

            newColumnMeta.add(new ColumnMetaData(columnName, columnType, primaryKey));
        }

        return newColumnMeta;
    }

    /**
     * Add column meta data.
     *
     * @param alterStatement alter table statement
     * @param newColumnMeta  new column meta data
     */
    private void addColumn(final AlterTableStatement alterStatement, final List<ColumnMetaData> newColumnMeta) {
        for (ColumnDefinition each : alterStatement.getAddColumns()) {
            newColumnMeta.add(new ColumnMetaData(each.getName(), each.getType(), each.isPrimaryKey()));
        }
    }

    /**
     * Drop column meta data.
     *
     * @param alterStatement alter table statement
     * @param newColumnMeta  new column meta data
     */
    private void dropColumn(final AlterTableStatement alterStatement, final List<ColumnMetaData> newColumnMeta) {
        Iterator<ColumnMetaData> it = newColumnMeta.iterator();
        while (it.hasNext()) {
            ColumnMetaData eachMeata = it.next();
            if (alterStatement.getDropColumns().contains(eachMeata.getColumnName())) {
                it.remove();
            }
        }
    }

    /**
     * Use shardingTableMetaData create SQLStatement.
     *
     * @param shardingTableMetaData table metadata
     * @return sql statement info
     */
    protected SQLStatement newStatement(final ShardingTableMetaData shardingTableMetaData) {
        AlterTableStatement statement = (AlterTableStatement) newStatement();
        statement.setTableMetaDataMap(shardingTableMetaData);
        return statement;
    }

    /**
     * Create statement.
     *
     * @return empty sql statment
     */
    protected SQLStatement newStatement() {
        return new AlterTableStatement();
    }

}
