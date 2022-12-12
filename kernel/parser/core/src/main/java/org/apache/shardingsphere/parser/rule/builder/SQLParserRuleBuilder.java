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

package org.apache.shardingsphere.parser.rule.builder;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRuleBuilder;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.constant.SQLParserOrder;
import org.apache.shardingsphere.parser.rule.SQLParserRule;

import java.util.Map;

/**
 * SQL parser rule builder.
 */
public final class SQLParserRuleBuilder implements GlobalRuleBuilder<SQLParserRuleConfiguration> {
    
    @Override
    public SQLParserRule build(final SQLParserRuleConfiguration ruleConfig, final Map<String, ShardingSphereDatabase> databases, final InstanceContext instanceContext,
                               final ConfigurationProperties props) {
        return new SQLParserRule(ruleConfig);
    }
    
    @Override
    public int getOrder() {
        return SQLParserOrder.ORDER;
    }
    
    @Override
    public Class<SQLParserRuleConfiguration> getTypeClass() {
        return SQLParserRuleConfiguration.class;
    }
}
