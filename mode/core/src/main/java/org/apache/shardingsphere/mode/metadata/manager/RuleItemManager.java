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

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.mode.event.dispatch.rule.alter.AlterRuleItemEvent;
import org.apache.shardingsphere.mode.event.dispatch.rule.drop.DropRuleItemEvent;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.apache.shardingsphere.mode.spi.RuleItemConfigurationChangedProcessor;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Rule item manager.
 */
public class RuleItemManager {
    
    private final AtomicReference<MetaDataContexts> metaDataContexts;
    
    private final DatabaseRuleConfigurationManager ruleConfigurationManager;
    
    private final MetaDataPersistService metaDataPersistService;
    
    public RuleItemManager(final AtomicReference<MetaDataContexts> metaDataContexts, final PersistRepository repository, final DatabaseRuleConfigurationManager ruleConfigurationManager) {
        this.metaDataContexts = metaDataContexts;
        this.ruleConfigurationManager = ruleConfigurationManager;
        metaDataPersistService = new MetaDataPersistService(repository);
    }
    
    /**
     * Alter with rule item.
     *
     * @param event alter rule item event
     * @throws SQLException SQL Exception
     */
    @SuppressWarnings({"rawtypes", "unchecked", "unused"})
    public void alterRuleItem(final AlterRuleItemEvent event) throws SQLException {
        if (!event.getActiveVersion().equals(metaDataPersistService.getMetaDataVersionPersistService()
                .getActiveVersionByFullPath(event.getActiveVersionKey()))) {
            return;
        }
        RuleItemConfigurationChangedProcessor processor = TypedSPILoader.getService(RuleItemConfigurationChangedProcessor.class, event.getType());
        String yamlContent = metaDataPersistService.getMetaDataVersionPersistService()
                .getVersionPathByActiveVersion(event.getActiveVersionKey(), event.getActiveVersion());
        String databaseName = event.getDatabaseName();
        RuleConfiguration currentRuleConfig = processor.findRuleConfiguration(metaDataContexts.get().getMetaData().getDatabase(databaseName));
        synchronized (this) {
            processor.changeRuleItemConfiguration(event, currentRuleConfig, processor.swapRuleItemConfiguration(event, yamlContent));
            ruleConfigurationManager.alterRuleConfiguration(databaseName, currentRuleConfig);
        }
    }
    
    /**
     * Drop with rule item.
     *
     * @param event drop rule item event
     * @throws SQLException SQL Exception
     */
    @SuppressWarnings({"rawtypes", "unchecked", "unused"})
    public void dropRuleItem(final DropRuleItemEvent event) throws SQLException {
        String databaseName = event.getDatabaseName();
        if (!metaDataContexts.get().getMetaData().containsDatabase(databaseName)) {
            return;
        }
        RuleItemConfigurationChangedProcessor processor = TypedSPILoader.getService(RuleItemConfigurationChangedProcessor.class, event.getType());
        RuleConfiguration currentRuleConfig = processor.findRuleConfiguration(metaDataContexts.get().getMetaData().getDatabase(databaseName));
        synchronized (this) {
            processor.dropRuleItemConfiguration(event, currentRuleConfig);
            ruleConfigurationManager.dropRuleConfiguration(databaseName, currentRuleConfig);
        }
    }
}
