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

package org.apache.shardingsphere.replication.primaryreplica.spring.boot;

import org.apache.shardingsphere.replication.primaryreplica.algorithm.RandomReplicaLoadBalanceAlgorithm;
import org.apache.shardingsphere.replication.primaryreplica.algorithm.config.AlgorithmProvidedPrimaryReplicaReplicationRuleConfiguration;
import org.apache.shardingsphere.replication.primaryreplica.api.config.rule.PrimaryReplicaReplicationDataSourceRuleConfiguration;
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
@SpringBootTest(classes = PrimaryReplicaReplicationSpringBootStarterTest.class)
@SpringBootApplication
@ActiveProfiles("replica-query")
public class PrimaryReplicaReplicationSpringBootStarterTest {
    
    @Resource
    private RandomReplicaLoadBalanceAlgorithm random;
    
    @Resource
    private AlgorithmProvidedPrimaryReplicaReplicationRuleConfiguration config;
    
    @Test
    public void assertLoadBalanceAlgorithm() {
        assertTrue(random.getProps().isEmpty());
    }
    
    @Test
    public void assertPrimaryReplicaReplicationRuleConfiguration() {
        assertThat(config.getDataSources().size(), is(1));
        PrimaryReplicaReplicationDataSourceRuleConfiguration dataSourceRuleConfig = config.getDataSources().stream().findFirst().get();
        assertThat(dataSourceRuleConfig.getName(), is("pr_ds"));
        assertThat(dataSourceRuleConfig.getPrimaryDataSourceName(), is("primary_ds"));
        assertThat(dataSourceRuleConfig.getLoadBalancerName(), is("random"));
        assertThat(dataSourceRuleConfig.getReplicaDataSourceNames().size(), is(2));
        assertTrue(config.getDataSources().contains(dataSourceRuleConfig));
        assertThat(config.getLoadBalanceAlgorithms().size(), is(1));
        assertTrue(config.getLoadBalanceAlgorithms().containsKey("random"));
    }
}
