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

package org.apache.shardingsphere.mode.metadata.manager.rule;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;
import org.apache.shardingsphere.mode.spi.rule.RuleItemConfigurationChangedProcessor;
import org.apache.shardingsphere.mode.spi.rule.item.alter.AlterRuleItem;
import org.apache.shardingsphere.mode.spi.rule.item.drop.DropRuleItem;

import java.sql.SQLException;

/**
 * Database rule item manager.
 */
@RequiredArgsConstructor
public final class DatabaseRuleItemManager {
    
    private final MetaDataContexts metaDataContexts;
    
    private final DatabaseRuleConfigurationManager databaseRuleConfigManager;
    
    private final MetaDataPersistFacade metaDataPersistFacade;
    
    /**
     * Alter rule item.
     *
     * @param alterRuleItem alter rule item
     * @throws SQLException SQL Exception
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void alter(final AlterRuleItem alterRuleItem) throws SQLException {
        Preconditions.checkArgument(String.valueOf(alterRuleItem.getActiveVersion()).equals(metaDataPersistFacade.getRepository().query(alterRuleItem.getActiveVersionKey())),
                "Invalid active version: %s of key: %s", alterRuleItem.getActiveVersion(), alterRuleItem.getActiveVersionKey());
        RuleItemConfigurationChangedProcessor processor = TypedSPILoader.getService(RuleItemConfigurationChangedProcessor.class, alterRuleItem.getType());
        String yamlContent = metaDataPersistFacade.getMetaDataVersionService().loadContent(alterRuleItem.getActiveVersionKey(), alterRuleItem.getActiveVersion());
        String databaseName = alterRuleItem.getDatabaseName();
        RuleConfiguration currentRuleConfig = processor.findRuleConfiguration(metaDataContexts.getMetaData().getDatabase(databaseName));
        synchronized (this) {
            processor.changeRuleItemConfiguration(alterRuleItem, currentRuleConfig, processor.swapRuleItemConfiguration(alterRuleItem, yamlContent));
            databaseRuleConfigManager.alter(databaseName, currentRuleConfig);
        }
    }
    
    /**
     * Drop rule item.
     *
     * @param dropRuleItem drop rule item
     * @throws SQLException SQL Exception
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void drop(final DropRuleItem dropRuleItem) throws SQLException {
        String databaseName = dropRuleItem.getDatabaseName();
        Preconditions.checkState(metaDataContexts.getMetaData().containsDatabase(databaseName), "No database '%s' exists.", databaseName);
        RuleItemConfigurationChangedProcessor processor = TypedSPILoader.getService(RuleItemConfigurationChangedProcessor.class, dropRuleItem.getType());
        RuleConfiguration currentRuleConfig = processor.findRuleConfiguration(metaDataContexts.getMetaData().getDatabase(databaseName));
        synchronized (this) {
            processor.dropRuleItemConfiguration(dropRuleItem, currentRuleConfig);
            databaseRuleConfigManager.drop(databaseName, currentRuleConfig);
        }
    }
}
