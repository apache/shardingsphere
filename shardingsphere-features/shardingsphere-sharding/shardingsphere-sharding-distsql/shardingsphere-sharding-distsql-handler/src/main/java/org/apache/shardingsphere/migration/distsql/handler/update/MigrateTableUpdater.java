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

package org.apache.shardingsphere.migration.distsql.handler.update;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.shardingsphere.data.pipeline.api.MigrationJobPublicAPI;
import org.apache.shardingsphere.data.pipeline.api.PipelineJobPublicAPIFactory;
import org.apache.shardingsphere.data.pipeline.api.pojo.CreateMigrationJobParameter;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobCreationException;
import org.apache.shardingsphere.infra.distsql.update.RALUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.migration.distsql.statement.MigrateTableStatement;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.swapper.YamlShardingRuleConfigurationSwapper;

import java.util.Objects;

/**
 * Migrate table updater.
 */
@Slf4j
public final class MigrateTableUpdater implements RALUpdater<MigrateTableStatement> {
    
    private static final MigrationJobPublicAPI JOB_API = PipelineJobPublicAPIFactory.getMigrationJobPublicAPI();
    
    private static final YamlShardingRuleConfigurationSwapper SHARDING_RULE_CONFIG_SWAPPER = new YamlShardingRuleConfigurationSwapper();
    
    @Override
    public void executeUpdate(final String databaseName, final MigrateTableStatement sqlStatement) {
        log.info("start migrate job by {}", sqlStatement);
        ShardingSphereDatabase targetDatabase = PipelineContext.getContextManager().getMetaDataContexts().getMetaData().getDatabase(sqlStatement.getTargetDatabaseName());
        ShardingRuleConfiguration targetShardingRule = targetDatabase.getRuleMetaData().getConfigurations().stream().filter(each -> each instanceof ShardingRuleConfiguration)
                .map(each -> (ShardingRuleConfiguration) each).findFirst().orElseThrow(() -> new PipelineJobCreationException("No sharding rule found."));
        targetShardingRule.getTables().removeIf(each -> !Objects.equals(each.getLogicTable(), sqlStatement.getTargetTableName()));
        targetShardingRule.getAutoTables().removeIf(each -> !Objects.equals(each.getLogicTable(), sqlStatement.getTargetTableName()));
        CreateMigrationJobParameter createMigrationJobParameter = new CreateMigrationJobParameter(sqlStatement.getSourceDatabaseName(), sqlStatement.getSourceTableName(),
                ObjectUtils.defaultIfNull(sqlStatement.getTargetDatabaseName(), sqlStatement.getSourceDatabaseName()), sqlStatement.getTargetTableName(),
                SHARDING_RULE_CONFIG_SWAPPER.swapToYamlConfiguration(targetShardingRule), targetDatabase.getResource().getDataSources());
        JOB_API.createJobAndStart(createMigrationJobParameter);
    }
    
    @Override
    public String getType() {
        return MigrateTableStatement.class.getName();
    }
}
