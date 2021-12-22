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

package org.apache.shardingsphere.data.pipeline.scenario.rulealtered;

import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.core.fixture.EmbedTestingServer;
import org.apache.shardingsphere.data.pipeline.core.util.ReflectionUtil;
import org.apache.shardingsphere.data.pipeline.core.util.ResourceUtil;
import org.apache.shardingsphere.data.pipeline.core.util.RuleAlteredContextUtil;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public final class RuleAlteredJobTest {
    
    @BeforeClass
    public static void beforeClass() throws Exception {
        EmbedTestingServer.start();
        RuleAlteredContextUtil.mockModeConfig();
    }
    
    @Test
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    public void assertExecute() {
        new RuleAlteredJob().execute(mockShardingContext());
        Map<String, RuleAlteredJobScheduler> jobSchedulerMap = ReflectionUtil.getStaticFieldValue(RuleAlteredJobSchedulerCenter.class, "JOB_SCHEDULER_MAP", Map.class);
        assertNotNull(jobSchedulerMap);
        assertFalse(jobSchedulerMap.isEmpty());
    }
    
    private ShardingContext mockShardingContext() {
        return new ShardingContext("1", null, 2, YamlEngine.marshal(ResourceUtil.mockJobConfig()), 0, null);
    }
}
