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

package org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.engine.database.type;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.engine.database.DatabaseRuleOperator;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleDropExecutor;
import org.apache.shardingsphere.distsql.statement.type.rdl.rule.aware.StaticDataSourceContainedRuleAwareStatement;
import org.apache.shardingsphere.distsql.statement.type.rdl.rule.database.DatabaseRuleDefinitionStatement;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.checker.DatabaseRuleConfigurationEmptyChecker;
import org.apache.shardingsphere.infra.config.rule.scope.DatabaseRuleConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.attribute.datasource.StaticDataSourceRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.node.rule.tuple.annotation.RuleNodeTupleEntity;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;

import java.util.Objects;

/**
 * Drop database rule operator.
 */
@RequiredArgsConstructor
public final class DropDatabaseRuleOperator implements DatabaseRuleOperator {
    
    private final ContextManager contextManager;
    
    @SuppressWarnings("rawtypes")
    private final DatabaseRuleDropExecutor executor;
    
    @Override
    @SuppressWarnings("unchecked")
    public void operate(final DatabaseRuleDefinitionStatement sqlStatement, final ShardingSphereDatabase database, final RuleConfiguration currentRuleConfig) {
        if (!executor.hasAnyOneToBeDropped(sqlStatement)) {
            return;
        }
        if (sqlStatement instanceof StaticDataSourceContainedRuleAwareStatement) {
            for (StaticDataSourceRuleAttribute each : database.getRuleMetaData().getAttributes(StaticDataSourceRuleAttribute.class)) {
                ((StaticDataSourceContainedRuleAwareStatement) sqlStatement).getNames().forEach(each::cleanStorageNodeDataSource);
            }
            // TODO refactor to new metadata refresh way
        }
        MetaDataManagerPersistService metaDataManagerPersistService = contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService();
        RuleConfiguration toBeDroppedRuleItemConfig = executor.buildToBeDroppedRuleConfiguration(sqlStatement);
        metaDataManagerPersistService.removeRuleConfigurationItem(database, toBeDroppedRuleItemConfig);
        RuleConfiguration toBeAlteredRuleConfig = executor.buildToBeAlteredRuleConfiguration(sqlStatement);
        if (null != toBeAlteredRuleConfig) {
            if (TypedSPILoader.getService(DatabaseRuleConfigurationEmptyChecker.class, toBeAlteredRuleConfig.getClass()).isEmpty((DatabaseRuleConfiguration) toBeAlteredRuleConfig)) {
                YamlRuleConfiguration yamlRuleConfig = new YamlRuleConfigurationSwapperEngine().swapToYamlRuleConfiguration(currentRuleConfig);
                metaDataManagerPersistService.removeRuleConfiguration(database, currentRuleConfig, Objects.requireNonNull(yamlRuleConfig.getClass().getAnnotation(RuleNodeTupleEntity.class)).value());
            } else {
                metaDataManagerPersistService.alterRuleConfiguration(database, toBeAlteredRuleConfig);
            }
        }
    }
}
