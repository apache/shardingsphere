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
import org.apache.shardingsphere.infra.config.rule.checker.DatabaseRuleConfigurationEmptyChecker;
import org.apache.shardingsphere.infra.config.rule.scope.DatabaseRuleConfiguration;
import org.apache.shardingsphere.infra.exception.external.sql.type.wrapper.SQLWrapperException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;
import org.apache.shardingsphere.mode.node.path.type.database.metadata.rule.DatabaseRuleNodePath;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePath;
import org.apache.shardingsphere.mode.spi.rule.RuleChangedItemType;
import org.apache.shardingsphere.mode.spi.rule.RuleItemConfigurationChangedProcessor;

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
     * @param databaseRuleNodePath database rule node path
     * @throws SQLWrapperException SQL wrapper exception
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void alter(final DatabaseRuleNodePath databaseRuleNodePath) {
        RuleItemConfigurationChangedProcessor processor = TypedSPILoader.getService(RuleItemConfigurationChangedProcessor.class,
                new RuleChangedItemType(databaseRuleNodePath.getRuleType(), databaseRuleNodePath.getDatabaseRuleItem().getType()));
        String yamlContent = metaDataPersistFacade.getVersionService().loadContent(new VersionNodePath(databaseRuleNodePath));
        String databaseName = databaseRuleNodePath.getDatabase().getDatabaseName();
        RuleConfiguration currentRuleConfig = processor.findRuleConfiguration(metaDataContexts.getMetaData().getDatabase(databaseName));
        String itemName = databaseRuleNodePath.getDatabaseRuleItem().getName();
        synchronized (this) {
            processor.changeRuleItemConfiguration(itemName, currentRuleConfig, processor.swapRuleItemConfiguration(itemName, yamlContent));
            try {
                databaseRuleConfigManager.refresh(databaseName, currentRuleConfig, true);
            } catch (final SQLException ex) {
                throw new SQLWrapperException(ex);
            }
        }
    }
    
    /**
     * Drop rule item.
     *
     * @param databaseRuleNodePath database rule node path
     * @throws SQLWrapperException SQL wrapper exception
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void drop(final DatabaseRuleNodePath databaseRuleNodePath) {
        String databaseName = databaseRuleNodePath.getDatabase().getDatabaseName();
        Preconditions.checkState(metaDataContexts.getMetaData().containsDatabase(databaseName), "No database '%s' exists.", databaseName);
        RuleItemConfigurationChangedProcessor processor = TypedSPILoader.getService(RuleItemConfigurationChangedProcessor.class,
                new RuleChangedItemType(databaseRuleNodePath.getRuleType(), null == databaseRuleNodePath.getDatabaseRuleItem() ? null : databaseRuleNodePath.getDatabaseRuleItem().getType()));
        RuleConfiguration currentRuleConfig = processor.findRuleConfiguration(metaDataContexts.getMetaData().getDatabase(databaseName));
        String itemName = null == databaseRuleNodePath.getDatabaseRuleItem() ? null : databaseRuleNodePath.getDatabaseRuleItem().getName();
        synchronized (this) {
            processor.dropRuleItemConfiguration(itemName, currentRuleConfig);
            try {
                databaseRuleConfigManager.refresh(databaseName, currentRuleConfig,
                        !TypedSPILoader.getService(DatabaseRuleConfigurationEmptyChecker.class, currentRuleConfig.getClass()).isEmpty((DatabaseRuleConfiguration) currentRuleConfig));
            } catch (final SQLException ex) {
                throw new SQLWrapperException(ex);
            }
        }
    }
}
