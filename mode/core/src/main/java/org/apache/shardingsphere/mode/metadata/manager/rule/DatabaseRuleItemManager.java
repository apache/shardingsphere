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
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.scope.DatabaseRuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.scope.DatabaseRuleConfigurationEmptyChecker;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;
import org.apache.shardingsphere.mode.node.path.type.metadata.rule.DatabaseRuleItem;
import org.apache.shardingsphere.mode.node.path.type.metadata.rule.DatabaseRuleNodePath;
import org.apache.shardingsphere.mode.node.path.type.version.VersionNodePath;
import org.apache.shardingsphere.mode.spi.rule.RuleItemConfigurationChangedProcessor;
import org.apache.shardingsphere.mode.spi.rule.item.alter.AlterNamedRuleItem;
import org.apache.shardingsphere.mode.spi.rule.item.alter.AlterRuleItem;
import org.apache.shardingsphere.mode.spi.rule.item.drop.DropRuleItem;

import java.sql.SQLException;

/**
 * Database rule item manager.
 */
@RequiredArgsConstructor
@Slf4j
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
        VersionNodePath versionNodePath = new VersionNodePath(getDatabaseRuleNodePath(alterRuleItem));
        if (!checkActiveVersion(versionNodePath, alterRuleItem.getCurrentVersion())) {
            return;
        }
        RuleItemConfigurationChangedProcessor processor = TypedSPILoader.getService(RuleItemConfigurationChangedProcessor.class, alterRuleItem.getType());
        String yamlContent = metaDataPersistFacade.getMetaDataVersionService().loadContent(versionNodePath);
        String databaseName = alterRuleItem.getDatabaseName();
        RuleConfiguration currentRuleConfig = processor.findRuleConfiguration(metaDataContexts.getMetaData().getDatabase(databaseName));
        synchronized (this) {
            processor.changeRuleItemConfiguration(alterRuleItem, currentRuleConfig, processor.swapRuleItemConfiguration(alterRuleItem, yamlContent));
            databaseRuleConfigManager.refresh(databaseName, currentRuleConfig, true);
        }
    }
    
    private boolean checkActiveVersion(final VersionNodePath versionNodePath, final int currentVersion) {
        if (String.valueOf(currentVersion).equals(metaDataPersistFacade.getRepository().query(versionNodePath.getActiveVersionPath()))) {
            return true;
        }
        log.warn("Invalid active version `{}` of key `{}`", currentVersion, versionNodePath.getActiveVersionPath());
        return false;
    }
    
    private DatabaseRuleNodePath getDatabaseRuleNodePath(final AlterRuleItem alterRuleItem) {
        String ruleType = alterRuleItem.getType().getRuleType();
        String itemType = alterRuleItem.getType().getRuleItemType();
        DatabaseRuleItem databaseRuleItem = alterRuleItem instanceof AlterNamedRuleItem
                ? new DatabaseRuleItem(itemType, ((AlterNamedRuleItem) alterRuleItem).getItemName())
                : new DatabaseRuleItem(itemType);
        return new DatabaseRuleNodePath(alterRuleItem.getDatabaseName(), ruleType, databaseRuleItem);
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
            databaseRuleConfigManager.refresh(databaseName, currentRuleConfig,
                    !TypedSPILoader.getService(DatabaseRuleConfigurationEmptyChecker.class, currentRuleConfig.getClass()).isEmpty((DatabaseRuleConfiguration) currentRuleConfig));
        }
    }
}
