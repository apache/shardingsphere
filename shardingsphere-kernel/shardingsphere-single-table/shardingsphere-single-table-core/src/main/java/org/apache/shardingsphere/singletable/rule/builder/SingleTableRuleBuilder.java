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

package org.apache.shardingsphere.singletable.rule.builder;

import org.apache.shardingsphere.singletable.config.SingleTableRuleConfiguration;
import org.apache.shardingsphere.singletable.constant.SingleTableOrder;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.schema.SchemaRulesBuilderMaterials;
import org.apache.shardingsphere.infra.rule.builder.schema.SchemaRuleBuilder;
import org.apache.shardingsphere.singletable.rule.SingleTableRule;

import java.util.Collection;

/**
 * Single table rule builder.
 */
public final class SingleTableRuleBuilder implements SchemaRuleBuilder<SingleTableRuleConfiguration> {
    
    @Override
    public SingleTableRule build(final SchemaRulesBuilderMaterials materials, final SingleTableRuleConfiguration config, final Collection<ShardingSphereRule> builtRules) {
        return new SingleTableRule(materials.getDatabaseType(), materials.getDataSourceMap(), builtRules, materials.getProps());
    }
    
    @Override
    public int getOrder() {
        return SingleTableOrder.ORDER;
    }
    
    @Override
    public Class<SingleTableRuleConfiguration> getTypeClass() {
        return SingleTableRuleConfiguration.class;
    }
}
