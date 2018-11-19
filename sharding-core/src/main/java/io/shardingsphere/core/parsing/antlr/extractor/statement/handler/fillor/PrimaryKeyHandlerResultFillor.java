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

package io.shardingsphere.core.parsing.antlr.extractor.statement.handler.fillor;

import com.google.common.base.Optional;

import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.result.PrimaryKeyExtractResult;
import io.shardingsphere.core.parsing.antlr.sql.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antlr.sql.ddl.ColumnDefinition;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.ddl.create.table.CreateTableStatement;

/**
 * Primary keyHandler result fillor.
 * 
 * @author duhongjun
 */
public class PrimaryKeyHandlerResultFillor extends AbstractHandlerResultFillor {

    public PrimaryKeyHandlerResultFillor() {
        super(PrimaryKeyExtractResult.class);
    }

    @Override
    protected void fillSQLStatement(Object extractResult, SQLStatement statement) {
        if (statement instanceof AlterTableStatement) {
            fillAlter((PrimaryKeyExtractResult) extractResult, (AlterTableStatement) statement);
        } else if (statement instanceof CreateTableStatement) {
            fillCreate((PrimaryKeyExtractResult) extractResult, (CreateTableStatement) statement);
        }
    }

    private void fillAlter(final PrimaryKeyExtractResult result, final AlterTableStatement statement) {
        for (String each : result.getPrimaryKeyColumnNames()) {
            Optional<ColumnDefinition> updateColumn = statement.getColumnDefinitionByName(each);
            if (updateColumn.isPresent()) {
                updateColumn.get().setPrimaryKey(true);
                statement.getUpdateColumns().put(each, updateColumn.get());
            }
        }
    }

    private void fillCreate(final PrimaryKeyExtractResult result, final CreateTableStatement statement) {
        for (String each : result.getPrimaryKeyColumnNames()) {
            statement.getPrimaryKeyColumns().add(each);
        }
    }

}
