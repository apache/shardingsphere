/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.parsing.antlr.filler.common.segment.impl.ddl;

import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parsing.antlr.filler.common.SQLSegmentCommonFiller;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.definition.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.definition.constraint.ConstraintDefinitionSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.core.parsing.antlr.sql.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLStatement;

import com.google.common.base.Optional;

/**
 * Constraint definition filler.
 *
 * @author duhongjun
 */
public final class ConstraintDefinitionFiller implements SQLSegmentCommonFiller<ConstraintDefinitionSegment> {
    
    @Override
    public void fill(final ConstraintDefinitionSegment sqlSegment, final SQLStatement sqlStatement, final String sql, final ShardingTableMetaData shardingTableMetaData) {
        if (sqlStatement instanceof CreateTableStatement) {
            fill(sqlSegment, (CreateTableStatement) sqlStatement);
        } else if (sqlStatement instanceof AlterTableStatement) {
            fill(sqlSegment, (AlterTableStatement) sqlStatement, shardingTableMetaData);
        }
    }
    
    private void fill(final ConstraintDefinitionSegment sqlSegment, final CreateTableStatement createTableStatement) {
        for (ColumnDefinitionSegment each : createTableStatement.getColumnDefinitions()) {
            if (sqlSegment.getPrimaryKeyColumnNames().contains(each.getColumnName())) {
                each.setPrimaryKey(true);
            }
        }
    }
    
    private void fill(final ConstraintDefinitionSegment sqlSegment, final AlterTableStatement alterTableStatement, final ShardingTableMetaData shardingTableMetaData) {
        for (String each : sqlSegment.getPrimaryKeyColumnNames()) {
            Optional<ColumnDefinitionSegment> modifiedColumn = alterTableStatement.findColumnDefinition(each, shardingTableMetaData);
            if (modifiedColumn.isPresent()) {
                modifiedColumn.get().setPrimaryKey(true);
                alterTableStatement.getModifiedColumnDefinitions().put(each, modifiedColumn.get());
            }
        }
    }
}
