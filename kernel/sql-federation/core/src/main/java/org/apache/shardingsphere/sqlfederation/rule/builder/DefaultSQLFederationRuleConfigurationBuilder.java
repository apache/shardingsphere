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

package org.apache.shardingsphere.sqlfederation.rule.builder;

import org.apache.shardingsphere.infra.rule.builder.global.DefaultGlobalRuleConfigurationBuilder;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sqlfederation.api.config.SQLFederationRuleConfiguration;
import org.apache.shardingsphere.sqlfederation.constant.SQLFederationOrder;

/**
 * Default sql federation rule configuration builder.
 */
public final class DefaultSQLFederationRuleConfigurationBuilder implements DefaultGlobalRuleConfigurationBuilder<SQLFederationRuleConfiguration, SQLFederationRuleBuilder> {
    
    public static final CacheOption DEFAULT_EXECUTION_PLAN_CACHE_OPTION = new CacheOption(2000, 65535L);
    
    @Override
    public SQLFederationRuleConfiguration build() {
        return new SQLFederationRuleConfiguration(false, false, DEFAULT_EXECUTION_PLAN_CACHE_OPTION);
    }
    
    @Override
    public int getOrder() {
        return SQLFederationOrder.ORDER;
    }
    
    @Override
    public Class<SQLFederationRuleBuilder> getTypeClass() {
        return SQLFederationRuleBuilder.class;
    }
}
