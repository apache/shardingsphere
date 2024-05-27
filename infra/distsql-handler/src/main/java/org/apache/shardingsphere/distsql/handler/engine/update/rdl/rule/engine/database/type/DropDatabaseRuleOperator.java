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
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.DatabaseRuleDropExecutor;
import org.apache.shardingsphere.distsql.statement.rdl.rule.aware.StaticDataSourceContainedRuleAwareStatement;
import org.apache.shardingsphere.distsql.statement.rdl.rule.database.DatabaseRuleDefinitionStatement;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.scope.DatabaseRuleConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.infra.rule.attribute.datasource.StaticDataSourceRuleAttribute;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.mode.service.persist.MetaDataManagerPersistService;
import org.apache.shardingsphere.mode.tuple.annotation.RepositoryTupleEntity;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Collection;
import java.util.Collections;
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
    public Collection<MetaDataVersion> operate(final DatabaseRuleDefinitionStatement sqlStatement, final ShardingSphereDatabase database, final RuleConfiguration currentRuleConfig) {
        if (!executor.hasAnyOneToBeDropped(sqlStatement)) {
            return Collections.emptyList();
        }
        if (sqlStatement instanceof StaticDataSourceContainedRuleAwareStatement) {
            for (StaticDataSourceRuleAttribute each : database.getRuleMetaData().getAttributes(StaticDataSourceRuleAttribute.class)) {
                ((StaticDataSourceContainedRuleAwareStatement) sqlStatement).getNames().forEach(each::cleanStorageNodeDataSource);
            }
            // TODO refactor to new metadata refresh way
        }
        MetaDataManagerPersistService metaDataManagerPersistService = contextManager.getPersistServiceFacade().getMetaDataManagerPersistService();
        RuleConfiguration toBeDroppedRuleConfig = executor.buildToBeDroppedRuleConfiguration(sqlStatement);
        metaDataManagerPersistService.removeRuleConfigurationItem(database.getName(), toBeDroppedRuleConfig);
        RuleConfiguration toBeAlteredRuleConfig = executor.buildToBeAlteredRuleConfiguration(sqlStatement);
        if (null != toBeAlteredRuleConfig && ((DatabaseRuleConfiguration) toBeAlteredRuleConfig).isEmpty()) {
            YamlRuleConfiguration yamlRuleConfig = new YamlRuleConfigurationSwapperEngine().swapToYamlRuleConfiguration(currentRuleConfig);
            metaDataManagerPersistService.removeRuleConfiguration(database.getName(), Objects.requireNonNull(yamlRuleConfig.getClass().getAnnotation(RepositoryTupleEntity.class)).value());
            return Collections.emptyList();
        }
        return metaDataManagerPersistService.alterRuleConfiguration(database.getName(), toBeAlteredRuleConfig);
    }
}
