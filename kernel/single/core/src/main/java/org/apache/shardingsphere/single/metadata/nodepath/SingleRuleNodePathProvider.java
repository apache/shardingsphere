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

package org.apache.shardingsphere.single.metadata.nodepath;

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.mode.node.path.rule.RuleNodePath;
import org.apache.shardingsphere.mode.node.spi.RuleNodePathProvider;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;

import java.util.Arrays;
import java.util.Collections;

/**
 * Single rule node path provider.
 */
public final class SingleRuleNodePathProvider implements RuleNodePathProvider {
    
    public static final String RULE_TYPE = "single";
    
    public static final String TABLES = "tables";
    
    public static final String DEFAULT_DATA_SOURCE = "default_data_source";
    
    private static final RuleNodePath INSTANCE = new RuleNodePath(RULE_TYPE, Collections.emptyList(), Arrays.asList(TABLES, DEFAULT_DATA_SOURCE));
    
    @Override
    public RuleNodePath getRuleNodePath() {
        return INSTANCE;
    }
    
    @Override
    public Class<? extends RuleConfiguration> getType() {
        return SingleRuleConfiguration.class;
    }
}
