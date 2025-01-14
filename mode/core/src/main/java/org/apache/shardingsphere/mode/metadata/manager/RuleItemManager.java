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

package org.apache.shardingsphere.mode.metadata.manager;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.apache.shardingsphere.mode.spi.rule.RuleItemConfigurationChangedProcessor;
import org.apache.shardingsphere.mode.spi.rule.item.alter.AlterRuleItem;
import org.apache.shardingsphere.mode.spi.rule.item.drop.DropRuleItem;

import java.sql.SQLException;

/**
 * Rule item manager.
 */
public class RuleItemManager {
    
    private final MetaDataContexts metaDataContexts;
    
    private final DatabaseRuleConfigurationManager ruleConfigManager;
    
    private final MetaDataPersistService metaDataPersistService;
    
    public RuleItemManager(final MetaDataContexts metaDataContexts, final PersistRepository repository, final DatabaseRuleConfigurationManager ruleConfigManager) {
        this.metaDataContexts = metaDataContexts;
        this.ruleConfigManager = ruleConfigManager;
        metaDataPersistService = new MetaDataPersistService(repository);
    }
    
    /**
     * Alter with rule item.
     *
     * @param alterRuleItem alter rule item
     * @throws SQLException SQL Exception
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void alterRuleItem(final AlterRuleItem alterRuleItem) throws SQLException {
        Preconditions.checkArgument(alterRuleItem.getActiveVersion().equals(metaDataPersistService.getRepository().query(alterRuleItem.getActiveVersionKey())),
                "Invalid active version: {} of key: {}", alterRuleItem.getActiveVersion(), alterRuleItem.getActiveVersionKey());
        RuleItemConfigurationChangedProcessor processor = TypedSPILoader.getService(RuleItemConfigurationChangedProcessor.class, alterRuleItem.getType());
        String yamlContent = metaDataPersistService.getMetaDataVersionPersistService()
                .getVersionPathByActiveVersion(alterRuleItem.getActiveVersionKey(), alterRuleItem.getActiveVersion());
        String databaseName = alterRuleItem.getDatabaseName();
        RuleConfiguration currentRuleConfig = processor.findRuleConfiguration(metaDataContexts.getMetaData().getDatabase(databaseName));
        synchronized (this) {
            processor.changeRuleItemConfiguration(alterRuleItem, currentRuleConfig, processor.swapRuleItemConfiguration(alterRuleItem, yamlContent));
            ruleConfigManager.alterRuleConfiguration(databaseName, currentRuleConfig);
        }
    }
    
    /**
     * Drop with rule item.
     *
     * @param dropRuleItem drop rule item
     * @throws SQLException SQL Exception
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void dropRuleItem(final DropRuleItem dropRuleItem) throws SQLException {
        String databaseName = dropRuleItem.getDatabaseName();
        Preconditions.checkState(metaDataContexts.getMetaData().containsDatabase(databaseName), "No database '%s' exists.", databaseName);
        RuleItemConfigurationChangedProcessor processor = TypedSPILoader.getService(RuleItemConfigurationChangedProcessor.class, dropRuleItem.getType());
        RuleConfiguration currentRuleConfig = processor.findRuleConfiguration(metaDataContexts.getMetaData().getDatabase(databaseName));
        synchronized (this) {
            processor.dropRuleItemConfiguration(dropRuleItem, currentRuleConfig);
            ruleConfigManager.dropRuleConfiguration(databaseName, currentRuleConfig);
        }
    }
}
