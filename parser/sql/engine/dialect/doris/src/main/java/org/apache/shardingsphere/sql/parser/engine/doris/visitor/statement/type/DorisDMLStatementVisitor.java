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

import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DMLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.AlterRoutineLoadContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.CallContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ColumnsClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ColumnMappingContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.CreateRoutineLoadContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.DataSourcePropertyContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.PauseRoutineLoadContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ResumeRoutineLoadContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.DoStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.HandlerStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ImportStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.IdentifierContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.IndexHintContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.JobPropertyContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.LoadDataStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.LoadPropertyContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.LoadStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.LoadXmlStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.WindowClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.WindowFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.WindowItemContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.WindowSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.WindowingClauseContext;
import org.apache.shardingsphere.sql.parser.engine.doris.visitor.statement.DorisStatementVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.PartitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.job.JobNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.property.PropertiesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.property.PropertySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnMappingSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WindowItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WindowSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.IndexHintSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.CallStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DoStatement;
import org.apache.shardingsphere.sql.parser.statement.core.util.SQLUtils;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.doris.dml.DorisAlterRoutineLoadStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dml.DorisCreateRoutineLoadStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dml.DorisPauseRoutineLoadStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dml.DorisResumeRoutineLoadStatement;
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
    public ASTNode visitCreateRoutineLoad(final CreateRoutineLoadContext ctx) {
        DorisCreateRoutineLoadStatement result = new DorisCreateRoutineLoadStatement(getDatabaseType());
        if (null != ctx.jobName()) {
            JobNameSegment jobName = new JobNameSegment(ctx.jobName().start.getStartIndex(), ctx.jobName().stop.getStopIndex(), new IdentifierValue(ctx.jobName().getText()));
            if (null != ctx.owner()) {
                OwnerSegment owner = (OwnerSegment) visit(ctx.owner());
                jobName.setOwner(owner);
                result.setDatabase(new DatabaseSegment(ctx.owner().start.getStartIndex(), ctx.owner().stop.getStopIndex(), new IdentifierValue(ctx.owner().getText())));
            }
            result.setJobName(jobName);
        }
        if (null != ctx.tableName()) {
            result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        }
        if (null != ctx.mergeType()) {
            result.setMergeType(ctx.mergeType().getText());
        }
        if (null != ctx.loadProperty()) {
            for (int i = 0; i < ctx.loadProperty().size(); i++) {
                LoadPropertyContext loadPropCtx = ctx.loadProperty(i);
                if (null != loadPropCtx.columnSeparatorClause()) {
                    result.setColumnSeparator(SQLUtils.getExactlyValue(loadPropCtx.columnSeparatorClause().string_().getText()));
                }
                if (null != loadPropCtx.columnsClause()) {
                    processColumnMappings(loadPropCtx.columnsClause(), result);
                }
                if (null != loadPropCtx.precedingFilterClause()) {
                    result.setPrecedingFilter((ExpressionSegment) visit(loadPropCtx.precedingFilterClause().expr()));
                }
                if (null != loadPropCtx.whereClause()) {
                    result.setWhere((WhereSegment) visit(loadPropCtx.whereClause()));
                }
                if (null != loadPropCtx.partitionNames()) {
                    for (IdentifierContext each : loadPropCtx.partitionNames().identifier()) {
                        PartitionSegment partitionSegment = new PartitionSegment(each.getStart().getStartIndex(), each.getStop().getStopIndex(), (IdentifierValue) visit(each));
                        result.getPartitions().add(partitionSegment);
                    }
                }
                if (null != loadPropCtx.deleteOnClause()) {
                    result.setDeleteOn((ExpressionSegment) visit(loadPropCtx.deleteOnClause().expr()));
                }
                if (null != loadPropCtx.orderByClause()) {
                    result.setOrderBy((OrderBySegment) visit(loadPropCtx.orderByClause()));
                }
            }
        }
        if (null != ctx.jobProperties()) {
            PropertiesSegment propertiesSegment = new PropertiesSegment(ctx.jobProperties().start.getStartIndex(), ctx.jobProperties().stop.getStopIndex());
            for (int i = 0; i < ctx.jobProperties().jobProperty().size(); i++) {
                JobPropertyContext propertyCtx = ctx.jobProperties().jobProperty(i);
                String key = getPropertyKey(propertyCtx.identifier(), propertyCtx.SINGLE_QUOTED_TEXT(), propertyCtx.DOUBLE_QUOTED_TEXT());
                String value = SQLUtils.getExactlyValue(propertyCtx.literals().getText());
                PropertySegment propertySegment = new PropertySegment(propertyCtx.start.getStartIndex(), propertyCtx.stop.getStopIndex(), key, value);
                propertiesSegment.getProperties().add(propertySegment);
            }
            result.setJobProperties(propertiesSegment);
        }
        if (null != ctx.dataSource()) {
            result.setDataSource(ctx.dataSource().getText());
        }
        if (null != ctx.dataSourceProperties() && null != ctx.dataSourceProperties().dataSourceProperty()) {
            PropertiesSegment propertiesSegment = new PropertiesSegment(ctx.dataSourceProperties().start.getStartIndex(), ctx.dataSourceProperties().stop.getStopIndex());
            for (int i = 0; i < ctx.dataSourceProperties().dataSourceProperty().size(); i++) {
                DataSourcePropertyContext propertyCtx = ctx.dataSourceProperties().dataSourceProperty(i);
                String key = getPropertyKey(propertyCtx.identifier(), propertyCtx.SINGLE_QUOTED_TEXT(), propertyCtx.DOUBLE_QUOTED_TEXT());
                String value = SQLUtils.getExactlyValue(propertyCtx.literals().getText());
                PropertySegment propertySegment = new PropertySegment(propertyCtx.start.getStartIndex(), propertyCtx.stop.getStopIndex(), key, value);
                propertiesSegment.getProperties().add(propertySegment);
            }
            result.setDataSourceProperties(propertiesSegment);
        }
        if (null != ctx.string_()) {
            result.setComment(SQLUtils.getExactlyValue(ctx.string_().getText()));
        }
        result.addParameterMarkers(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitAlterRoutineLoad(final AlterRoutineLoadContext ctx) {
        DorisAlterRoutineLoadStatement result = new DorisAlterRoutineLoadStatement(getDatabaseType());
        if (null != ctx.jobName()) {
            JobNameSegment jobName = new JobNameSegment(ctx.jobName().start.getStartIndex(), ctx.jobName().stop.getStopIndex(), new IdentifierValue(ctx.jobName().getText()));
            if (null != ctx.owner()) {
                OwnerSegment owner = (OwnerSegment) visit(ctx.owner());
                jobName.setOwner(owner);
            }
            result.setJobName(jobName);
        }
        if (null != ctx.jobProperties()) {
            PropertiesSegment propertiesSegment = new PropertiesSegment(ctx.jobProperties().start.getStartIndex(), ctx.jobProperties().stop.getStopIndex());
            for (int i = 0; i < ctx.jobProperties().jobProperty().size(); i++) {
                JobPropertyContext propertyCtx = ctx.jobProperties().jobProperty(i);
                String key = getPropertyKey(propertyCtx.identifier(), propertyCtx.SINGLE_QUOTED_TEXT(), propertyCtx.DOUBLE_QUOTED_TEXT());
                String value = SQLUtils.getExactlyValue(propertyCtx.literals().getText());
                PropertySegment propertySegment = new PropertySegment(propertyCtx.start.getStartIndex(), propertyCtx.stop.getStopIndex(), key, value);
                propertiesSegment.getProperties().add(propertySegment);
            }
            result.setJobProperties(propertiesSegment);
        }
        if (null != ctx.dataSource()) {
            result.setDataSource(ctx.dataSource().getText());
        }
        if (null != ctx.dataSourceProperties() && null != ctx.dataSourceProperties().dataSourceProperty()) {
            PropertiesSegment propertiesSegment = new PropertiesSegment(ctx.dataSourceProperties().start.getStartIndex(), ctx.dataSourceProperties().stop.getStopIndex());
            for (int i = 0; i < ctx.dataSourceProperties().dataSourceProperty().size(); i++) {
                DataSourcePropertyContext propertyCtx = ctx.dataSourceProperties().dataSourceProperty(i);
                String key = getPropertyKey(propertyCtx.identifier(), propertyCtx.SINGLE_QUOTED_TEXT(), propertyCtx.DOUBLE_QUOTED_TEXT());
                String value = SQLUtils.getExactlyValue(propertyCtx.literals().getText());
                PropertySegment propertySegment = new PropertySegment(propertyCtx.start.getStartIndex(), propertyCtx.stop.getStopIndex(), key, value);
                propertiesSegment.getProperties().add(propertySegment);
            }
            result.setDataSourceProperties(propertiesSegment);
        }
        result.addParameterMarkers(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitPauseRoutineLoad(final PauseRoutineLoadContext ctx) {
        DorisPauseRoutineLoadStatement result = new DorisPauseRoutineLoadStatement(getDatabaseType());
        result.setAll(null != ctx.ALL());
        if (null != ctx.jobName()) {
            JobNameSegment jobName = new JobNameSegment(ctx.jobName().start.getStartIndex(), ctx.jobName().stop.getStopIndex(), new IdentifierValue(ctx.jobName().getText()));
            if (null != ctx.owner()) {
                OwnerSegment owner = (OwnerSegment) visit(ctx.owner());
                jobName.setOwner(owner);
            }
            result.setJobName(jobName);
        }
        result.addParameterMarkers(getParameterMarkerSegments());
        return result;
    }
    
    @Override
    public ASTNode visitResumeRoutineLoad(final ResumeRoutineLoadContext ctx) {
        DorisResumeRoutineLoadStatement result = new DorisResumeRoutineLoadStatement(getDatabaseType());
        result.setAll(null != ctx.ALL());
        if (null != ctx.jobName()) {
            JobNameSegment jobName = new JobNameSegment(ctx.jobName().start.getStartIndex(), ctx.jobName().stop.getStopIndex(), new IdentifierValue(ctx.jobName().getText()));
            if (null != ctx.owner()) {
                OwnerSegment owner = (OwnerSegment) visit(ctx.owner());
                jobName.setOwner(owner);
            }
            result.setJobName(jobName);
        }
        result.addParameterMarkers(getParameterMarkerSegments());
        return result;
    }
    
    private void processColumnMappings(final ColumnsClauseContext columnsClauseCtx, final DorisCreateRoutineLoadStatement statement) {
        for (int i = 0; i < columnsClauseCtx.columnMapping().size(); i++) {
            ColumnMappingContext mappingCtx = columnsClauseCtx.columnMapping(i);
            ColumnSegment column = (ColumnSegment) visit(mappingCtx.columnName());
            ColumnMappingSegment columnMapping = new ColumnMappingSegment(mappingCtx.start.getStartIndex(), mappingCtx.stop.getStopIndex(), column);
            if (null != mappingCtx.expr()) {
                columnMapping.setMappingExpression((ExpressionSegment) visit(mappingCtx.expr()));
            }
            statement.getColumnMappings().add(columnMapping);
        }
    }
    
    private String getPropertyKey(final IdentifierContext identifier, final TerminalNode singleQuotedText, final TerminalNode doubleQuotedText) {
        if (null != singleQuotedText) {
            return SQLUtils.getExactlyValue(singleQuotedText.getText());
        }
        if (null != doubleQuotedText) {
            return SQLUtils.getExactlyValue(doubleQuotedText.getText());
        }
        return SQLUtils.getExactlyValue(identifier.getText());
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
