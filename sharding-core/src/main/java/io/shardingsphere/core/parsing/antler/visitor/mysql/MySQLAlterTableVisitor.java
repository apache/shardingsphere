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

package io.shardingsphere.core.parsing.antler.visitor.mysql;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import io.shardingsphere.core.metadata.table.ColumnMetaData;
import io.shardingsphere.core.parsing.antler.phrase.visitor.AddPrimaryKeyVisitor;
import io.shardingsphere.core.parsing.antler.phrase.visitor.DropPrimaryKeyVisitor;
import io.shardingsphere.core.parsing.antler.phrase.visitor.RenameIndexVisitor;
import io.shardingsphere.core.parsing.antler.phrase.visitor.mysql.MySQLAddColumnVisitor;
import io.shardingsphere.core.parsing.antler.phrase.visitor.mysql.MySQLAddIndexVisitor;
import io.shardingsphere.core.parsing.antler.phrase.visitor.mysql.MySQLChangeColumnVisitor;
import io.shardingsphere.core.parsing.antler.phrase.visitor.mysql.MySQLDropIndexVisitor;
import io.shardingsphere.core.parsing.antler.phrase.visitor.mysql.MySQLModifyColumnVisitor;
import io.shardingsphere.core.parsing.antler.sql.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antler.sql.ddl.ColumnPosition;
import io.shardingsphere.core.parsing.antler.sql.ddl.mysql.MySQLAlterTableStatement;
import io.shardingsphere.core.parsing.antler.statement.visitor.AlterTableVisitor;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;

public class MySQLAlterTableVisitor extends AlterTableVisitor {
    public MySQLAlterTableVisitor() {
        addVisitor(new MySQLAddColumnVisitor());
        addVisitor(new MySQLAddIndexVisitor());
        addVisitor(new MySQLDropIndexVisitor());
        addVisitor(new RenameIndexVisitor());

        addVisitor(new AddPrimaryKeyVisitor("addConstraint"));
        addVisitor(new DropPrimaryKeyVisitor());

        addVisitor(new MySQLChangeColumnVisitor());
        addVisitor(new MySQLModifyColumnVisitor());
    }

    /**
     * Create statement.
     *
     * @return empty sql statment
     */
    protected SQLStatement newStatement() {
        return new MySQLAlterTableStatement();
    }

    /**
     * Adjust column position.
     *
     * @param alterStatement alter table statement
     * @param newColumnMeta table new column meta data
     */
    protected void adjustColumn(final AlterTableStatement alterStatement, final List<ColumnMetaData> newColumnMeta) {
        MySQLAlterTableStatement mysqlAlter = (MySQLAlterTableStatement) alterStatement;
        if (mysqlAlter.getPositionChangedColumns().isEmpty()) {
            return;
        }

        if (mysqlAlter.getPositionChangedColumns().size() > 1) {
            Collections.sort(mysqlAlter.getPositionChangedColumns());
        }

        for (ColumnPosition each : mysqlAlter.getPositionChangedColumns()) {
            if (each.getFirstColumn() != null) {
                adjustFirst(newColumnMeta, each.getFirstColumn());
            } else {
                adjustAfter(newColumnMeta, each);
            }
        }
    }

    /**
     * Adjust column to first.
     *
     * @param newColumnMeta new columns meta
     * @param columnName first column name
     */
    private void adjustFirst(final List<ColumnMetaData> newColumnMeta, final String columnName) {
        ColumnMetaData firstMeta = null;
        Iterator<ColumnMetaData> it = newColumnMeta.iterator();
        while (it.hasNext()) {
            ColumnMetaData eachMeata = it.next();
            if (eachMeata.getColumnName().equals(columnName)) {
                firstMeta = eachMeata;
                it.remove();
                break;
            }
        }

        if (null != firstMeta) {
            newColumnMeta.add(0, firstMeta);
        }
    }

    /**
     * Adjust column to after column.
     *
     * @param newColumnMeta new columns meta
     * @param columnPosition column position
     */
    private void adjustAfter(final List<ColumnMetaData> newColumnMeta, final ColumnPosition columnPosition) {
        int afterIndex = -1;
        int adjustColumnIndex = -1;
        for (int i = 0; i < newColumnMeta.size(); i++) {
            if (newColumnMeta.get(i).getColumnName().equals(columnPosition.getColumnName())) {
                adjustColumnIndex = i;
            }

            if (newColumnMeta.get(i).getColumnName().equals(columnPosition.getAfterColumn())) {
                afterIndex = i;
            }

            if (adjustColumnIndex >= 0 && afterIndex >= 0) {
                break;
            }
        }

        if (adjustColumnIndex >= 0 && afterIndex >= 0 && adjustColumnIndex != afterIndex + 1) {
            ColumnMetaData adjustColumn = newColumnMeta.remove(adjustColumnIndex);
            if (afterIndex < adjustColumnIndex) {
                afterIndex = afterIndex + 1;
            }
            newColumnMeta.add(afterIndex, adjustColumn);
        }
    }
}
