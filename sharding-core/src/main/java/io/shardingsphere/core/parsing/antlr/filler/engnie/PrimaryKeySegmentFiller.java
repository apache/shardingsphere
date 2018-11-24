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
import io.shardingsphere.core.parsing.antlr.filler.AbstractSQLSegmentFiller;
import io.shardingsphere.core.parsing.antlr.sql.segment.AddPrimaryKeySegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;
import io.shardingsphere.core.parsing.antlr.sql.statement.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antlr.sql.statement.ddl.ColumnDefinition;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.ddl.create.table.CreateTableStatement;

/**
 * Primary keyHandler result filler.
 *
 * @author duhongjun
 */
public final class PrimaryKeySegmentFiller extends AbstractSQLSegmentFiller {
    
    public PrimaryKeySegmentFiller() {
        super(AddPrimaryKeySegment.class);
    }
    
    @Override
    protected void doFill(final SQLSegment sqlSegment, final SQLStatement sqlStatement, final ShardingTableMetaData shardingTableMetaData) {
        if (sqlStatement instanceof AlterTableStatement) {
            fillAlter((AddPrimaryKeySegment) sqlSegment, (AlterTableStatement) sqlStatement, shardingTableMetaData);
        } else if (sqlStatement instanceof CreateTableStatement) {
            fillCreate((AddPrimaryKeySegment) sqlSegment, (CreateTableStatement) sqlStatement);
        }
    }
    
    private void fillAlter(final AddPrimaryKeySegment addPrimaryKeySegment, final AlterTableStatement alterTableStatement, final ShardingTableMetaData shardingTableMetaData) {
        for (String each : addPrimaryKeySegment.getPrimaryKeyColumnNames()) {
            Optional<ColumnDefinition> updateColumn = alterTableStatement.findColumnDefinition(each, shardingTableMetaData);
            if (updateColumn.isPresent()) {
                updateColumn.get().setPrimaryKey(true);
                alterTableStatement.getUpdateColumns().put(each, updateColumn.get());
            }
        }
    }
    
    private void fillCreate(final AddPrimaryKeySegment addPrimaryKeySegment, final CreateTableStatement createTableStatement) {
        for (String each : addPrimaryKeySegment.getPrimaryKeyColumnNames()) {
            createTableStatement.getPrimaryKeyColumns().add(each);
        }
    }
}
