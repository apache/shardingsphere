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

package org.apache.shardingsphere.sql.parser.binder.statement.ddl;

import lombok.Getter;
import org.apache.shardingsphere.sql.parser.binder.segment.table.TablesContext;
import org.apache.shardingsphere.sql.parser.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.AddColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.column.alter.ModifyColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.ConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.binder.type.TableAvailable;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Alter table statement context.
 */
@Getter
public final class AlterTableStatementContext extends CommonSQLStatementContext<AlterTableStatement> implements TableAvailable {
    
    private final TablesContext tablesContext;
    
    public AlterTableStatementContext(final AlterTableStatement sqlStatement) {
        super(sqlStatement);
        tablesContext = new TablesContext(sqlStatement.getTable());
    }
    
    @Override
    public Collection<SimpleTableSegment> getAllTables() {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        result.add(getSqlStatement().getTable());
        for (AddColumnDefinitionSegment each : getSqlStatement().getAddColumnDefinitions()) {
            for (ColumnDefinitionSegment columnDefinition : each.getColumnDefinitions()) {
                result.addAll(columnDefinition.getReferencedTables());
            }
        }
        for (ModifyColumnDefinitionSegment each : getSqlStatement().getModifyColumnDefinitions()) {
            result.addAll(each.getColumnDefinition().getReferencedTables());
        }
        for (ConstraintDefinitionSegment each : getSqlStatement().getAddConstraintDefinitions()) {
            if (each.getReferencedTable().isPresent()) {
                result.add(each.getReferencedTable().get());
            }
        }
        return result;
    }
}
