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

package org.apache.shardingsphere.infra.rule.builder.database;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.RuleBuilder;
import org.apache.shardingsphere.infra.rule.scope.DatabaseRule;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;

import java.util.Collection;

/**
 * Database rule builder.
 * 
 * @param <T> type of rule configuration
 */
@SingletonSPI
public interface DatabaseRuleBuilder<T extends RuleConfiguration> extends RuleBuilder<T> {
    
    /**
     * Build database rule.
     *
     * @param ruleConfig rule configuration
     * @param databaseName database name
     * @param protocolType protocol type
     * @param resourceMetaData resource meta data
     * @param builtRules built rules
     * @param computeNodeInstanceContext compute node instance context
     * @return built database rule
     */
    DatabaseRule build(T ruleConfig, String databaseName, DatabaseType protocolType,
                       ResourceMetaData resourceMetaData, Collection<ShardingSphereRule> builtRules, ComputeNodeInstanceContext computeNodeInstanceContext);
}
