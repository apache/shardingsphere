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

package org.apache.shardingsphere.infra.rule.checker;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPIRegistry;

import java.util.Collections;
import java.util.Map;

/**
 * Rule configuration checker factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RuleConfigurationCheckerFactory {
    
    static {
        ShardingSphereServiceLoader.register(RuleConfigurationChecker.class);
    }
    
    /**
     * Create new instance of rule configuration checker.
     * 
     * @param config rule configuration
     * @return new instance of rule configuration checker
     */
    @SuppressWarnings("rawtypes")
    public static RuleConfigurationChecker newInstance(final RuleConfiguration config) {
        Map<Class<?>, RuleConfigurationChecker> checkers = OrderedSPIRegistry.getRegisteredServicesByClass(RuleConfigurationChecker.class, Collections.singleton(config.getClass()));
        Preconditions.checkArgument(checkers.containsKey(config.getClass()), "Can not find rule configuration checker for rule type: `%s`", config.getClass());
        return checkers.get(config.getClass());
    }
}
