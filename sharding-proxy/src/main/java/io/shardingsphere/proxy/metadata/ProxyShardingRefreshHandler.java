/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.proxy.metadata;

import io.shardingsphere.core.metadata.table.RefreshHandler;
import io.shardingsphere.proxy.config.RuleRegistry;
import lombok.RequiredArgsConstructor;

/**
 * Sharding table meta data refreshing handler for proxy.
 *
 * @author zhaojun
 */
@RequiredArgsConstructor
public final class ProxyShardingRefreshHandler implements RefreshHandler {
    
    private static final RuleRegistry RULE_REGISTRY = RuleRegistry.getInstance();
    
    private final String logicTableName;
    
    @Override
    public void execute() {
        RULE_REGISTRY.getMetaData().getTable().refresh(RULE_REGISTRY.getShardingRule().getTableRule(logicTableName), RULE_REGISTRY.getShardingRule());
    }
}
