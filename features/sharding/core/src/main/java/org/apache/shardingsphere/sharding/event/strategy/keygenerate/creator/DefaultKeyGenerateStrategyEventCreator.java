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

package org.apache.shardingsphere.sharding.event.strategy.keygenerate.creator;

import org.apache.shardingsphere.infra.rule.event.GovernanceEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.event.UniqueRuleItemChangedEventCreator;
import org.apache.shardingsphere.sharding.event.strategy.keygenerate.AddDefaultKeyGenerateStrategyEvent;
import org.apache.shardingsphere.sharding.event.strategy.keygenerate.AlterDefaultKeyGenerateStrategyEvent;
import org.apache.shardingsphere.sharding.event.strategy.keygenerate.DeleteDefaultKeyGenerateStrategyEvent;

/**
 * Default key generate strategy event creator.
 */
public final class DefaultKeyGenerateStrategyEventCreator implements UniqueRuleItemChangedEventCreator {
    
    @Override
    public GovernanceEvent create(final String databaseName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return new AddDefaultKeyGenerateStrategyEvent(databaseName, event.getKey(), event.getValue());
        }
        if (Type.UPDATED == event.getType()) {
            return new AlterDefaultKeyGenerateStrategyEvent(databaseName, event.getKey(), event.getValue());
        }
        return new DeleteDefaultKeyGenerateStrategyEvent(databaseName);
    }
}
