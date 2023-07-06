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

package org.apache.shardingsphere.sharding.event.table.binding;

import org.apache.shardingsphere.infra.rule.event.rule.RuleItemChangedEvent;
import org.apache.shardingsphere.infra.rule.event.rule.alter.AlterNamedRuleItemEvent;
import org.apache.shardingsphere.infra.rule.event.rule.drop.DropNamedRuleItemEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.event.NamedRuleItemChangedEventCreator;
import org.apache.shardingsphere.sharding.subscriber.ShardingTableReferenceChangedGenerator;

/**
 * Sharding table reference creator.
 */
public final class ShardingTableReferenceEventCreator implements NamedRuleItemChangedEventCreator {
    
    @Override
    public RuleItemChangedEvent create(final String databaseName, final String tableName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType() || Type.UPDATED == event.getType()) {
            return new AlterNamedRuleItemEvent(databaseName, tableName, event.getKey(), event.getValue(), ShardingTableReferenceChangedGenerator.TYPE);
        }
        return new DropNamedRuleItemEvent(databaseName, tableName, ShardingTableReferenceChangedGenerator.TYPE);
    }
}
