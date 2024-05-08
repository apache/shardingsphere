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

package org.apache.shardingsphere.mode.event;

import org.apache.shardingsphere.infra.rule.event.rule.RuleItemChangedEvent;
import org.apache.shardingsphere.infra.rule.event.rule.alter.AlterNamedRuleItemEvent;
import org.apache.shardingsphere.infra.rule.event.rule.drop.DropNamedRuleItemEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;

/**
 * Named rule item changed event creator.
 */
public final class NamedRuleItemChangedEventCreator {
    
    /**
     * Create named rule item changed event.
     * 
     * @param databaseName database name
     * @param itemName item name
     * @param event data changed event
     * @param type rule item type
     * @return named rule item changed event
     */
    public RuleItemChangedEvent create(final String databaseName, final String itemName, final DataChangedEvent event, final String type) {
        return Type.ADDED == event.getType() || Type.UPDATED == event.getType()
                ? new AlterNamedRuleItemEvent(databaseName, itemName, event.getKey(), event.getValue(), type)
                : new DropNamedRuleItemEvent(databaseName, itemName, type);
    }
}
