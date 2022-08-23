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

package org.apache.shardingsphere.migration.distsql.parser.core;

import com.google.common.base.Splitter;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.AddMigrationSourceResourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.AlgorithmDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.ApplyMigrationContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.CheckMigrationContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.CleanMigrationContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.DropMigrationSourceResourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.MigrateTableContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.PasswordContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.PropertiesDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.PropertyContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.ResetMigrationContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.ResourceDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.RestoreMigrationSourceWritingContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.ShowMigrationCheckAlgorithmsContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.ShowMigrationListContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.ShowMigrationSourceResourcesContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.ShowMigrationStatusContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.StartMigrationContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.StopMigrationContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.StopMigrationSourceWritingContext;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.distsql.parser.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.HostnameAndPortBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.URLBasedDataSourceSegment;
import org.apache.shardingsphere.migration.distsql.statement.AddMigrationSourceResourceStatement;
import org.apache.shardingsphere.migration.distsql.statement.ApplyMigrationStatement;
import org.apache.shardingsphere.migration.distsql.statement.CheckMigrationStatement;
import org.apache.shardingsphere.migration.distsql.statement.CleanMigrationStatement;
import org.apache.shardingsphere.migration.distsql.statement.DropMigrationSourceResourceStatement;
import org.apache.shardingsphere.migration.distsql.statement.MigrateTableStatement;
import org.apache.shardingsphere.migration.distsql.statement.ResetMigrationStatement;
import org.apache.shardingsphere.migration.distsql.statement.RestoreMigrationSourceWritingStatement;
import org.apache.shardingsphere.migration.distsql.statement.ShowMigrationCheckAlgorithmsStatement;
import org.apache.shardingsphere.migration.distsql.statement.ShowMigrationListStatement;
import org.apache.shardingsphere.migration.distsql.statement.ShowMigrationSourceResourcesStatement;
import org.apache.shardingsphere.migration.distsql.statement.ShowMigrationStatusStatement;
import org.apache.shardingsphere.migration.distsql.statement.StartMigrationStatement;
import org.apache.shardingsphere.migration.distsql.statement.StopMigrationSourceWritingStatement;
import org.apache.shardingsphere.migration.distsql.statement.StopMigrationStatement;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * SQL statement visitor for migration dist SQL.
 */
public final class MigrationDistSQLStatementVisitor extends MigrationDistSQLStatementBaseVisitor<ASTNode> implements SQLVisitor {
    
    @Override
    public ASTNode visitMigrateTable(final MigrateTableContext ctx) {
        List<String> source = Splitter.on('.').splitToList(getIdentifierValue(ctx.sourceTableName()));
        List<String> target = Splitter.on('.').splitToList(getIdentifierValue(ctx.targetTableName()));
        String sourceResourceName = source.get(0);
        String sourceSchemaName = 3 == source.size() ? source.get(1) : null;
        String sourceTableName = source.get(source.size() - 1);
        String targetDatabaseName = target.size() > 1 ? target.get(0) : null;
        String targetTableName = target.get(target.size() - 1);
        return new MigrateTableStatement(sourceResourceName, sourceSchemaName, sourceTableName, targetDatabaseName, targetTableName);
    }
    
    @Override
    public ASTNode visitShowMigrationList(final ShowMigrationListContext ctx) {
        return new ShowMigrationListStatement();
    }
    
    @Override
    public ASTNode visitShowMigrationStatus(final ShowMigrationStatusContext ctx) {
        return new ShowMigrationStatusStatement(getIdentifierValue(ctx.jobId()));
    }
    
    @Override
    public ASTNode visitStartMigration(final StartMigrationContext ctx) {
        return new StartMigrationStatement(getIdentifierValue(ctx.jobId()));
    }
    
    @Override
    public ASTNode visitStopMigration(final StopMigrationContext ctx) {
        return new StopMigrationStatement(getIdentifierValue(ctx.jobId()));
    }
    
    @Override
    public ASTNode visitCleanMigration(final CleanMigrationContext ctx) {
        return new CleanMigrationStatement(getIdentifierValue(ctx.jobId()));
    }
    
    @Override
    public ASTNode visitResetMigration(final ResetMigrationContext ctx) {
        return new ResetMigrationStatement(getIdentifierValue(ctx.jobId()));
    }
    
    @Override
    public ASTNode visitCheckMigration(final CheckMigrationContext ctx) {
        return new CheckMigrationStatement(getIdentifierValue(ctx.jobId()), null == ctx.algorithmDefinition() ? null : (AlgorithmSegment) visit(ctx.algorithmDefinition()));
    }
    
    @Override
    public ASTNode visitShowMigrationCheckAlgorithms(final ShowMigrationCheckAlgorithmsContext ctx) {
        return new ShowMigrationCheckAlgorithmsStatement();
    }
    
    @Override
    public ASTNode visitStopMigrationSourceWriting(final StopMigrationSourceWritingContext ctx) {
        return new StopMigrationSourceWritingStatement(getIdentifierValue(ctx.jobId()));
    }
    
    @Override
    public ASTNode visitRestoreMigrationSourceWriting(final RestoreMigrationSourceWritingContext ctx) {
        return new RestoreMigrationSourceWritingStatement(getIdentifierValue(ctx.jobId()));
    }
    
    @Override
    public ASTNode visitApplyMigration(final ApplyMigrationContext ctx) {
        return new ApplyMigrationStatement(getIdentifierValue(ctx.jobId()));
    }
    
    @Override
    public ASTNode visitAlgorithmDefinition(final AlgorithmDefinitionContext ctx) {
        return new AlgorithmSegment(getIdentifierValue(ctx.algorithmTypeName()), getAlgorithmProperties(ctx));
    }
    
    private Properties getAlgorithmProperties(final AlgorithmDefinitionContext ctx) {
        Properties result = new Properties();
        if (null == ctx.algorithmProperties()) {
            return result;
        }
        for (MigrationDistSQLStatementParser.AlgorithmPropertyContext each : ctx.algorithmProperties().algorithmProperty()) {
            result.setProperty(IdentifierValue.getQuotedContent(each.key.getText()), IdentifierValue.getQuotedContent(each.value.getText()));
        }
        return result;
    }
    
    private String getIdentifierValue(final ParseTree context) {
        if (null == context) {
            return null;
        }
        return new IdentifierValue(context.getText()).getValue();
    }
    
    @Override
    public ASTNode visitResourceDefinition(final MigrationDistSQLStatementParser.ResourceDefinitionContext ctx) {
        String user = getIdentifierValue(ctx.user());
        String password = null == ctx.password() ? "" : getPassword(ctx.password());
        Properties props = getProperties(ctx.propertiesDefinition());
        DataSourceSegment result = null;
        if (null != ctx.urlSource()) {
            result = new URLBasedDataSourceSegment(getIdentifierValue(ctx.resourceName()), getIdentifierValue(ctx.urlSource().url()), user, password, props);
        }
        if (null != ctx.simpleSource()) {
            result = new HostnameAndPortBasedDataSourceSegment(getIdentifierValue(ctx.resourceName()), getIdentifierValue(ctx.simpleSource().hostname()), ctx.simpleSource().port().getText(),
                    getIdentifierValue(ctx.simpleSource().dbName()), user, password, props);
        }
        return result;
    }
    
    private String getPassword(final PasswordContext ctx) {
        return getIdentifierValue(ctx);
    }
    
    private Properties getProperties(final PropertiesDefinitionContext ctx) {
        Properties result = new Properties();
        if (null == ctx || null == ctx.properties()) {
            return result;
        }
        for (PropertyContext each : ctx.properties().property()) {
            result.setProperty(IdentifierValue.getQuotedContent(each.key.getText()), IdentifierValue.getQuotedContent(each.value.getText()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitAddMigrationSourceResource(final AddMigrationSourceResourceContext ctx) {
        Collection<DataSourceSegment> dataSources = new ArrayList<>();
        for (ResourceDefinitionContext each : ctx.resourceDefinition()) {
            dataSources.add((DataSourceSegment) visit(each));
        }
        return new AddMigrationSourceResourceStatement(dataSources);
    }
    
    @Override
    public ASTNode visitDropMigrationSourceResource(final DropMigrationSourceResourceContext ctx) {
        return new DropMigrationSourceResourceStatement(ctx.resourceName().stream().map(ParseTree::getText).map(each -> new IdentifierValue(each).getValue()).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitShowMigrationSourceResources(final ShowMigrationSourceResourcesContext ctx) {
        return new ShowMigrationSourceResourcesStatement();
    }
}
