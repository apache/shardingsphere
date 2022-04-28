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

package org.apache.shardingsphere.infra.rule.builder.schema;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.type.ordered.OrderedSPIRegistry;

import java.util.Collection;
import java.util.Map;

/**
 * Default schema rule configuration builder factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DefaultSchemaRuleConfigurationBuilderFactory {
    
    static {
        ShardingSphereServiceLoader.register(DefaultSchemaRuleConfigurationBuilder.class);
    }
    
    /**
     * Create new instance of schema rule builder.
     *
     * @param builders schema rule builders
     * @return new instance of schema rule builder
     */
    @SuppressWarnings("rawtypes")
    public static Map<SchemaRuleBuilder, DefaultSchemaRuleConfigurationBuilder> newInstance(final Collection<SchemaRuleBuilder> builders) {
        return OrderedSPIRegistry.getRegisteredServices(DefaultSchemaRuleConfigurationBuilder.class, builders);
    }
}
