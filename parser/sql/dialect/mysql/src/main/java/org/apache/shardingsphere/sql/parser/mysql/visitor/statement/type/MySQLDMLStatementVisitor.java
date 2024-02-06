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

package org.apache.shardingsphere.sql.parser.mysql.visitor.statement.type;

import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DMLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CallContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DoStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ExprContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.HandlerStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ImportStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.IndexHintContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.LoadDataStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.LoadStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.LoadXmlStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.WindowClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.WindowItemContext;
import org.apache.shardingsphere.sql.parser.mysql.visitor.statement.MySQLStatementVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.WindowItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.WindowSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.IndexHintSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLCallStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLDoStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLHandlerStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLImportStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLLoadDataStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLLoadXMLStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * DML statement visitor for MySQL.
 */
public final class MySQLDMLStatementVisitor extends MySQLStatementVisitor implements DMLStatementVisitor {
    
    @Override
    public ASTNode visitCall(final CallContext ctx) {
        List<ExpressionSegment> params = new ArrayList<>();
        ctx.expr().forEach(each -> params.add((ExpressionSegment) visit(each)));
        return new MySQLCallStatement(ctx.identifier().getText(), params);
    }
    
    @Override
    public ASTNode visitDoStatement(final DoStatementContext ctx) {
        List<ExpressionSegment> params = new ArrayList<>();
        ctx.expr().forEach(each -> params.add((ExpressionSegment) visit(each)));
        return new MySQLDoStatement(params);
    }
    
    @Override
    public ASTNode visitHandlerStatement(final HandlerStatementContext ctx) {
        return new MySQLHandlerStatement();
    }
    
    @Override
    public ASTNode visitImportStatement(final ImportStatementContext ctx) {
        return new MySQLImportStatement();
    }
    
    @Override
    public ASTNode visitLoadStatement(final LoadStatementContext ctx) {
        return null == ctx.loadDataStatement() ? visit(ctx.loadXmlStatement()) : visit(ctx.loadDataStatement());
    }
    
    @Override
    public ASTNode visitLoadDataStatement(final LoadDataStatementContext ctx) {
        return new MySQLLoadDataStatement((SimpleTableSegment) visit(ctx.tableName()));
    }
    
    @Override
    public ASTNode visitLoadXmlStatement(final LoadXmlStatementContext ctx) {
        return new MySQLLoadXMLStatement((SimpleTableSegment) visit(ctx.tableName()));
    }
    
    @Override
    public ASTNode visitIndexHint(final IndexHintContext ctx) {
        Collection<String> indexNames = new LinkedList<>();
        ctx.indexName().forEach(each -> indexNames.add(each.getText()));
        String useType;
        if (null != ctx.USE()) {
            useType = ctx.USE().getText();
        } else if (null != ctx.IGNORE()) {
            useType = ctx.IGNORE().getText();
        } else {
            useType = ctx.FORCE().getText();
        }
        IndexHintSegment result = new IndexHintSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), indexNames, useType,
                null == ctx.INDEX() ? ctx.KEY().getText() : ctx.INDEX().getText(), getOriginalText(ctx));
        if (null != ctx.FOR()) {
            String hintScope;
            if (null != ctx.JOIN()) {
                hintScope = "JOIN";
            } else if (null != ctx.ORDER()) {
                hintScope = "ORDER BY";
            } else {
                hintScope = "GROUP BY";
            }
            result.setHintScope(hintScope);
        }
        return result;
    }
    
    @Override
    public ASTNode visitWindowClause(final WindowClauseContext ctx) {
        WindowSegment result = new WindowSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        for (WindowItemContext each : ctx.windowItem()) {
            result.getItemSegments().add((WindowItemSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitWindowItem(final WindowItemContext ctx) {
        WindowItemSegment result = new WindowItemSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        result.setWindowName(new IdentifierValue(ctx.identifier().getText()));
        if (null != ctx.windowSpecification().PARTITION()) {
            result.setPartitionListSegments(getExpressions(ctx.windowSpecification().expr()));
        }
        if (null != ctx.windowSpecification().orderByClause()) {
            result.setOrderBySegment((OrderBySegment) visit(ctx.windowSpecification().orderByClause()));
        }
        if (null != ctx.windowSpecification().frameClause()) {
            result.setFrameClause(new CommonExpressionSegment(ctx.windowSpecification().frameClause().start.getStartIndex(), ctx.windowSpecification().frameClause().stop.getStopIndex(),
                    ctx.windowSpecification().frameClause().getText()));
        }
        return result;
    }
    
    private Collection<ExpressionSegment> getExpressions(final List<ExprContext> exprList) {
        if (null == exprList) {
            return Collections.emptyList();
        }
        Collection<ExpressionSegment> result = new ArrayList<>(exprList.size());
        for (ExprContext each : exprList) {
            result.add((ExpressionSegment) visit(each));
        }
        return result;
    }
}
