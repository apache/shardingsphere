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

package org.apache.shardingsphere.shadow.checker;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

class ShadowRuleConfigurationCheckerTest {
    
    @Test
    void assertCheck() {
        new ShadowRuleConfigurationChecker().check("", createShadowRuleConfiguration(), createDataSourceMap(), Collections.emptyList());
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new LinkedHashMap<>(2, 1F);
        result.put("ds", new MockedDataSource());
        result.put("ds_shadow", new MockedDataSource());
        return result;
    }
    
    private ShadowRuleConfiguration createShadowRuleConfiguration() {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.setShadowAlgorithms(Collections.singletonMap("user-id-insert-match-algorithm", createAlgorithmConfiguration()));
        result.setDataSources(Collections.singleton(new ShadowDataSourceConfiguration("shadow-data-source", "ds", "ds_shadow")));
        result.setTables(Collections.singletonMap("t_order", new ShadowTableConfiguration(new LinkedList<>(), new LinkedList<>(Collections.singleton("user-id-insert-match-algorithm")))));
        return result;
    }
    
    private AlgorithmConfiguration createAlgorithmConfiguration() {
        return new AlgorithmConfiguration("user-id-insert-match-algorithm",
                PropertiesBuilder.build(new Property("column", "shadow"), new Property("operation", "insert"), new Property("regex", "[1]")));
    }
}
