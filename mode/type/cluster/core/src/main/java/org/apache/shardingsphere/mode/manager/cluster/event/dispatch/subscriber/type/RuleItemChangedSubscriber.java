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

package org.apache.shardingsphere.mode.manager.cluster.event.dispatch.subscriber.type;

import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mode.event.dispatch.rule.alter.AlterNamedRuleItemEvent;
import org.apache.shardingsphere.mode.event.dispatch.rule.alter.AlterRuleItemEvent;
import org.apache.shardingsphere.mode.event.dispatch.rule.alter.AlterUniqueRuleItemEvent;
import org.apache.shardingsphere.mode.event.dispatch.rule.drop.DropNamedRuleItemEvent;
import org.apache.shardingsphere.mode.event.dispatch.rule.drop.DropRuleItemEvent;
import org.apache.shardingsphere.mode.event.dispatch.rule.drop.DropUniqueRuleItemEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.event.dispatch.subscriber.DispatchEventSubscriber;
import org.apache.shardingsphere.mode.metadata.manager.RuleItemManager;
import org.apache.shardingsphere.mode.spi.item.AlterNamedRuleItem;
import org.apache.shardingsphere.mode.spi.item.AlterRuleItem;
import org.apache.shardingsphere.mode.spi.item.AlterUniqueRuleItem;
import org.apache.shardingsphere.mode.spi.item.DropNamedRuleItem;
import org.apache.shardingsphere.mode.spi.item.DropRuleItem;
import org.apache.shardingsphere.mode.spi.item.DropUniqueRuleItem;

import java.sql.SQLException;

/**
 * Rule item changed subscriber.
 */
@RequiredArgsConstructor
public final class RuleItemChangedSubscriber implements DispatchEventSubscriber {
    
    private final RuleItemManager ruleItemManager;
    
    public RuleItemChangedSubscriber(final ContextManager contextManager) {
        ruleItemManager = contextManager.getMetaDataContextManager().getRuleItemManager();
    }
    
    /**
     * Renew with alter rule item.
     *
     * @param event alter rule item event
     * @throws SQLException SQL Exception
     */
    @Subscribe
    public void renew(final AlterRuleItemEvent event) throws SQLException {
        // TODO remove the event and this subscriber
        ruleItemManager.alterRuleItem(convertToAlterRuleItem(event));
    }
    
    /**
     * Renew with drop rule item.
     *
     * @param event drop rule item event
     * @throws SQLException SQL Exception
     */
    @Subscribe
    public void renew(final DropRuleItemEvent event) throws SQLException {
        // TODO remove the event and this subscriber
        ruleItemManager.dropRuleItem(convertToDropRuleItem(event));
    }
    
    private AlterRuleItem convertToAlterRuleItem(final AlterRuleItemEvent event) {
        if (event instanceof AlterNamedRuleItemEvent) {
            AlterNamedRuleItemEvent alterNamedRuleItemEvent = (AlterNamedRuleItemEvent) event;
            return new AlterNamedRuleItem(alterNamedRuleItemEvent.getDatabaseName(), alterNamedRuleItemEvent.getItemName(), event.getActiveVersionKey(), event.getActiveVersion(), event.getType());
        }
        AlterUniqueRuleItemEvent alterUniqueRuleItemEvent = (AlterUniqueRuleItemEvent) event;
        return new AlterUniqueRuleItem(alterUniqueRuleItemEvent.getDatabaseName(), alterUniqueRuleItemEvent.getActiveVersionKey(), event.getActiveVersion(), event.getType());
    }
    
    private DropRuleItem convertToDropRuleItem(final DropRuleItemEvent event) {
        if (event instanceof DropNamedRuleItemEvent) {
            DropNamedRuleItemEvent dropNamedRuleItemEvent = (DropNamedRuleItemEvent) event;
            return new DropNamedRuleItem(dropNamedRuleItemEvent.getDatabaseName(), dropNamedRuleItemEvent.getItemName(), event.getType());
        }
        DropUniqueRuleItemEvent dropUniqueRuleItemEvent = (DropUniqueRuleItemEvent) event;
        return new DropUniqueRuleItem(dropUniqueRuleItemEvent.getDatabaseName(), event.getType());
    }
}
