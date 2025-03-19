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

package org.apache.shardingsphere.readwritesplitting.checker;

import org.apache.shardingsphere.infra.config.rule.checker.DatabaseRuleConfigurationEmptyChecker;
import org.apache.shardingsphere.readwritesplitting.config.ReadwriteSplittingRuleConfiguration;

/**
 * Readwrite-splitting rule configuration empty checker.
 */
public final class ReadwriteSplittingRuleConfigurationEmptyChecker implements DatabaseRuleConfigurationEmptyChecker<ReadwriteSplittingRuleConfiguration> {
    
    @Override
    public boolean isEmpty(final ReadwriteSplittingRuleConfiguration ruleConfig) {
        return ruleConfig.getDataSourceGroups().isEmpty();
    }
    
    @Override
    public Class<ReadwriteSplittingRuleConfiguration> getType() {
        return ReadwriteSplittingRuleConfiguration.class;
    }
}
