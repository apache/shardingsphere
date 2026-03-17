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
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.StopRoutineLoadContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.DoStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.HandlerStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ImportStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.IdentifierContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.IndexHintContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.JobPropertyContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.AssignmentContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.LoadDataPropertyContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.LoadDataIgnoreLinesContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.LoadDataSetClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.LeadLagInfoContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.DorisLoadDataStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.LoadPropertyContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.String_Context;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.UserVariableContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.BrokerLoadStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.BrokerLoadDataDescContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.BrokerLoadPropertyContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.BrokerLoadWithClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.BrokerLoadPropertiesContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.BrokerLoadDataPropertiesContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.BrokerLoadSetAssignmentContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.ColumnNameContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.LoadStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.LoadXmlStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.WindowClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.WindowFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.WindowItemContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.WindowSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.DorisStatementParser.WindowingClauseContext;
import org.apache.shardingsphere.sql.parser.engine.doris.visitor.statement.DorisStatementVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.load.BrokerLoadDataDescSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.load.ColNameOrUserVarSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.load.IgnoreLinesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.load.LiteralValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.PartitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.job.JobNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.property.PropertiesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.property.PropertySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnMappingSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
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
import org.apache.shardingsphere.sql.parser.statement.core.value.literal.impl.NumberLiteralValue;
import org.apache.shardingsphere.sql.parser.statement.doris.dml.DorisAlterRoutineLoadStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dml.DorisCreateRoutineLoadStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dml.DorisPauseRoutineLoadStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dml.DorisResumeRoutineLoadStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dml.DorisStopRoutineLoadStatement;
import org.apache.shardingsphere.sql.parser.statement.doris.dml.DorisBrokerLoadStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dml.MySQLLoadDataStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dml.MySQLHandlerStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dml.MySQLImportStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dml.MySQLLoadXMLStatement;

import java.util.Collection;
import java.util.Collections;
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
        if (null != ctx.brokerLoadStatement()) {
            return visit(ctx.brokerLoadStatement());
        }
        return null == ctx.dorisLoadDataStatement() ? visit(ctx.loadXmlStatement()) : visit(ctx.dorisLoadDataStatement());
    }
    
    @Override
    public ASTNode visitBrokerLoadStatement(final BrokerLoadStatementContext ctx) {
        DorisBrokerLoadStatement result = new DorisBrokerLoadStatement(getDatabaseType());
        result.setLoadLabel(ctx.identifier().getText());
        if (null != ctx.owner()) {
            result.setDatabase(new DatabaseSegment(ctx.owner().start.getStartIndex(), ctx.owner().stop.getStopIndex(), new IdentifierValue(ctx.owner().getText())));
        }
        for (BrokerLoadDataDescContext each : ctx.brokerLoadDataDesc()) {
            result.getDataDescs().add(buildBrokerLoadDataDesc(each));
        }
        BrokerLoadWithClauseContext withCtx = ctx.brokerLoadWithClause();
        if (null != withCtx.S3()) {
            result.setBrokerType("S3");
        } else if (null != withCtx.HDFS()) {
            result.setBrokerType("HDFS");
        } else if (null != withCtx.BROKER()) {
            result.setBrokerType("BROKER");
            result.setBrokerName(SQLUtils.getExactlyValue(withCtx.string_().getText()));
        }
        PropertiesSegment brokerProps = new PropertiesSegment(withCtx.start.getStartIndex(), withCtx.stop.getStopIndex());
        for (BrokerLoadPropertyContext each : withCtx.brokerLoadProperty()) {
            brokerProps.getProperties().add(buildBrokerLoadProperty(each));
        }
        result.setBrokerProperties(brokerProps);
        if (null != ctx.brokerLoadProperties()) {
            BrokerLoadPropertiesContext loadPropsCtx = ctx.brokerLoadProperties();
            PropertiesSegment loadProps = new PropertiesSegment(loadPropsCtx.start.getStartIndex(), loadPropsCtx.stop.getStopIndex());
            for (BrokerLoadPropertyContext each : loadPropsCtx.brokerLoadProperty()) {
                loadProps.getProperties().add(buildBrokerLoadProperty(each));
            }
            result.setLoadProperties(loadProps);
        }
        if (null != ctx.string_()) {
            result.setComment(SQLUtils.getExactlyValue(ctx.string_().getText()));
        }
        result.addParameterMarkers(getParameterMarkerSegments());
        return result;
    }
    
    private BrokerLoadDataDescSegment buildBrokerLoadDataDesc(final BrokerLoadDataDescContext ctx) {
        BrokerLoadDataDescSegment result = new BrokerLoadDataDescSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        if (null != ctx.MERGE()) {
            result.setMergeType("MERGE");
        } else if (null != ctx.APPEND()) {
            result.setMergeType("APPEND");
        } else if (ctx.DELETE().size() > (null != ctx.ON() ? 1 : 0)) {
            result.setMergeType("DELETE");
        }
        for (String_Context each : ctx.string_()) {
            result.getFilePaths().add(SQLUtils.getExactlyValue(each.getText()));
        }
        result.setNegative(null != ctx.NEGATIVE());
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        if (null != ctx.partitionNames()) {
            for (IdentifierContext each : ctx.partitionNames().identifier()) {
                result.getPartitions().add(new PartitionSegment(each.getStart().getStartIndex(), each.getStop().getStopIndex(), (IdentifierValue) visit(each)));
            }
        }
        if (null != ctx.brokerLoadColumnTerminator()) {
            result.setColumnSeparator(SQLUtils.getExactlyValue(ctx.brokerLoadColumnTerminator().string_().getText()));
        }
        if (null != ctx.brokerLoadLineTerminator()) {
            result.setLineDelimiter(SQLUtils.getExactlyValue(ctx.brokerLoadLineTerminator().string_().getText()));
        }
        if (null != ctx.brokerLoadFormatClause()) {
            result.setFormatType(SQLUtils.getExactlyValue(ctx.brokerLoadFormatClause().string_().getText()));
        }
        if (null != ctx.brokerLoadCompressClause()) {
            result.setCompressType(SQLUtils.getExactlyValue(ctx.brokerLoadCompressClause().string_().getText()));
        }
        extractBrokerLoadDataDescColumns(ctx, result);
        extractBrokerLoadDataDescExpressions(ctx, result);
        if (null != ctx.brokerLoadDataProperties()) {
            BrokerLoadDataPropertiesContext dataPropsCtx = ctx.brokerLoadDataProperties();
            PropertiesSegment dataProps = new PropertiesSegment(dataPropsCtx.start.getStartIndex(), dataPropsCtx.stop.getStopIndex());
            for (BrokerLoadPropertyContext each : dataPropsCtx.brokerLoadProperty()) {
                dataProps.getProperties().add(buildBrokerLoadProperty(each));
            }
            result.setDataProperties(dataProps);
        }
        return result;
    }
    
    private void extractBrokerLoadDataDescColumns(final BrokerLoadDataDescContext ctx, final BrokerLoadDataDescSegment result) {
        if (null != ctx.brokerLoadColumnList()) {
            for (ColumnNameContext each : ctx.brokerLoadColumnList().columnName()) {
                result.getColumnList().add((ColumnSegment) visit(each));
            }
        }
        if (null != ctx.columnName() && !ctx.columnName().isEmpty()) {
            for (ColumnNameContext each : ctx.columnName()) {
                result.getColumnsFromPath().add((ColumnSegment) visit(each));
            }
        }
        if (null != ctx.brokerLoadSetAssignment() && !ctx.brokerLoadSetAssignment().isEmpty()) {
            for (BrokerLoadSetAssignmentContext each : ctx.brokerLoadSetAssignment()) {
                ColumnSegment column = (ColumnSegment) visit(each.columnName());
                ExpressionSegment value = (ExpressionSegment) visit(each.expr());
                ColumnAssignmentSegment assignment = new ColumnAssignmentSegment(each.start.getStartIndex(), each.stop.getStopIndex(), Collections.singletonList(column), value);
                result.getSetAssignments().add(assignment);
            }
        }
    }
    
    private void extractBrokerLoadDataDescExpressions(final BrokerLoadDataDescContext ctx, final BrokerLoadDataDescSegment result) {
        if (null != ctx.expr() && !ctx.expr().isEmpty()) {
            int exprIndex = 0;
            if (null != ctx.PRECEDING()) {
                result.setPrecedingFilter((ExpressionSegment) visit(ctx.expr(exprIndex)));
                exprIndex++;
            }
            if (null != ctx.WHERE()) {
                result.setWhereExpr((ExpressionSegment) visit(ctx.expr(exprIndex)));
                exprIndex++;
            }
            if (null != ctx.ON()) {
                result.setDeleteOnExpr((ExpressionSegment) visit(ctx.expr(exprIndex)));
            }
        }
        if (null != ctx.ORDER()) {
            result.setOrderByColumn(ctx.identifier().getText());
        }
    }
    
    private PropertySegment buildBrokerLoadProperty(final BrokerLoadPropertyContext ctx) {
        String key = getPropertyKey(ctx.identifier(), ctx.SINGLE_QUOTED_TEXT(), ctx.DOUBLE_QUOTED_TEXT());
        String value = SQLUtils.getExactlyValue(ctx.literals().getText());
        return new PropertySegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), key, value);
    }
    
    @Override
    public ASTNode visitDorisLoadDataStatement(final DorisLoadDataStatementContext ctx) {
        String_Context fileCtx = ctx.loadDataFileName;
        LiteralValueSegment fileNameSeg = new LiteralValueSegment(fileCtx.start.getStartIndex(), fileCtx.stop.getStopIndex(), SQLUtils.getExactlyValue(fileCtx.getText()));
        boolean local = null != ctx.LOCAL();
        MySQLLoadDataStatement result = new MySQLLoadDataStatement(getDatabaseType(), (SimpleTableSegment) visit(ctx.tableName()));
        result.setLocal(local);
        result.setFileName(fileNameSeg);
        if (null != ctx.partitionNames()) {
            for (IdentifierContext each : ctx.partitionNames().identifier()) {
                result.getPartitions().add(new PartitionSegment(each.getStart().getStartIndex(), each.getStop().getStopIndex(), (IdentifierValue) visit(each)));
            }
        }
        if (null != ctx.loadDataColumnTerminator()) {
            String_Context s = ctx.loadDataColumnTerminator().string_();
            result.setColumnSeparator(new LiteralValueSegment(s.start.getStartIndex(), s.stop.getStopIndex(), SQLUtils.getExactlyValue(s.getText())));
        }
        if (null != ctx.loadDataLineTerminator()) {
            String_Context s = ctx.loadDataLineTerminator().string_();
            result.setLineDelimiter(new LiteralValueSegment(s.start.getStartIndex(), s.stop.getStopIndex(), SQLUtils.getExactlyValue(s.getText())));
        }
        if (null != ctx.loadDataIgnoreLines()) {
            LoadDataIgnoreLinesContext ignoreCtx = ctx.loadDataIgnoreLines();
            long number = Long.parseLong(ignoreCtx.numberLiterals().getText().replaceAll("^[+]", ""));
            String unit = null != ignoreCtx.LINES() ? "LINES" : "ROWS";
            result.setIgnoreLines(new IgnoreLinesSegment(ignoreCtx.start.getStartIndex(), ignoreCtx.stop.getStopIndex(), number, unit));
        }
        if (null != ctx.fieldOrVarSpec() && null != ctx.fieldOrVarSpec().userVariable()) {
            for (UserVariableContext uv : ctx.fieldOrVarSpec().userVariable()) {
                result.getColumnList().add(new ColNameOrUserVarSegment(uv.start.getStartIndex(), uv.stop.getStopIndex(), new IdentifierValue(SQLUtils.getExactlyValue(uv.getText()))));
            }
        }
        if (null != ctx.loadDataSetClause()) {
            LoadDataSetClauseContext setCtx = ctx.loadDataSetClause();
            Collection<ColumnAssignmentSegment> assignments = new LinkedList<>();
            for (AssignmentContext each : setCtx.assignment()) {
                assignments.add((ColumnAssignmentSegment) visit(each));
            }
            result.setSetAssignments(new SetAssignmentSegment(setCtx.start.getStartIndex(), setCtx.stop.getStopIndex(), assignments));
        }
        if (null != ctx.loadDataProperties()) {
            PropertiesSegment propertiesSegment = new PropertiesSegment(ctx.loadDataProperties().start.getStartIndex(), ctx.loadDataProperties().stop.getStopIndex());
            for (LoadDataPropertyContext each : ctx.loadDataProperties().loadDataProperty()) {
                String key = getPropertyKey(each.identifier(), each.SINGLE_QUOTED_TEXT(), each.DOUBLE_QUOTED_TEXT());
                String value = SQLUtils.getExactlyValue(each.literals().getText());
                propertiesSegment.getProperties().add(new PropertySegment(each.start.getStartIndex(), each.stop.getStopIndex(), key, value));
            }
            result.setProperties(propertiesSegment);
        }
        return result;
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
    
    @Override
    public ASTNode visitStopRoutineLoad(final StopRoutineLoadContext ctx) {
        DorisStopRoutineLoadStatement result = new DorisStopRoutineLoadStatement(getDatabaseType());
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
        List<Integer> indexStartIndices = new LinkedList<>();
        List<Integer> indexStopIndices = new LinkedList<>();
        if (null != ctx.indexNameList()) {
            ctx.indexNameList().indexName().forEach(each -> {
                indexNames.add(each.getText());
                indexStartIndices.add(each.getStart().getStartIndex());
                indexStopIndices.add(each.getStop().getStopIndex());
            });
        }
        String useType;
        if (null != ctx.USE()) {
            useType = ctx.USE().getText();
        } else if (null != ctx.IGNORE()) {
            useType = ctx.IGNORE().getText();
        } else {
            useType = ctx.FORCE().getText();
        }
        IndexHintSegment result = new IndexHintSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), indexNames, indexStartIndices, indexStopIndices, useType,
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
        FunctionSegment result = new FunctionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ctx.funcName.getText(), getOriginalText(ctx));
        if (null != ctx.NTILE()) {
            result.getParameters().add((ExpressionSegment) visit(ctx.simpleExpr()));
        }
        if (null != ctx.LEAD() || null != ctx.LAG() || null != ctx.FIRST_VALUE() || null != ctx.LAST_VALUE()) {
            result.getParameters().add((ExpressionSegment) visit(ctx.expr()));
            appendLeadLagParameters(result.getParameters(), ctx.leadLagInfo());
        }
        if (null != ctx.NTH_VALUE()) {
            result.getParameters().add((ExpressionSegment) visit(ctx.expr()));
            result.getParameters().add((ExpressionSegment) visit(ctx.simpleExpr()));
        }
        result.setWindow((WindowItemSegment) visit(ctx.windowingClause()));
        return result;
    }
    
    private void appendLeadLagParameters(final Collection<ExpressionSegment> parameters, final LeadLagInfoContext ctx) {
        if (null == ctx) {
            return;
        }
        if (null != ctx.NUMBER_()) {
            parameters.add(
                    new LiteralExpressionSegment(ctx.NUMBER_().getSymbol().getStartIndex(), ctx.NUMBER_().getSymbol().getStopIndex(), new NumberLiteralValue(ctx.NUMBER_().getText()).getValue()));
        } else {
            int startIndex = ctx.QUESTION_().getSymbol().getStartIndex();
            int stopIndex = ctx.QUESTION_().getSymbol().getStopIndex();
            ParameterMarkerExpressionSegment parameterMarker = new ParameterMarkerExpressionSegment(startIndex, stopIndex, getParameterMarkerSegments().size());
            getParameterMarkerSegments().add(parameterMarker);
            parameters.add(parameterMarker);
        }
        if (null != ctx.expr()) {
            parameters.add((ExpressionSegment) visit(ctx.expr()));
        }
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
