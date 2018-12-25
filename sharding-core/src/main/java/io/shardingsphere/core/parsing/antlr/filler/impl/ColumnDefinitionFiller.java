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

package io.shardingsphere.core.parsing.antlr.filler.impl;

import com.google.common.base.Optional;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.antlr.filler.SQLStatementFiller;
import io.shardingsphere.core.parsing.antlr.sql.segment.column.ColumnDefinitionSegment;
import io.shardingsphere.core.parsing.antlr.sql.statement.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antlr.sql.statement.ddl.ColumnDefinition;
import io.shardingsphere.core.parsing.antlr.sql.statement.ddl.CreateTableStatement;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.core.util.SQLUtil;

/**
 * Column definition filler.
 *
 * @author duhongjun
 */
public final class ColumnDefinitionFiller implements SQLStatementFiller<ColumnDefinitionSegment> {
    
    @Override
    public void fill(final ColumnDefinitionSegment sqlSegment, final SQLStatement sqlStatement, final String sql, final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData) {
        if (sqlStatement instanceof CreateTableStatement) {
            fill(sqlSegment, (CreateTableStatement) sqlStatement);
        } else if (sqlStatement instanceof AlterTableStatement) {
            fill(sqlSegment, (AlterTableStatement) sqlStatement, shardingTableMetaData);
        } 
    }
    
    private void fill(final ColumnDefinitionSegment sqlSegment, final CreateTableStatement createTableStatement) {
        createTableStatement.getColumnDefinitions().add(new ColumnDefinition(SQLUtil.getExactlyValue(sqlSegment.getName()), sqlSegment.getType(), sqlSegment.isPrimaryKey()));
    }
    
    private void fill(final ColumnDefinitionSegment sqlSegment, final AlterTableStatement alterTableStatement, final ShardingTableMetaData shardingTableMetaData) {
        String oldName = sqlSegment.getOldName();
        if (null != oldName) {
            Optional<ColumnDefinition> oldDefinition = alterTableStatement.findColumnDefinition(oldName, shardingTableMetaData);
            if (!oldDefinition.isPresent()) {
                return;
            }
            oldDefinition.get().setName(sqlSegment.getName());
            if (null != sqlSegment.getType()) {
                oldDefinition.get().setType(sqlSegment.getType());
            }
            alterTableStatement.getUpdateColumns().put(oldName, oldDefinition.get());
        } else {
            ColumnDefinition columnDefinition = new ColumnDefinition(sqlSegment.getName(), sqlSegment.getType(), sqlSegment.isPrimaryKey());
            if (!sqlSegment.isAdd()) {
                alterTableStatement.getUpdateColumns().put(sqlSegment.getName(), columnDefinition);
            } else if (!alterTableStatement.findColumnDefinitionFromMetaData(sqlSegment.getName(), shardingTableMetaData).isPresent()) {
                alterTableStatement.getAddColumns().add(columnDefinition);
            }
        }
        if (null != sqlSegment.getPosition()) {
            alterTableStatement.getPositionChangedColumns().add(sqlSegment.getPosition());
        }
    }
}
