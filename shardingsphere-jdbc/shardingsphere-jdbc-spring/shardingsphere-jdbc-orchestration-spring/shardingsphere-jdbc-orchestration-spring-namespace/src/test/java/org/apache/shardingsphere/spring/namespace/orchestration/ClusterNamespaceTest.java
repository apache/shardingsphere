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

package org.apache.shardingsphere.spring.namespace.orchestration;

import org.apache.shardingsphere.cluster.configuration.config.ClusterConfiguration;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "classpath:META-INF/rdb/namespace/clusterNamespace.xml")
public final class ClusterNamespaceTest extends AbstractJUnit4SpringContextTests {
    
    @Test
    public void assertClusterConfiguration() {
        ClusterConfiguration clusterConfiguration = applicationContext.getBean("cluster", ClusterConfiguration.class);
        assertNotNull(clusterConfiguration);
        assertNotNull(clusterConfiguration.getHeartbeat());
        assertThat(clusterConfiguration.getHeartbeat().getSql(), is("select 1"));
        assertThat(clusterConfiguration.getHeartbeat().getInterval(), is(60));
        assertThat(clusterConfiguration.getHeartbeat().getThreadCount(), is(1));
        assertFalse(clusterConfiguration.getHeartbeat().isRetryEnable());
        assertThat(clusterConfiguration.getHeartbeat().getRetryMaximum(), is(3));
        assertThat(clusterConfiguration.getHeartbeat().getRetryInterval(), is(3));
    }
}
