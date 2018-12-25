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
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.ddl.create.table.CreateTableStatement;
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
        if (sqlStatement instanceof AlterTableStatement) {
            fillAlter(sqlSegment, (AlterTableStatement) sqlStatement, shardingTableMetaData);
        } else if (sqlStatement instanceof CreateTableStatement) {
            fillCreate(sqlSegment, (CreateTableStatement) sqlStatement);
        }
    }
    
    private void fillAlter(final ColumnDefinitionSegment columnDefinitionSegment, final AlterTableStatement alterTableStatement, final ShardingTableMetaData shardingTableMetaData) {
        String oldName = columnDefinitionSegment.getOldName();
        if (null != oldName) {
            Optional<ColumnDefinition> oldDefinition = alterTableStatement.findColumnDefinition(oldName, shardingTableMetaData);
            if (!oldDefinition.isPresent()) {
                return;
            }
            oldDefinition.get().setName(columnDefinitionSegment.getName());
            if (null != columnDefinitionSegment.getType()) {
                oldDefinition.get().setType(columnDefinitionSegment.getType());
            }
            alterTableStatement.getUpdateColumns().put(oldName, oldDefinition.get());
        } else {
            ColumnDefinition columnDefinition = new ColumnDefinition(columnDefinitionSegment.getName(), columnDefinitionSegment.getType(), columnDefinitionSegment.isPrimaryKey());
            if (!columnDefinitionSegment.isAdd()) {
                alterTableStatement.getUpdateColumns().put(columnDefinitionSegment.getName(), columnDefinition);
            } else if (!alterTableStatement.findColumnDefinitionFromMetaData(columnDefinitionSegment.getName(), shardingTableMetaData).isPresent()) {
                alterTableStatement.getAddColumns().add(columnDefinition);
            }
        }
        if (null != columnDefinitionSegment.getPosition()) {
            alterTableStatement.getPositionChangedColumns().add(columnDefinitionSegment.getPosition());
        }
    }
    
    private void fillCreate(final ColumnDefinitionSegment columnDefinitionSegment, final CreateTableStatement createTableStatement) {
        createTableStatement.getColumnNames().add(SQLUtil.getExactlyValue(columnDefinitionSegment.getName()));
        createTableStatement.getColumnTypes().add(columnDefinitionSegment.getType());
        if (columnDefinitionSegment.isPrimaryKey()) {
            createTableStatement.getPrimaryKeyColumns().add(columnDefinitionSegment.getName());
        }
    }
}
