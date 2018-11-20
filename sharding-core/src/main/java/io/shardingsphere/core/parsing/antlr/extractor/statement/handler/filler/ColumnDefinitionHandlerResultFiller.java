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

package io.shardingsphere.core.parsing.antlr.extractor.statement.handler.filler;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.result.ColumnDefinitionExtractResult;
import io.shardingsphere.core.parsing.antlr.sql.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antlr.sql.ddl.ColumnDefinition;
import io.shardingsphere.core.parsing.antlr.sql.ddl.mysql.MySQLAlterTableStatement;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.ddl.create.table.CreateTableStatement;
import io.shardingsphere.core.util.SQLUtil;

/**
 * Column definition handler result filler.
 *
 * @author duhongjun
 */
public final class ColumnDefinitionHandlerResultFiller extends AbstractHandlerResultFiller {
    
    public ColumnDefinitionHandlerResultFiller() {
        super(ColumnDefinitionExtractResult.class);
    }
    
    @Override
    protected void fillSQLStatement(final Object extractResult, final SQLStatement statement) {
        ColumnDefinitionExtractResult columnExtractResult = (ColumnDefinitionExtractResult) extractResult;
        if (statement instanceof AlterTableStatement) {
            fillAlter(columnExtractResult, (AlterTableStatement) statement);
        } else if (statement instanceof CreateTableStatement) {
            fillCreate(columnExtractResult, (CreateTableStatement) statement);
        }
    }
    
    private void fillAlter(final ColumnDefinitionExtractResult columnExtractResult, final AlterTableStatement alterTableStatement) {
        String oldName = columnExtractResult.getOldName();
        if (null != oldName) {
            Optional<ColumnDefinition> oldDefinition = alterTableStatement.getColumnDefinitionByName(oldName);
            if (!oldDefinition.isPresent()) {
                return;
            }
            oldDefinition.get().setName(columnExtractResult.getName());
            if (null != columnExtractResult.getType()) {
                oldDefinition.get().setType(columnExtractResult.getType());
                oldDefinition.get().setLength(columnExtractResult.getLength());
            }
            alterTableStatement.getUpdateColumns().put(oldName, oldDefinition.get());
        } else {
            ColumnDefinition columnDefinition = new ColumnDefinition(columnExtractResult.getName(), columnExtractResult.getType(), columnExtractResult.getLength(), columnExtractResult.isPrimaryKey());
            if (!columnExtractResult.isAdd()) {
                alterTableStatement.getUpdateColumns().put(columnExtractResult.getName(), columnDefinition);
            } else if (!alterTableStatement.findColumnDefinition(columnExtractResult.getName()).isPresent()) {
                alterTableStatement.getAddColumns().add(columnDefinition);
            }
        }
        if (null != columnExtractResult.getPosition()) {
            MySQLAlterTableStatement mysqlAlterTable = (MySQLAlterTableStatement) alterTableStatement;
            mysqlAlterTable.getPositionChangedColumns().add(columnExtractResult.getPosition());
        }
    }
    
    private void fillCreate(final ColumnDefinitionExtractResult columnDefinition, final CreateTableStatement createTableStatement) {
        createTableStatement.getColumnNames().add(SQLUtil.getExactlyValue(columnDefinition.getName()));
        createTableStatement.getColumnTypes().add(columnDefinition.getType());
        if (columnDefinition.isPrimaryKey()) {
            createTableStatement.getPrimaryKeyColumns().add(columnDefinition.getName());
        }
    }
}
