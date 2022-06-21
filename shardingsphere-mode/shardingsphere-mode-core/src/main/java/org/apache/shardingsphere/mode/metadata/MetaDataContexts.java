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

package org.apache.shardingsphere.mode.metadata;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContext;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContextFactory;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.infra.rule.identifier.type.ResourceHeldRule;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

/**
 * Meta data contexts.
 */
@RequiredArgsConstructor
@Getter
public final class MetaDataContexts implements AutoCloseable {
    
    private final MetaDataPersistService persistService;
    
    private final ShardingSphereMetaData metaData;
    
    private final OptimizerContext optimizerContext;
    
    public MetaDataContexts(final MetaDataPersistService persistService) {
        this(persistService, new ShardingSphereMetaData(),
                OptimizerContextFactory.create(new HashMap<>(), new ShardingSphereRuleMetaData(GlobalRulesBuilder.buildRules(Collections.emptyList(), Collections.emptyMap()))));
    }
    
    /**
     * Get persist service.
     *
     * @return persist service
     */
    public Optional<MetaDataPersistService> getPersistService() {
        return Optional.ofNullable(persistService);
    }
    
    @Override
    public void close() throws Exception {
        if (null != persistService) {
            persistService.getRepository().close();
        }
        metaData.getGlobalRuleMetaData().findRules(ResourceHeldRule.class).forEach(ResourceHeldRule::closeStaleResources);
        metaData.getDatabases().values().forEach(each -> each.getRuleMetaData().findRules(ResourceHeldRule.class).forEach(ResourceHeldRule::closeStaleResources));
    }
}
