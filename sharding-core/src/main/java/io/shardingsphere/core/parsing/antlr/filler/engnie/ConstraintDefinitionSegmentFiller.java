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

package io.shardingsphere.core.parsing.antlr.filler.engnie;

import com.google.common.base.Optional;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.antlr.filler.SQLSegmentFiller;
import io.shardingsphere.core.parsing.antlr.sql.segment.ConstraintDefinitionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;
import io.shardingsphere.core.parsing.antlr.sql.statement.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antlr.sql.statement.ddl.ColumnDefinition;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.ddl.create.table.CreateTableStatement;

/**
 * Constraint definition segment filler.
 *
 * @author duhongjun
 */
public final class ConstraintDefinitionSegmentFiller implements SQLSegmentFiller {
    
    @Override
    public void fill(final SQLSegment sqlSegment, final SQLStatement sqlStatement, final ShardingTableMetaData shardingTableMetaData) {
        if (sqlStatement instanceof AlterTableStatement) {
            fillAlter((ConstraintDefinitionSegment) sqlSegment, (AlterTableStatement) sqlStatement, shardingTableMetaData);
        } else if (sqlStatement instanceof CreateTableStatement) {
            fillCreate((ConstraintDefinitionSegment) sqlSegment, (CreateTableStatement) sqlStatement);
        }
    }
    
    private void fillAlter(final ConstraintDefinitionSegment constraintDefinitionSegment, final AlterTableStatement alterTableStatement, final ShardingTableMetaData shardingTableMetaData) {
        for (String each : constraintDefinitionSegment.getPrimaryKeyColumnNames()) {
            Optional<ColumnDefinition> updateColumn = alterTableStatement.findColumnDefinition(each, shardingTableMetaData);
            if (updateColumn.isPresent()) {
                updateColumn.get().setPrimaryKey(true);
                alterTableStatement.getUpdateColumns().put(each, updateColumn.get());
            }
        }
    }
    
    private void fillCreate(final ConstraintDefinitionSegment constraintDefinitionSegment, final CreateTableStatement createTableStatement) {
        for (String each : constraintDefinitionSegment.getPrimaryKeyColumnNames()) {
            createTableStatement.getPrimaryKeyColumns().add(each);
        }
    }
}
