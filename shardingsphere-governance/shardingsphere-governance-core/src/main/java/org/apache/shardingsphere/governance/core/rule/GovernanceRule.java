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

package org.apache.shardingsphere.governance.core.rule;

import lombok.Getter;
import org.apache.shardingsphere.governance.core.registry.RegistryCenter;
import org.apache.shardingsphere.governance.core.registry.RegistryCenterRepositoryFactory;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceConfiguration;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.infra.persist.DistMetaDataPersistService;
import org.apache.shardingsphere.infra.persist.rule.PersistRule;
import org.apache.shardingsphere.infra.rule.identifier.level.FeatureRule;
import org.apache.shardingsphere.infra.rule.identifier.scope.GlobalRule;

/**
 * Governance rule.
 */
@Getter
public final class GovernanceRule implements PersistRule, FeatureRule, GlobalRule {
    
    private final DistMetaDataPersistService distMetaDataPersistService;
    
    private final RegistryCenter registryCenter;
    
    public GovernanceRule(final GovernanceConfiguration config) {
        RegistryCenterRepository repository = RegistryCenterRepositoryFactory.newInstance(config.getRegistryCenterConfiguration());
        distMetaDataPersistService = new DistMetaDataPersistService(repository);
        registryCenter = new RegistryCenter(repository);
    }
}
