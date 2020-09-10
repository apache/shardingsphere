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

package org.apache.shardingsphere.primaryreplica.spring.boot;

import org.apache.shardingsphere.primaryreplica.algorithm.RandomPrimaryReplicaLoadBalanceAlgorithm;
import org.apache.shardingsphere.primaryreplica.algorithm.config.AlgorithmProvidedPrimaryReplicaRuleConfiguration;
import org.apache.shardingsphere.primaryreplica.api.config.rule.PrimaryReplicaDataSourceRuleConfiguration;
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
@SpringBootTest(classes = PrimaryReplicaSpringBootStarterTest.class)
@SpringBootApplication
@ActiveProfiles("primaryreplica")
public class PrimaryReplicaSpringBootStarterTest {
    
    @Resource
    private RandomPrimaryReplicaLoadBalanceAlgorithm random;
    
    @Resource
    private AlgorithmProvidedPrimaryReplicaRuleConfiguration primaryReplicaRuleConfiguration;
    
    @Test
    public void assertLoadBalanceAlgorithm() {
        assertTrue(random.getProps().isEmpty());
    }
    
    @Test
    public void assertPrimaryReplicaRuleConfiguration() {
        assertThat(primaryReplicaRuleConfiguration.getDataSources().size(), is(1));
        PrimaryReplicaDataSourceRuleConfiguration primaryReplicaDataSourceRuleConfiguration = primaryReplicaRuleConfiguration.getDataSources().stream().findFirst().get();
        assertThat(primaryReplicaDataSourceRuleConfiguration.getName(), is("ds_ms"));
        assertThat(primaryReplicaDataSourceRuleConfiguration.getPrimaryDataSourceName(), is("ds_primary"));
        assertThat(primaryReplicaDataSourceRuleConfiguration.getLoadBalancerName(), is("random"));
        assertThat(primaryReplicaDataSourceRuleConfiguration.getReplicaDataSourceNames().size(), is(2));
        assertTrue(primaryReplicaRuleConfiguration.getDataSources().contains(primaryReplicaDataSourceRuleConfiguration));
        assertThat(primaryReplicaRuleConfiguration.getLoadBalanceAlgorithms().size(), is(1));
        assertTrue(primaryReplicaRuleConfiguration.getLoadBalanceAlgorithms().containsKey("random"));
    }
}
