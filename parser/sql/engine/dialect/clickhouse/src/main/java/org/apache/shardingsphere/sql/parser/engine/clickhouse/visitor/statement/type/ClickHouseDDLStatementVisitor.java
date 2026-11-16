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

package org.apache.shardingsphere.sql.parser.engine.clickhouse.visitor.statement.type;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DDLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.ClickHouseStatementParser;
import org.apache.shardingsphere.sql.parser.engine.clickhouse.visitor.statement.ClickHouseStatementVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.engine.EngineSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.CreateTableOptionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.DropTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * DDL statement visitor for ClickHouse.
 */
@HighFrequencyInvocation
public final class ClickHouseDDLStatementVisitor extends ClickHouseStatementVisitor implements DDLStatementVisitor {
    
    public ClickHouseDDLStatementVisitor(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    @Override
    public ASTNode visitCreateTable(final ClickHouseStatementParser.CreateTableContext ctx) {
        Collection<ColumnDefinitionSegment> columns = new ArrayList<>(ctx.columnDefinition().size());
        for (ClickHouseStatementParser.ColumnDefinitionContext each : ctx.columnDefinition()) {
            columns.add((ColumnDefinitionSegment) visit(each));
        }
        return CreateTableStatement.builder().databaseType(getDatabaseType()).table((SimpleTableSegment) visit(ctx.tableName()))
                .ifNotExists(null != ctx.ifNotExists()).temporary(null != ctx.TEMPORARY()).columnDefinitions(columns)
                .createTableOption(null == ctx.tableEngineClause() ? null : (CreateTableOptionSegment) visit(ctx.tableEngineClause())).build();
    }
    
    @Override
    public ASTNode visitColumnDefinition(final ClickHouseStatementParser.ColumnDefinitionContext ctx) {
        boolean notNull = null != ctx.NOT() || null == ctx.NULL() && !"Nullable".equalsIgnoreCase(ctx.dataType().dataTypeName().getText());
        return new ColumnDefinitionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), (ColumnSegment) visit(ctx.columnName()),
                (DataTypeSegment) visit(ctx.dataType()), false, notNull, getOriginalText(ctx));
    }
    
    @Override
    public ASTNode visitTableEngineClause(final ClickHouseStatementParser.TableEngineClauseContext ctx) {
        CreateTableOptionSegment result = new CreateTableOptionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        ClickHouseStatementParser.TableEngineContext engine = ctx.tableEngine();
        result.setEngine(new EngineSegment(engine.start.getStartIndex(), engine.stop.getStopIndex(), ((IdentifierValue) visit(engine.identifier())).getValue()));
        return result;
    }
    
    @Override
    public ASTNode visitDropTable(final ClickHouseStatementParser.DropTableContext ctx) {
        return new DropTableStatement(getDatabaseType(), Collections.singletonList((SimpleTableSegment) visit(ctx.tableName())), null != ctx.ifExists(), null != ctx.TEMPORARY(), false);
    }
}
