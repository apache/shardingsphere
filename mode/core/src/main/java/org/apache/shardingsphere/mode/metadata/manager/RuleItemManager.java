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
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.apache.shardingsphere.mode.spi.RuleItemConfigurationChangedProcessor;
import org.apache.shardingsphere.mode.spi.item.AlterRuleItem;
import org.apache.shardingsphere.mode.spi.item.DropRuleItem;
import org.apache.shardingsphere.mode.spi.item.RuleItemChanged;

import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Rule item manager.
 */
@Slf4j
public class RuleItemManager {
    
    private final AtomicReference<MetaDataContexts> metaDataContexts;
    
    private final DatabaseRuleConfigurationManager ruleConfigManager;
    
    private final MetaDataPersistService metaDataPersistService;
    
    private final RuleItemChangedBuilder ruleItemChangedBuilder;
    
    public RuleItemManager(final AtomicReference<MetaDataContexts> metaDataContexts, final PersistRepository repository, final DatabaseRuleConfigurationManager ruleConfigManager) {
        this.metaDataContexts = metaDataContexts;
        this.ruleConfigManager = ruleConfigManager;
        metaDataPersistService = new MetaDataPersistService(repository);
        ruleItemChangedBuilder = new RuleItemChangedBuilder();
    }
    
    /**
     * Alter with rule item.
     *
     * @param databaseName database name
     * @param activeVersionKey active version key
     * @param activeVersion active version
     * @param changeType change type
     */
    public void alterRuleItem(final String databaseName, final String activeVersionKey, final String activeVersion, final Type changeType) {
        Optional<RuleItemChanged> ruleItemChanged = ruleItemChangedBuilder.build(databaseName, activeVersionKey, activeVersion, changeType);
        if (!ruleItemChanged.isPresent()) {
            return;
        }
        RuleItemConfigurationChangedProcessor processor = TypedSPILoader.getService(RuleItemConfigurationChangedProcessor.class, ruleItemChanged.get().getType());
        if (ruleItemChanged.get() instanceof AlterRuleItem) {
            alter(databaseName, activeVersionKey, activeVersion, (AlterRuleItem) ruleItemChanged.get(), processor);
        } else if (ruleItemChanged.get() instanceof DropRuleItem) {
            drop(databaseName, (DropRuleItem) ruleItemChanged.get(), processor);
        }
    }
    
    private void alter(final String databaseName, final String activeVersionKey, final String activeVersion, final AlterRuleItem alterRuleItem, final RuleItemConfigurationChangedProcessor processor) {
        Preconditions.checkArgument(activeVersion.equals(metaDataPersistService.getMetaDataVersionPersistService()
                .getActiveVersionByFullPath(activeVersionKey)), "Invalid active version: {} of key: {}", activeVersion, activeVersionKey);
        String yamlContent = metaDataPersistService.getMetaDataVersionPersistService().getVersionPathByActiveVersion(activeVersionKey, activeVersion);
        RuleConfiguration currentRuleConfig = processor.findRuleConfiguration(metaDataContexts.get().getMetaData().getDatabase(databaseName));
        synchronized (this) {
            processor.changeRuleItemConfiguration(alterRuleItem, currentRuleConfig, processor.swapRuleItemConfiguration(alterRuleItem, yamlContent));
            try {
                ruleConfigManager.alterRuleConfiguration(databaseName, currentRuleConfig);
            } catch (final SQLException ex) {
                log.error("Alter rule configuration failed, databaseName:{}, key:{}, version:{}", databaseName, activeVersionKey, activeVersion, ex);
            }
        }
    }
    
    private void drop(final String databaseName, final DropRuleItem dropRuleItem, final RuleItemConfigurationChangedProcessor processor) {
        Preconditions.checkState(metaDataContexts.get().getMetaData().containsDatabase(databaseName), "No database '%s' exists.", databaseName);
        RuleConfiguration currentRuleConfig = processor.findRuleConfiguration(metaDataContexts.get().getMetaData().getDatabase(databaseName));
        synchronized (this) {
            processor.dropRuleItemConfiguration(dropRuleItem, currentRuleConfig);
            try {
                ruleConfigManager.dropRuleConfiguration(databaseName, currentRuleConfig);
            } catch (final SQLException ex) {
                log.error("Drop rule configuration failed, databaseName:{}, type:{}", databaseName, dropRuleItem.getType(), ex);
            }
        }
    }
}
