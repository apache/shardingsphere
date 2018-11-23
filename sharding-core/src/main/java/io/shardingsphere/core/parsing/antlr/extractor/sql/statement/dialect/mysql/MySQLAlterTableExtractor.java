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

package io.shardingsphere.core.parsing.antlr.extractor.sql.statement.dialect.mysql;

import io.shardingsphere.core.metadata.table.ColumnMetaData;
import io.shardingsphere.core.parsing.antlr.extractor.sql.segment.AddPrimaryKeyExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.sql.segment.DropPrimaryKeyExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.sql.segment.RenameIndexExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.sql.segment.RuleName;
import io.shardingsphere.core.parsing.antlr.extractor.sql.segment.dialect.mysql.MySQLAddColumnExtractHandler;
import io.shardingsphere.core.parsing.antlr.extractor.sql.segment.dialect.mysql.MySQLAddIndexExtractHandler;
import io.shardingsphere.core.parsing.antlr.extractor.sql.segment.dialect.mysql.MySQLChangeColumnExtractHandler;
import io.shardingsphere.core.parsing.antlr.extractor.sql.segment.dialect.mysql.MySQLDropIndexExtractHandler;
import io.shardingsphere.core.parsing.antlr.extractor.sql.segment.dialect.mysql.MySQLModifyColumnExtractHandler;
import io.shardingsphere.core.parsing.antlr.extractor.sql.statement.AlterTableExtractor;
import io.shardingsphere.core.parsing.antlr.sql.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antlr.sql.ddl.ColumnPosition;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * MySQL alter table statement extractor.
 * 
 * @author duhongjun
 */
public final class MySQLAlterTableExtractor extends AlterTableExtractor {
    
    public MySQLAlterTableExtractor() {
        addExtractor(new MySQLAddColumnExtractHandler());
        addExtractor(new MySQLAddIndexExtractHandler());
        addExtractor(new MySQLDropIndexExtractHandler());
        addExtractor(new RenameIndexExtractor());
        addExtractor(new AddPrimaryKeyExtractor(RuleName.ADD_CONSTRAINT));
        addExtractor(new DropPrimaryKeyExtractor());
        addExtractor(new MySQLChangeColumnExtractHandler());
        addExtractor(new MySQLModifyColumnExtractHandler());
    }
    
    @Override
    protected void adjustColumnDefinition(final AlterTableStatement alterTableStatement, final List<ColumnMetaData> newColumnMetaData) {
        if (alterTableStatement.getPositionChangedColumns().isEmpty()) {
            return;
        }
        if (alterTableStatement.getPositionChangedColumns().size() > 1) {
            Collections.sort(alterTableStatement.getPositionChangedColumns());
        }
        for (ColumnPosition each : alterTableStatement.getPositionChangedColumns()) {
            if (null != each.getFirstColumn()) {
                adjustFirst(newColumnMetaData, each.getFirstColumn());
            } else {
                adjustAfter(newColumnMetaData, each);
            }
        }
    }
    
    private void adjustFirst(final List<ColumnMetaData> newColumnMetaData, final String columnName) {
        ColumnMetaData firstMetaData = null;
        Iterator<ColumnMetaData> iterator = newColumnMetaData.iterator();
        while (iterator.hasNext()) {
            ColumnMetaData each = iterator.next();
            if (each.getColumnName().equals(columnName)) {
                firstMetaData = each;
                iterator.remove();
                break;
            }
        }
        if (null != firstMetaData) {
            newColumnMetaData.add(0, firstMetaData);
        }
    }
    
    private void adjustAfter(final List<ColumnMetaData> newColumnMetaData, final ColumnPosition columnPosition) {
        int afterIndex = -1;
        int adjustColumnIndex = -1;
        for (int i = 0; i < newColumnMetaData.size(); i++) {
            if (newColumnMetaData.get(i).getColumnName().equals(columnPosition.getColumnName())) {
                adjustColumnIndex = i;
            }
            if (newColumnMetaData.get(i).getColumnName().equals(columnPosition.getAfterColumn())) {
                afterIndex = i;
            }
            if (adjustColumnIndex >= 0 && afterIndex >= 0) {
                break;
            }
        }
        if (adjustColumnIndex >= 0 && afterIndex >= 0 && adjustColumnIndex != afterIndex + 1) {
            ColumnMetaData adjustColumnMetaData = newColumnMetaData.remove(adjustColumnIndex);
            if (afterIndex < adjustColumnIndex) {
                afterIndex = afterIndex + 1;
            }
            newColumnMetaData.add(afterIndex, adjustColumnMetaData);
        }
    }
}
