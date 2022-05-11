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

import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.shadow.algorithm.config.AlgorithmProvidedShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.factory.ShadowAlgorithmFactory;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

public final class AlgorithmProvidedShadowRuleConfigurationCheckerTest {
    
    @Test
    public void assertCheck() {
        new AlgorithmProvidedShadowRuleConfigurationChecker().check("", createAlgorithmProvidedShadowRuleConfiguration());
    }
    
    private AlgorithmProvidedShadowRuleConfiguration createAlgorithmProvidedShadowRuleConfiguration() {
        AlgorithmProvidedShadowRuleConfiguration result = new AlgorithmProvidedShadowRuleConfiguration();
        result.setShadowAlgorithms(Collections.singletonMap(
                "user-id-insert-match-algorithm", ShadowAlgorithmFactory.newInstance(new ShardingSphereAlgorithmConfiguration("REGEX_MATCH", createProperties()))));
        result.setDataSources(Collections.singletonMap("shadow-data-source", new ShadowDataSourceConfiguration("ds", "ds_shadow")));
        result.setTables(Collections.singletonMap("t_order", new ShadowTableConfiguration(new LinkedList<>(), new LinkedList<>(Collections.singleton("user-id-insert-match-algorithm")))));
        return result;
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty("column", "shadow");
        result.setProperty("operation", "insert");
        result.setProperty("regex", "[1]");
        return result;
    }
}
