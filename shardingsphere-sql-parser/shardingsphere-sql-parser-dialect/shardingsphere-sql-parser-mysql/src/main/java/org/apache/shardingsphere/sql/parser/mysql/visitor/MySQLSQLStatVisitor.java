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
import lombok.Setter;
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
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MySQL SQL Stats visitor for MySQL.
 */
@Getter
@Setter
public final class MySQLSQLStatVisitor extends MySQLStatementBaseVisitor<Boolean> {

    private Map<String, SimpleTableSegment> tables = new LinkedHashMap<>();

    private Map<Integer, ColumnSegment> columns = new LinkedHashMap<>();

    @Override
    public Boolean visitTableFactor(final TableFactorContext ctx) {
        if (null != ctx.tableName()) {
            SimpleTableSegment tableSegment = getTableName(ctx.tableName());
            if (null != ctx.alias()) {
                tableSegment.setAlias(getAlias(ctx.alias()));
            }
            addTable(tableSegment);
            return true;
        }
        return super.visitTableFactor(ctx);
    }

    @Override
    public Boolean visitInsert(final InsertContext ctx) {
        SimpleTableSegment tableSegment = getTableName(ctx.tableName());
        addTable(tableSegment);
        if (null != ctx.insertValuesClause()) {
            visit(ctx.insertValuesClause());
        } else if (null != ctx.insertSelectClause()) {
            visit(ctx.insertSelectClause());
        } else {
            visit(ctx.setAssignmentsClause());
        }
        return true;
    }

    @Override
    public Boolean visitColumnRef(final ColumnRefContext ctx) {
        ColumnSegment column = getColumn(ctx);
        addColumn(column);
        return true;
    }

    @Override
    public Boolean visitColumnDefinition(final ColumnDefinitionContext ctx) {
        ColumnSegment column = getColumn(ctx.column_name);
        addColumn(column);
        return true;
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
        ColumnSegment column = new ColumnSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), name);
        column.setOwner(owner);
        return column;
    }

    private ColumnSegment getColumn(final IdentifierContext ctx) {
        ColumnSegment column = new ColumnSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), new IdentifierValue(ctx.getText()));
        return column;
    }

    private AliasSegment getAlias(final AliasContext ctx) {
        return new AliasSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), new IdentifierValue(ctx.textOrIdentifier().getText()));
    }

    private SimpleTableSegment getTableName(final TableNameContext ctx) {
        SimpleTableSegment result = new SimpleTableSegment(new TableNameSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), getTableFromIden(ctx.name().identifier())));
        OwnerContext owner = ctx.owner();
        if (null != owner) {
            result.setOwner(new OwnerSegment(owner.getStart().getStartIndex(), owner.getStop().getStopIndex(), new IdentifierValue(owner.identifier().getText())));
        }
        return result;
    }

    @Override
    public Boolean visitTableName(final TableNameContext ctx) {
        SimpleTableSegment tableSegment = getTableName(ctx);
        addTable(tableSegment);
        return true;
    }

    @Override
    public Boolean visitTableWild(final TableWildContext ctx) {
        ColumnSegment column = new ColumnSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), new IdentifierValue("*"));
        IdentifierContext owner = ctx.identifier().get(ctx.identifier().size() - 1);
        column.setOwner(new OwnerSegment(owner.start.getStartIndex(), owner.stop.getStopIndex(), new IdentifierValue(owner.getText())));
        addColumn(column);
        return true;
    }

    private int hashCode(final ColumnSegment column) {
        StringBuilder columString = new StringBuilder();
        if (column.getOwner().isPresent()) {
            columString.append(column.getOwner().get().getIdentifier().getValue());
        }
        columString.append(column.getIdentifier().getValue());
        return columString.toString().hashCode();
    }

    private Boolean addTable(final SimpleTableSegment tableSegment) {
        if (!tables.containsKey(tableSegment.getTableName().getIdentifier().getValue())) {
            tables.put(tableSegment.getTableName().getIdentifier().getValue(), tableSegment);
        }
        return true;
    }

    private Boolean addColumn(final ColumnSegment column) {
        int columnHashcode = hashCode(column);
        if (!columns.containsKey(columnHashcode)) {
            columns.put(columnHashcode, column);
        }
        return true;
    }

    private IdentifierValue getTableFromIden(final IdentifierContext ctx) {
        return new IdentifierValue(ctx.getText());
    }
}
