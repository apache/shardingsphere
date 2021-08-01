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

package org.apache.shardingsphere.infra.config.persist.repository;

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.rule.persist.DistMetaDataPersistRuleConfiguration;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;

import java.util.Collection;
import java.util.Properties;

/**
 * Dist meta data persist repository factory.
 */
public final class DistMetaDataPersistRepositoryFactory {
    
    static {
        ShardingSphereServiceLoader.register(DistMetaDataPersistRepository.class);
    }
    
    /**
     * Create new instance of dist meta data persist repository.
     *
     * @param configurations collection of rule configuration
     * @return new instance of dist meta data persist repository
     */
    public static DistMetaDataPersistRepository newInstance(final Collection<RuleConfiguration> configurations) {
        DistMetaDataPersistRuleConfiguration ruleConfiguration = configurations.stream().filter(each -> each instanceof DistMetaDataPersistRuleConfiguration)
                .map(each -> (DistMetaDataPersistRuleConfiguration) each).findFirst().orElse(new DistMetaDataPersistRuleConfiguration("Local", new Properties()));
        return TypedSPIRegistry.getRegisteredService(DistMetaDataPersistRepository.class, ruleConfiguration.getType(), ruleConfiguration.getProps());
    }
}
