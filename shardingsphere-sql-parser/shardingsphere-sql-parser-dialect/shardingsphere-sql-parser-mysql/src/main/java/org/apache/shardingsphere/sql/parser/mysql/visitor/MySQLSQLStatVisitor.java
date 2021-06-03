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

package org.apache.shardingsphere.sql.parser.mysql.visitor;

import lombok.Getter;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementBaseVisitor;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AliasContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ColumnDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ColumnRefContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.IdentifierContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.InsertContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.OwnerContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableFactorContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableWildContext;
import org.apache.shardingsphere.sql.parser.sql.common.SQLStats;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

/**
 * SQL Stats visitor for MySQL.
 */
@Getter
public final class MySQLSQLStatVisitor extends MySQLStatementBaseVisitor<SQLStats> {
    
    private final SQLStats sqlStats = new SQLStats();
    
    @Override
    public SQLStats visitTableFactor(final TableFactorContext ctx) {
        if (null != ctx.tableName()) {
            SimpleTableSegment tableSegment = getTableName(ctx.tableName());
            if (null != ctx.alias()) {
                AliasContext aliasContext = ctx.alias();
                tableSegment.setAlias(new AliasSegment(aliasContext.start.getStartIndex(), aliasContext.stop.getStopIndex(), new IdentifierValue(aliasContext.textOrIdentifier().getText())));
            }
            sqlStats.addTable(tableSegment);
            return sqlStats;
        }
        super.visitTableFactor(ctx);
        return sqlStats;
    }
    
    @Override
    public SQLStats visitInsert(final InsertContext ctx) {
        SimpleTableSegment tableSegment = getTableName(ctx.tableName());
        sqlStats.addTable(tableSegment);
        if (null != ctx.insertValuesClause()) {
            visit(ctx.insertValuesClause());
        } else if (null != ctx.insertSelectClause()) {
            visit(ctx.insertSelectClause());
        } else {
            visit(ctx.setAssignmentsClause());
        }
        return sqlStats;
    }
    
    @Override
    public SQLStats visitColumnRef(final ColumnRefContext ctx) {
        sqlStats.addColumn(getColumn(ctx));
        return sqlStats;
    }
    
    @Override
    public SQLStats visitColumnDefinition(final ColumnDefinitionContext ctx) {
        sqlStats.addColumn(new ColumnSegment(ctx.column_name.start.getStartIndex(), ctx.column_name.stop.getStopIndex(), new IdentifierValue(ctx.column_name.getText())));
        return sqlStats;
    }
    
    private ColumnSegment getColumn(final ColumnRefContext ctx) {
        IdentifierValue name;
        OwnerSegment owner = null;
        if (2 == ctx.identifier().size()) {
            name = new IdentifierValue(ctx.identifier(1).getText());
            owner = new OwnerSegment(ctx.identifier(0).start.getStartIndex(), ctx.identifier(0).stop.getStopIndex(), new IdentifierValue(ctx.identifier(0).getText()));
        } else if (3 == ctx.identifier().size()) {
            name = new IdentifierValue(ctx.identifier(2).getText());
            owner = new OwnerSegment(ctx.identifier(1).start.getStartIndex(), ctx.identifier(1).stop.getStopIndex(), new IdentifierValue(ctx.identifier(1).getText()));
        } else {
            name = new IdentifierValue(ctx.identifier(0).getText());
        }
        ColumnSegment result = new ColumnSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), name);
        result.setOwner(owner);
        return result;
    }
    
    private SimpleTableSegment getTableName(final TableNameContext ctx) {
        SimpleTableSegment result = new SimpleTableSegment(new TableNameSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), new IdentifierValue(ctx.name().identifier().getText())));
        OwnerContext owner = ctx.owner();
        if (null != owner) {
            result.setOwner(new OwnerSegment(owner.getStart().getStartIndex(), owner.getStop().getStopIndex(), new IdentifierValue(owner.identifier().getText())));
        }
        return result;
    }
    
    @Override
    public SQLStats visitTableName(final TableNameContext ctx) {
        SimpleTableSegment tableSegment = getTableName(ctx);
        sqlStats.addTable(tableSegment);
        return sqlStats;
    }
    
    @Override
    public SQLStats visitTableWild(final TableWildContext ctx) {
        ColumnSegment column = new ColumnSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), new IdentifierValue("*"));
        IdentifierContext owner = ctx.identifier().get(ctx.identifier().size() - 1);
        column.setOwner(new OwnerSegment(owner.start.getStartIndex(), owner.stop.getStopIndex(), new IdentifierValue(owner.getText())));
        sqlStats.addColumn(column);
        return sqlStats;
    }
    
    @Override
    public SQLStats visitTerminal(final TerminalNode node) {
        super.visitTerminal(node);
        return sqlStats;
    }
}
