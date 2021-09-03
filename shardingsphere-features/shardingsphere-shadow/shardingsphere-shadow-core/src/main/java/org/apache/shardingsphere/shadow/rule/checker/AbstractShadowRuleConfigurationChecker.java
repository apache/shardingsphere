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

package org.apache.shardingsphere.shadow.rule.checker;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.rule.checker.RuleConfigurationChecker;
import org.apache.shardingsphere.shadow.algorithm.config.AlgorithmProvidedShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;

/**
 * Abstract shadow rule configuration checker.
 *
 * @param <T> rule configuration
 */
public abstract class AbstractShadowRuleConfigurationChecker<T extends RuleConfiguration> implements RuleConfigurationChecker<T> {
    
    protected final void checkShadowRule(final String schemaName, final ShadowRuleConfiguration shadowRuleConfiguration) {
        boolean isShadow = !shadowRuleConfiguration.getColumn().isEmpty() && null != shadowRuleConfiguration.getSourceDataSourceNames() && null != shadowRuleConfiguration.getShadowDataSourceNames();
        Preconditions.checkState(isShadow, "No available shadow rule configuration in `%s` for governance.", schemaName);
    }
    
    protected final void checkShadowRule(final String schemaName, final AlgorithmProvidedShadowRuleConfiguration shadowRuleConfiguration) {
        boolean isShadow = !shadowRuleConfiguration.getColumn().isEmpty() && null != shadowRuleConfiguration.getSourceDataSourceNames() && null != shadowRuleConfiguration.getShadowDataSourceNames();
        Preconditions.checkState(isShadow, "No available shadow rule configuration in `%s` for governance.", schemaName);
    }
}
