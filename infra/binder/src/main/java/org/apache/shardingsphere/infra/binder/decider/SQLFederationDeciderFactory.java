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

package org.apache.shardingsphere.infra.binder.decider;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.util.spi.type.ordered.OrderedSPIRegistry;

import java.util.Collection;
import java.util.Map;

/**
 * SQL federation decider factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLFederationDeciderFactory {
    
    static {
        ShardingSphereServiceLoader.register(SQLFederationDecider.class);
    }
    
    /**
     * Get instances of SQL federation decider.
     * 
     * @param rules rules
     * @return got instances
     */
    @SuppressWarnings("rawtypes")
    public static Map<ShardingSphereRule, SQLFederationDecider> getInstances(final Collection<ShardingSphereRule> rules) {
        return OrderedSPIRegistry.getRegisteredServices(SQLFederationDecider.class, rules);
    }
}
