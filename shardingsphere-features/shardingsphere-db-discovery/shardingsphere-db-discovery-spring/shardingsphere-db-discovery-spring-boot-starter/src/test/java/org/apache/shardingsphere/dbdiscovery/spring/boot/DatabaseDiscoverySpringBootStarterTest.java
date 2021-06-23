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

package org.apache.shardingsphere.dbdiscovery.spring.boot;

import org.apache.shardingsphere.dbdiscovery.algorithm.config.AlgorithmProvidedDatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DatabaseDiscoverySpringBootStarterTest.class)
@SpringBootApplication
@ActiveProfiles("database-discovery")
public class DatabaseDiscoverySpringBootStarterTest {
    
    @Resource
    private AlgorithmProvidedDatabaseDiscoveryRuleConfiguration config;
    
    @Test
    public void assertHARuleConfiguration() {
        assertThat(config.getDataSources().size(), is(1));
        assertTrue(config.getDataSources().stream().findFirst().isPresent());
        DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig = config.getDataSources().stream().findFirst().get();
        assertThat(dataSourceRuleConfig.getName(), is("pr_ds"));
        assertThat(dataSourceRuleConfig.getDataSourceNames().size(), is(2));
        assertTrue(config.getDataSources().contains(dataSourceRuleConfig));
    }
}
