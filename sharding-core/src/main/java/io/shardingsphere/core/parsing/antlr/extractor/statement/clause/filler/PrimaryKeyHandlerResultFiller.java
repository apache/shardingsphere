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

package io.shardingsphere.core.parsing.antlr.extractor.statement.clause.filler;

import com.google.common.base.Optional;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.antlr.extractor.statement.clause.result.PrimaryKeyExtractResult;
import io.shardingsphere.core.parsing.antlr.sql.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antlr.sql.ddl.ColumnDefinition;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.ddl.create.table.CreateTableStatement;

/**
 * Primary keyHandler result filler.
 *
 * @author duhongjun
 */
public final class PrimaryKeyHandlerResultFiller extends AbstractHandlerResultFiller {
    
    public PrimaryKeyHandlerResultFiller() {
        super(PrimaryKeyExtractResult.class);
    }
    
    @Override
    protected void fillSQLStatement(final Object extractResult, final SQLStatement statement, final ShardingTableMetaData shardingTableMetaData) {
        if (statement instanceof AlterTableStatement) {
            fillAlter((PrimaryKeyExtractResult) extractResult, (AlterTableStatement) statement, shardingTableMetaData);
        } else if (statement instanceof CreateTableStatement) {
            fillCreate((PrimaryKeyExtractResult) extractResult, (CreateTableStatement) statement);
        }
    }
    
    private void fillAlter(final PrimaryKeyExtractResult primaryKeyExtractResult, final AlterTableStatement statement, final ShardingTableMetaData shardingTableMetaData) {
        for (String each : primaryKeyExtractResult.getPrimaryKeyColumnNames()) {
            Optional<ColumnDefinition> updateColumn = statement.findColumnDefinition(each, shardingTableMetaData);
            if (updateColumn.isPresent()) {
                updateColumn.get().setPrimaryKey(true);
                statement.getUpdateColumns().put(each, updateColumn.get());
            }
        }
    }
    
    private void fillCreate(final PrimaryKeyExtractResult primaryKeyExtractResult, final CreateTableStatement statement) {
        for (String each : primaryKeyExtractResult.getPrimaryKeyColumnNames()) {
            statement.getPrimaryKeyColumns().add(each);
        }
    }
}
