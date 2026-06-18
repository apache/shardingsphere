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

package org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.parser.core;

import com.google.common.base.Splitter;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.data.pipeline.distsql.statement.updatable.AlterTransmissionRuleStatement;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.segment.MigrationSourceTargetSegment;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.queryable.ShowMigrationCheckStatusStatement;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.queryable.ShowMigrationListStatement;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.queryable.ShowMigrationRuleStatement;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.queryable.ShowMigrationSourceStorageUnitsStatement;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.queryable.ShowMigrationStatusStatement;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.updatable.CheckMigrationStatement;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.updatable.CommitMigrationStatement;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.updatable.DropMigrationCheckStatement;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.updatable.MigrateTableStatement;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.updatable.RegisterMigrationSourceStorageUnitStatement;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.updatable.RollbackMigrationStatement;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.updatable.StartMigrationCheckStatement;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.updatable.StartMigrationStatement;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.updatable.StopMigrationCheckStatement;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.updatable.StopMigrationStatement;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.updatable.UnregisterMigrationSourceStorageUnitStatement;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.AlgorithmDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.AlterMigrationRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.BatchSizeContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.CheckMigrationContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.CommitMigrationContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.DropMigrationCheckContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.MigrateTableContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.PasswordContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.PropertiesDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.PropertyContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.RateLimiterContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.ReadDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.RegisterMigrationSourceStorageUnitContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.RollbackMigrationContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.ShardingSizeContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.ShowMigrationCheckAlgorithmsContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.ShowMigrationCheckStatusContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.ShowMigrationListContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.ShowMigrationRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.ShowMigrationSourceStorageUnitsContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.ShowMigrationStatusContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.SourceTableNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.StartMigrationCheckContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.StartMigrationContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.StopMigrationCheckContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.StopMigrationContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.StorageUnitDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.StreamChannelContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.TargetTableNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.TransmissionRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.UnregisterMigrationSourceStorageUnitContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.WorkerThreadContext;
import org.apache.shardingsphere.distsql.parser.autogen.MigrationDistSQLStatementParser.WriteDefinitionContext;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.distsql.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.segment.HostnameAndPortBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.segment.ReadOrWriteSegment;
import org.apache.shardingsphere.distsql.segment.TransmissionRuleSegment;
import org.apache.shardingsphere.distsql.segment.URLBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.show.ShowPluginsStatement;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * SQL statement visitor for migration DistSQL.
 */
public final class MigrationDistSQLStatementVisitor extends MigrationDistSQLStatementBaseVisitor<ASTNode> implements SQLVisitor<ASTNode> {
    
    @Override
    public ASTNode visitShowMigrationRule(final ShowMigrationRuleContext ctx) {
        return new ShowMigrationRuleStatement();
    }
    
    @Override
    public ASTNode visitAlterMigrationRule(final AlterMigrationRuleContext ctx) {
        return new AlterTransmissionRuleStatement("MIGRATION", (TransmissionRuleSegment) visit(ctx.transmissionRule()));
    }
    
    @Override
    public ASTNode visitTransmissionRule(final TransmissionRuleContext ctx) {
        TransmissionRuleSegment result = new TransmissionRuleSegment();
        if (null != ctx.readDefinition()) {
            result.setReadSegment((ReadOrWriteSegment) visit(ctx.readDefinition()));
        }
        if (null != ctx.writeDefinition()) {
            result.setWriteSegment((ReadOrWriteSegment) visit(ctx.writeDefinition()));
        }
        if (null != ctx.streamChannel()) {
            result.setStreamChannel((AlgorithmSegment) visit(ctx.streamChannel()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitReadDefinition(final ReadDefinitionContext ctx) {
        return new ReadOrWriteSegment(getWorkerThread(ctx.workerThread()), getBatchSize(ctx.batchSize()), getShardingSize(ctx.shardingSize()), getAlgorithmSegment(ctx.rateLimiter()));
    }
    
    @Override
    public ASTNode visitWriteDefinition(final WriteDefinitionContext ctx) {
        return new ReadOrWriteSegment(getWorkerThread(ctx.workerThread()), getBatchSize(ctx.batchSize()), getAlgorithmSegment(ctx.rateLimiter()));
    }
    
    private AlgorithmSegment getAlgorithmSegment(final RateLimiterContext ctx) {
        return null == ctx ? null : (AlgorithmSegment) visit(ctx);
    }
    
    @Override
    public ASTNode visitRateLimiter(final RateLimiterContext ctx) {
        return visit(ctx.algorithmDefinition());
    }
    
    @Override
    public ASTNode visitStreamChannel(final StreamChannelContext ctx) {
        return visit(ctx.algorithmDefinition());
    }
    
    private Integer getWorkerThread(final WorkerThreadContext ctx) {
        return null == ctx ? null : Integer.parseInt(ctx.intValue().getText());
    }
    
    private Integer getBatchSize(final BatchSizeContext ctx) {
        return null == ctx ? null : Integer.parseInt(ctx.intValue().getText());
    }
    
    private Integer getShardingSize(final ShardingSizeContext ctx) {
        return null == ctx ? null : Integer.parseInt(ctx.intValue().getText());
    }
    
    @Override
    public ASTNode visitMigrateTable(final MigrateTableContext ctx) {
        String targetDatabaseName = getTargetDatabaseName(ctx.targetTableName());
        MigrationSourceTargetSegment migrationSourceTargetSegment = getMigrationSourceTargetSegment(ctx.sourceTableName(), ctx.targetTableName());
        return new MigrateTableStatement(targetDatabaseName, Collections.singleton(migrationSourceTargetSegment));
    }
    
    private String getTargetDatabaseName(final TargetTableNameContext targetContext) {
        List<String> targetDatabaseNames = Splitter.on('.').splitToList(getRequiredIdentifierValue(targetContext));
        return targetDatabaseNames.size() > 1 ? targetDatabaseNames.get(0) : null;
    }
    
    private MigrationSourceTargetSegment getMigrationSourceTargetSegment(final SourceTableNameContext sourceContext, final TargetTableNameContext targetContext) {
        List<String> source = Splitter.on('.').splitToList(getRequiredIdentifierValue(sourceContext));
        List<String> target = Splitter.on('.').splitToList(getRequiredIdentifierValue(targetContext));
        String sourceDatabaseName = source.get(0);
        String sourceSchemaName = 3 == source.size() ? source.get(1) : null;
        String sourceTableName = source.get(source.size() - 1);
        String targetTableName = target.get(target.size() - 1);
        return new MigrationSourceTargetSegment(sourceDatabaseName, sourceSchemaName, sourceTableName, targetTableName);
    }
    
    private String getRequiredIdentifierValue(final ParseTree context) {
        return new IdentifierValue(context.getText()).getValue();
    }
    
    @Override
    public ASTNode visitShowMigrationList(final ShowMigrationListContext ctx) {
        return new ShowMigrationListStatement();
    }
    
    @Override
    public ASTNode visitShowMigrationStatus(final ShowMigrationStatusContext ctx) {
        return new ShowMigrationStatusStatement(getRequiredIdentifierValue(ctx.jobId()));
    }
    
    @Override
    public ASTNode visitStartMigration(final StartMigrationContext ctx) {
        return new StartMigrationStatement(getRequiredIdentifierValue(ctx.jobId()));
    }
    
    @Override
    public ASTNode visitStopMigration(final StopMigrationContext ctx) {
        return new StopMigrationStatement(getRequiredIdentifierValue(ctx.jobId()));
    }
    
    @Override
    public ASTNode visitRollbackMigration(final RollbackMigrationContext ctx) {
        return new RollbackMigrationStatement(getRequiredIdentifierValue(ctx.jobId()));
    }
    
    @Override
    public ASTNode visitCommitMigration(final CommitMigrationContext ctx) {
        return new CommitMigrationStatement(getRequiredIdentifierValue(ctx.jobId()));
    }
    
    @Override
    public ASTNode visitCheckMigration(final CheckMigrationContext ctx) {
        return new CheckMigrationStatement(getRequiredIdentifierValue(ctx.jobId()), null == ctx.algorithmDefinition() ? null : (AlgorithmSegment) visit(ctx.algorithmDefinition()));
    }
    
    @Override
    public ASTNode visitShowMigrationCheckAlgorithms(final ShowMigrationCheckAlgorithmsContext ctx) {
        return new ShowPluginsStatement("MIGRATION_CHECK");
    }
    
    @Override
    public ASTNode visitAlgorithmDefinition(final AlgorithmDefinitionContext ctx) {
        return new AlgorithmSegment(getIdentifierValue(ctx.algorithmTypeName()), getProperties(ctx.propertiesDefinition()));
    }
    
    private String getIdentifierValue(final ParseTree context) {
        return new IdentifierValue(context.getText()).getValue();
    }
    
    @Override
    public ASTNode visitStorageUnitDefinition(final MigrationDistSQLStatementParser.StorageUnitDefinitionContext ctx) {
        String user = getIdentifierValue(ctx.user());
        String password = null == ctx.password() ? "" : getPassword(ctx.password());
        Properties props = getProperties(ctx.propertiesDefinition());
        return null == ctx.urlSource()
                ? new HostnameAndPortBasedDataSourceSegment(getIdentifierValue(ctx.storageUnitName()),
                        getIdentifierValue(ctx.simpleSource().hostname()), ctx.simpleSource().port().getText(), getIdentifierValue(ctx.simpleSource().dbName()), user, password, props)
                : new URLBasedDataSourceSegment(getIdentifierValue(ctx.storageUnitName()), getIdentifierValue(ctx.urlSource().url()), user, password, props);
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
            result.setProperty(QuoteCharacter.unwrapAndTrimText(each.key.getText()), QuoteCharacter.unwrapAndTrimText(each.value.getText()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitRegisterMigrationSourceStorageUnit(final RegisterMigrationSourceStorageUnitContext ctx) {
        Collection<DataSourceSegment> dataSources = new LinkedList<>();
        for (StorageUnitDefinitionContext each : ctx.storageUnitDefinition()) {
            dataSources.add((DataSourceSegment) visit(each));
        }
        return new RegisterMigrationSourceStorageUnitStatement(dataSources);
    }
    
    @Override
    public ASTNode visitUnregisterMigrationSourceStorageUnit(final UnregisterMigrationSourceStorageUnitContext ctx) {
        return new UnregisterMigrationSourceStorageUnitStatement(ctx.storageUnitName().stream().map(ParseTree::getText).map(each -> new IdentifierValue(each).getValue()).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitShowMigrationSourceStorageUnits(final ShowMigrationSourceStorageUnitsContext ctx) {
        return new ShowMigrationSourceStorageUnitsStatement();
    }
    
    @Override
    public ASTNode visitShowMigrationCheckStatus(final ShowMigrationCheckStatusContext ctx) {
        return new ShowMigrationCheckStatusStatement(getRequiredIdentifierValue(ctx.jobId()));
    }
    
    @Override
    public ASTNode visitStartMigrationCheck(final StartMigrationCheckContext ctx) {
        return new StartMigrationCheckStatement(getRequiredIdentifierValue(ctx.jobId()));
    }
    
    @Override
    public ASTNode visitStopMigrationCheck(final StopMigrationCheckContext ctx) {
        return new StopMigrationCheckStatement(getRequiredIdentifierValue(ctx.jobId()));
    }
    
    @Override
    public ASTNode visitDropMigrationCheck(final DropMigrationCheckContext ctx) {
        return new DropMigrationCheckStatement(getRequiredIdentifierValue(ctx.jobId()));
    }
}
