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

package org.apache.shardingsphere.sql.parser.engine.doris.visitor.statement.type;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DMLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.CallContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.DoStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.HandlerStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ImportStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.IndexHintContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.LoadDataStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.LoadStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.LoadXmlStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.WindowClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.WindowFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.WindowItemContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.WindowSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.WindowingClauseContext;
import org.apache.shardingsphere.sql.parser.engine.doris.visitor.statement.DorisStatementVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WindowItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WindowSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.IndexHintSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.CallStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DoStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.mysql.dml.MySQLHandlerStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dml.MySQLImportStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dml.MySQLLoadDataStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dml.MySQLLoadXMLStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DML statement visitor for Doris.
 */
public final class DorisDMLStatementVisitor extends DorisStatementVisitor implements DMLStatementVisitor {
    
    public DorisDMLStatementVisitor(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    @Override
    public ASTNode visitCall(final CallContext ctx) {
        String procedureName = null == ctx.owner() ? ctx.identifier().getText() : ctx.owner().getText() + "." + ctx.identifier().getText();
        List<ExpressionSegment> params = ctx.expr().stream().map(each -> (ExpressionSegment) visit(each)).collect(Collectors.toList());
        return new CallStatement(getDatabaseType(), procedureName, params);
    }
    
    @Override
    public ASTNode visitDoStatement(final DoStatementContext ctx) {
        return new DoStatement(getDatabaseType(), ctx.expr().stream().map(each -> (ExpressionSegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitHandlerStatement(final HandlerStatementContext ctx) {
        return new MySQLHandlerStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitImportStatement(final ImportStatementContext ctx) {
        return new MySQLImportStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitLoadStatement(final LoadStatementContext ctx) {
        return null == ctx.loadDataStatement() ? visit(ctx.loadXmlStatement()) : visit(ctx.loadDataStatement());
    }
    
    @Override
    public ASTNode visitLoadDataStatement(final LoadDataStatementContext ctx) {
        return new MySQLLoadDataStatement(getDatabaseType(), (SimpleTableSegment) visit(ctx.tableName()));
    }
    
    @Override
    public ASTNode visitLoadXmlStatement(final LoadXmlStatementContext ctx) {
        return new MySQLLoadXMLStatement(getDatabaseType(), (SimpleTableSegment) visit(ctx.tableName()));
    }
    
    @Override
    public ASTNode visitIndexHint(final IndexHintContext ctx) {
        Collection<String> indexNames = new LinkedList<>();
        if (null != ctx.indexNameList()) {
            ctx.indexNameList().indexName().forEach(each -> indexNames.add(each.getText()));
        }
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
        if (null != ctx.indexHintClause().FOR()) {
            String hintScope;
            if (null != ctx.indexHintClause().JOIN()) {
                hintScope = "JOIN";
            } else if (null != ctx.indexHintClause().ORDER()) {
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
        WindowItemSegment windowItemSegment = (WindowItemSegment) visit(ctx.windowSpecification());
        result.setPartitionListSegments(windowItemSegment.getPartitionListSegments());
        result.setOrderBySegment(windowItemSegment.getOrderBySegment());
        result.setFrameClause(windowItemSegment.getFrameClause());
        return result;
    }
    
    @Override
    public ASTNode visitWindowSpecification(final WindowSpecificationContext ctx) {
        WindowItemSegment result = new WindowItemSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        if (null != ctx.PARTITION()) {
            result.setPartitionListSegments(getExpressions(ctx.expr()));
        }
        if (null != ctx.orderByClause()) {
            result.setOrderBySegment((OrderBySegment) visit(ctx.orderByClause()));
        }
        if (null != ctx.frameClause()) {
            result.setFrameClause(new CommonExpressionSegment(ctx.frameClause().start.getStartIndex(), ctx.frameClause().stop.getStopIndex(), ctx.frameClause().getText()));
        }
        if (null != ctx.identifier()) {
            result.setWindowName(new IdentifierValue(ctx.identifier().getText()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitWindowFunction(final WindowFunctionContext ctx) {
        super.visitWindowFunction(ctx);
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.funcName.getText(), getOriginalText(ctx));
        if (null != ctx.NTILE()) {
            result.getParameters().add((ExpressionSegment) visit(ctx.simpleExpr()));
        }
        if (null != ctx.LEAD() || null != ctx.LAG() || null != ctx.FIRST_VALUE() || null != ctx.LAST_VALUE()) {
            result.getParameters().add((ExpressionSegment) visit(ctx.expr()));
        }
        if (null != ctx.NTH_VALUE()) {
            result.getParameters().add((ExpressionSegment) visit(ctx.expr()));
            result.getParameters().add((ExpressionSegment) visit(ctx.simpleExpr()));
        }
        result.setWindow((WindowItemSegment) visit(ctx.windowingClause()));
        return result;
    }
    
    @Override
    public ASTNode visitWindowingClause(final WindowingClauseContext ctx) {
        WindowItemSegment result = new WindowItemSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        if (null != ctx.windowName) {
            result.setWindowName((IdentifierValue) visit(ctx.windowName));
        }
        if (null != ctx.windowSpecification()) {
            WindowItemSegment windowItemSegment = (WindowItemSegment) visit(ctx.windowSpecification());
            result.setPartitionListSegments(windowItemSegment.getPartitionListSegments());
            result.setOrderBySegment(windowItemSegment.getOrderBySegment());
            result.setFrameClause(windowItemSegment.getFrameClause());
        }
        return result;
    }
}
