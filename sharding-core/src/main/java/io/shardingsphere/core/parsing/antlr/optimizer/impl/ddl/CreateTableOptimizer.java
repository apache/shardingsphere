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

package io.shardingsphere.core.parsing.antlr.optimizer.impl.ddl;

import io.shardingsphere.core.metadata.table.ColumnMetaData;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.metadata.table.TableMetaData;
import io.shardingsphere.core.parsing.antlr.optimizer.SQLStatementOptimizer;
import io.shardingsphere.core.parsing.antlr.sql.statement.ddl.ColumnDefinition;
import io.shardingsphere.core.parsing.antlr.sql.statement.ddl.CreateTableStatement;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Create table optimizer.
 * 
 * @author duhongjun
 */
public final class CreateTableOptimizer implements SQLStatementOptimizer {
    
    @Override
    public void optimize(final SQLStatement sqlStatement, final ShardingTableMetaData shardingTableMetaData) {
        CreateTableStatement createStatement = (CreateTableStatement) sqlStatement;
        Collection<ColumnMetaData> newColumnMetaDataList = new LinkedList<>();
        for (ColumnDefinition each : createStatement.getColumnDefinitions()) {
            newColumnMetaDataList.add(new ColumnMetaData(each.getName(), each.getType(), each.isPrimaryKey()));
        }
        createStatement.setTableMetaData(new TableMetaData(newColumnMetaDataList));
    }
}
