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

package org.apache.shardingsphere.sql.parser.engine.opengauss.visitor.statement.type;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DALStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.AnalyzeTableContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.CheckpointContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ColIdContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ConfigurationParameterClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.EmptyStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ExplainContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ExplainableStmtContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.LoadContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ResetParameterContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.SetContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.ShowContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.VacuumContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.VacuumRelationContext;
import org.apache.shardingsphere.sql.parser.autogen.OpenGaussStatementParser.VacuumRelationListContext;
import org.apache.shardingsphere.sql.parser.engine.opengauss.visitor.statement.OpenGaussStatementVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.VariableAssignSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.AnalyzeTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.EmptyStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ExplainStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ShowStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.postgresql.dal.PostgreSQLCheckpointStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.dal.PostgreSQLLoadStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.dal.PostgreSQLResetParameterStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.dal.PostgreSQLVacuumStatement;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * DAL statement visitor for openGauss.
 */
public final class OpenGaussDALStatementVisitor extends OpenGaussStatementVisitor implements DALStatementVisitor {
    
    public OpenGaussDALStatementVisitor(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    @Override
    public ASTNode visitShow(final ShowContext ctx) {
        if (null != ctx.varName()) {
            return new ShowStatement(getDatabaseType(), ctx.varName().getText());
        }
        if (null != ctx.ZONE()) {
            return new ShowStatement(getDatabaseType(), "timezone");
        }
        if (null != ctx.ISOLATION()) {
            return new ShowStatement(getDatabaseType(), "transaction_isolation");
        }
        if (null != ctx.AUTHORIZATION()) {
            return new ShowStatement(getDatabaseType(), "session_authorization");
        }
        return new ShowStatement(getDatabaseType(), "ALL");
    }
    
    @Override
    public ASTNode visitSet(final SetContext ctx) {
        List<VariableAssignSegment> variableAssigns = new LinkedList<>();
        if (null != ctx.configurationParameterClause()) {
            VariableAssignSegment variableAssignSegment = (VariableAssignSegment) visit(ctx.configurationParameterClause());
            if (null != ctx.runtimeScope()) {
                variableAssignSegment.getVariable().setScope(ctx.runtimeScope().getText());
            }
            variableAssigns.add(variableAssignSegment);
        }
        return new SetStatement(getDatabaseType(), variableAssigns);
    }
    
    @Override
    public ASTNode visitConfigurationParameterClause(final ConfigurationParameterClauseContext ctx) {
        return new VariableAssignSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(),
                new VariableSegment(ctx.varName().start.getStartIndex(), ctx.varName().stop.getStopIndex(), ctx.varName().getText()), getAssignValue(ctx));
    }
    
    private String getAssignValue(final ConfigurationParameterClauseContext ctx) {
        if (null != ctx.varList()) {
            return ctx.varList().getText();
        }
        if (null != ctx.DEFAULT()) {
            return ctx.DEFAULT().getText();
        }
        return null;
    }
    
    @Override
    public ASTNode visitResetParameter(final ResetParameterContext ctx) {
        return new PostgreSQLResetParameterStatement(getDatabaseType(), null == ctx.ALL() ? ctx.identifier().getText() : "ALL");
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitAnalyzeTable(final AnalyzeTableContext ctx) {
        return new AnalyzeTableStatement(getDatabaseType(),
                null == ctx.vacuumRelationList() ? Collections.emptyList() : ((CollectionValue<SimpleTableSegment>) visit(ctx.vacuumRelationList())).getValue());
    }
    
    @Override
    public ASTNode visitVacuumRelationList(final VacuumRelationListContext ctx) {
        CollectionValue<SimpleTableSegment> result = new CollectionValue<>();
        for (VacuumRelationContext each : ctx.vacuumRelation()) {
            ColIdContext colId = each.qualifiedName().colId();
            TableNameSegment tableName = new TableNameSegment(colId.start.getStartIndex(), colId.stop.getStopIndex(), new IdentifierValue(colId.getText()));
            result.getValue().add(new SimpleTableSegment(tableName));
        }
        return result;
    }
    
    @Override
    public ASTNode visitLoad(final LoadContext ctx) {
        return new PostgreSQLLoadStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitVacuum(final VacuumContext ctx) {
        return new PostgreSQLVacuumStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitExplain(final ExplainContext ctx) {
        return new ExplainStatement(getDatabaseType(), (SQLStatement) visit(ctx.explainableStmt()));
    }
    
    @Override
    public ASTNode visitExplainableStmt(final ExplainableStmtContext ctx) {
        if (null != ctx.select()) {
            return visit(ctx.select());
        }
        if (null != ctx.insert()) {
            return visit(ctx.insert());
        }
        if (null != ctx.update()) {
            return visit(ctx.update());
        }
        if (null != ctx.delete()) {
            return visit(ctx.delete());
        }
        if (null != ctx.declare()) {
            // TODO visit declare statement
            return visit(ctx.declare());
        }
        if (null != ctx.executeStmt()) {
            return visit(ctx.executeStmt());
        }
        if (null != ctx.createMaterializedView()) {
            // TODO visit create materialized view statement
            return visit(ctx.createMaterializedView());
        }
        // TODO visit refresh materialized view statement
        return visit(ctx.refreshMatViewStmt());
    }
    
    @Override
    public ASTNode visitCheckpoint(final CheckpointContext ctx) {
        return new PostgreSQLCheckpointStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitEmptyStatement(final EmptyStatementContext ctx) {
        return new EmptyStatement(getDatabaseType());
    }
}
