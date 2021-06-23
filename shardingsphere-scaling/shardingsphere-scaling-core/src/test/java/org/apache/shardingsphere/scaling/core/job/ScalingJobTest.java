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

package org.apache.shardingsphere.scaling.core.job;

import lombok.SneakyThrows;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.governance.repository.api.config.RegistryCenterConfiguration;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceConfiguration;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;
import org.apache.shardingsphere.scaling.core.executor.engine.ExecuteEngine;
import org.apache.shardingsphere.scaling.core.fixture.EmbedTestingServer;
import org.apache.shardingsphere.scaling.core.job.schedule.JobScheduler;
import org.apache.shardingsphere.scaling.core.job.schedule.JobSchedulerCenter;
import org.apache.shardingsphere.scaling.core.util.ReflectionUtil;
import org.apache.shardingsphere.scaling.core.util.ResourceUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public final class ScalingJobTest {
    
    @BeforeClass
    public static void beforeClass() throws Exception {
        EmbedTestingServer.start();
        ReflectionUtil.setFieldValue(ScalingContext.getInstance(), "serverConfig", mockServerConfig());
        ReflectionUtil.setFieldValue(ScalingContext.getInstance(), "inventoryDumperExecuteEngine", mock(ExecuteEngine.class));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    public void assertExecute() {
        new ScalingJob().execute(mockShardingContext());
        Map<String, JobScheduler> jobSchedulerMap = ReflectionUtil.getStaticFieldValue(JobSchedulerCenter.class, "JOB_SCHEDULER_MAP", Map.class);
        assertNotNull(jobSchedulerMap);
        assertFalse(jobSchedulerMap.isEmpty());
    }
    
    @AfterClass
    public static void afterClass() throws Exception {
        ReflectionUtil.setFieldValue(ScalingContext.getInstance(), "serverConfig", null);
    }
    
    private static ServerConfiguration mockServerConfig() {
        ServerConfiguration result = new ServerConfiguration();
        result.setGovernanceConfig(new GovernanceConfiguration("test", new RegistryCenterConfiguration("Zookeeper", EmbedTestingServer.getConnectionString(), null), true));
        return result;
    }
    
    private ShardingContext mockShardingContext() {
        return new ShardingContext("1", null, 2, YamlEngine.marshal(ResourceUtil.mockJobConfig()), 0, null);
    }
}
