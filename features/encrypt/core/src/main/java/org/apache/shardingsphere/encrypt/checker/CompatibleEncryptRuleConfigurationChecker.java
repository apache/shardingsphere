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

package org.apache.shardingsphere.encrypt.checker;

import org.apache.shardingsphere.encrypt.api.config.CompatibleEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.constant.EncryptOrder;
import org.apache.shardingsphere.infra.config.rule.checker.RuleConfigurationChecker;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;

/**
 * Encrypt rule configuration checker.
 * 
 * @deprecated Should use new api, compatible api will remove in next version.
 */
@Deprecated
public final class CompatibleEncryptRuleConfigurationChecker implements RuleConfigurationChecker<CompatibleEncryptRuleConfiguration> {
    
    private final EncryptRuleConfigurationChecker delegate = new EncryptRuleConfigurationChecker();
    
    @Override
    public void check(final String databaseName, final CompatibleEncryptRuleConfiguration config, final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> builtRules) {
        delegate.check(databaseName, config.convertToEncryptRuleConfiguration(), dataSourceMap, builtRules);
    }
    
    @Override
    public int getOrder() {
        return EncryptOrder.COMPATIBLE_ORDER;
    }
    
    @Override
    public Class<CompatibleEncryptRuleConfiguration> getTypeClass() {
        return CompatibleEncryptRuleConfiguration.class;
    }
}
