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

package io.shardingsphere.core.parsing.antlr.extractor.statement.handler.result;

import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.base.Optional;

import io.shardingsphere.core.parsing.antlr.sql.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antlr.sql.ddl.ColumnDefinition;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.ddl.create.table.CreateTableStatement;
import lombok.Getter;
import lombok.Setter;

/**
 * Add primary key result.
 *
 * @author duhongjun
 */
@Getter
@Setter
public class PrimaryKeyExtractResult implements ExtractResult {
    
    Set<String> primaryKeyColumnNames = new LinkedHashSet<>();
    
    /**
     * Inject primary key to SQLStatement.
     * 
     * @param statement SQL statement
     */
    @Override
    public void fill(final SQLStatement statement) {
        if (statement instanceof AlterTableStatement) {
            injectAlter(statement);
        } else if (statement instanceof CreateTableStatement) {
            injectCreate(statement);
        }
    }
    
    private void injectAlter(final SQLStatement statement) {
        AlterTableStatement alterStatement = (AlterTableStatement) statement;
        for (String each : primaryKeyColumnNames) {
            Optional<ColumnDefinition> updateColumn = alterStatement.getColumnDefinitionByName(each);
            if (updateColumn.isPresent()) {
                updateColumn.get().setPrimaryKey(true);
                alterStatement.getUpdateColumns().put(each, updateColumn.get());
            }
        }
    }
    
    private void injectCreate(final SQLStatement statement) {
        CreateTableStatement createStatement = (CreateTableStatement) statement;
        for (String each : primaryKeyColumnNames) {
            createStatement.getPrimaryKeyColumns().add(each);
        }
    }
}
