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

package org.apache.shardingsphere.dbdiscovery.rule.checker;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.constant.DatabaseDiscoveryOrder;
import org.apache.shardingsphere.infra.rule.checker.RuleConfigurationChecker;

/**
 * Database discovery rule configuration checker.
 */
public final class DatabaseDiscoveryRuleConfigurationChecker implements RuleConfigurationChecker<DatabaseDiscoveryRuleConfiguration> {
    
    @Override
    public void check(final String schemaName, final DatabaseDiscoveryRuleConfiguration config) {
        config.getDataSources().forEach(each -> Preconditions.checkState(
                !each.getDiscoveryTypeName().isEmpty(), "No available DatabaseDiscovery rule configuration in `%s` for governance.", schemaName));
    }
    
    @Override
    public int getOrder() {
        return DatabaseDiscoveryOrder.ORDER;
    }
    
    @Override
    public Class<DatabaseDiscoveryRuleConfiguration> getTypeClass() {
        return DatabaseDiscoveryRuleConfiguration.class;
    }
}
