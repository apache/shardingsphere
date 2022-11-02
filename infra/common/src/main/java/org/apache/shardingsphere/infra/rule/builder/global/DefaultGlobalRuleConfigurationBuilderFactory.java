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

package org.apache.shardingsphere.infra.rule.builder.global;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.util.spi.type.ordered.OrderedSPIRegistry;

import java.util.Collection;
import java.util.Map;

/**
 * Default global rule configuration builder factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DefaultGlobalRuleConfigurationBuilderFactory {
    
    static {
        ShardingSphereServiceLoader.register(DefaultGlobalRuleConfigurationBuilder.class);
    }
    
    /**
     * Get instance default global rule configuration builder.
     *
     * @param globalRuleBuilders global rule builders
     * @return got instance
     */
    @SuppressWarnings("rawtypes")
    public static Map<GlobalRuleBuilder, DefaultGlobalRuleConfigurationBuilder> getInstance(final Collection<GlobalRuleBuilder> globalRuleBuilders) {
        return OrderedSPIRegistry.getRegisteredServices(DefaultGlobalRuleConfigurationBuilder.class, globalRuleBuilders);
    }
}
