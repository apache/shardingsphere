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

package org.apache.shardingsphere.distsql.handler.engine.update.ral.rule.spi.database;

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.identifier.scope.DatabaseRule;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;

/**
 * Import rule configuration provider.
 */
@SingletonSPI
public interface ImportRuleConfigurationProvider extends TypedSPI {
    
    /**
     * Check rule configuration.
     *
     * @param database database
     * @param ruleConfig rule configuration
     */
    void check(ShardingSphereDatabase database, RuleConfiguration ruleConfig);
    
    /**
     * Build database rule.
     *
     * @param database database
     * @param ruleConfig rule configuration
     * @param instanceContext instance context
     * @return built database rule
     */
    DatabaseRule build(ShardingSphereDatabase database, RuleConfiguration ruleConfig, InstanceContext instanceContext);
    
    @Override
    Class<? extends RuleConfiguration> getType();
}
