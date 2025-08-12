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

package org.apache.shardingsphere.shadow.yaml;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.test.it.yaml.YamlRuleConfigurationIT;

import java.util.Arrays;
import java.util.Collections;

class ShadowRuleConfigurationYamlIT extends YamlRuleConfigurationIT {
    
    ShadowRuleConfigurationYamlIT() {
        super("yaml/shadow-rule.yaml", getExpectedRuleConfiguration());
    }
    
    private static ShadowRuleConfiguration getExpectedRuleConfiguration() {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.getDataSources().add(new ShadowDataSourceConfiguration("shadowDataSource", "ds", "ds_shadow"));
        result.getTables().put("t_order", new ShadowTableConfiguration(
                Collections.singletonList("shadowDataSource"), Arrays.asList("user-id-insert-match-algorithm", "user-id-select-match-algorithm")));
        result.getTables().put("t_order_item", new ShadowTableConfiguration(
                Collections.singletonList("shadowDataSource"), Arrays.asList("user-id-insert-match-algorithm", "user-id-update-match-algorithm", "user-id-select-match-algorithm")));
        result.getTables().put("t_address", new ShadowTableConfiguration(
                Collections.singletonList("shadowDataSource"), Arrays.asList("user-id-insert-match-algorithm", "user-id-select-match-algorithm", "sql-hint-algorithm")));
        result.getShadowAlgorithms().put("user-id-insert-match-algorithm", new AlgorithmConfiguration("REGEX_MATCH",
                PropertiesBuilder.build(new Property("regex", "[1]"), new Property("column", "user_id"), new Property("operation", "insert"))));
        result.getShadowAlgorithms().put("user-id-update-match-algorithm", new AlgorithmConfiguration("REGEX_MATCH",
                PropertiesBuilder.build(new Property("regex", "[1]"), new Property("column", "user_id"), new Property("operation", "update"))));
        result.getShadowAlgorithms().put("user-id-select-match-algorithm", new AlgorithmConfiguration("REGEX_MATCH",
                PropertiesBuilder.build(new Property("regex", "[1]"), new Property("column", "user_id"), new Property("operation", "select"))));
        result.getShadowAlgorithms().put("sql-hint-algorithm", new AlgorithmConfiguration("SQL_HINT", PropertiesBuilder.build(new Property("shadow", true), new Property("foo", "bar"))));
        result.setDefaultShadowAlgorithmName("sql-hint-algorithm");
        return result;
    }
}
