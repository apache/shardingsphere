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

package org.apache.shardingsphere.data.pipeline.core.listener;

import org.apache.shardingsphere.elasticjob.infra.listener.ElasticJobListener;
import org.apache.shardingsphere.elasticjob.infra.listener.ShardingContexts;
import org.apache.shardingsphere.elasticjob.infra.spi.ElasticJobServiceLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PipelineElasticJobListenerTest {
    
    private PipelineElasticJobListener listener;
    
    @BeforeEach
    void setUp() {
        ElasticJobServiceLoader.registerTypedService(ElasticJobListener.class);
        listener = (PipelineElasticJobListener) ElasticJobServiceLoader.newTypedServiceInstance(ElasticJobListener.class, PipelineElasticJobListener.class.getName(), new Properties()).orElse(null);
    }
    
    @Test
    void assertBeforeJobExecuted() {
        listener.beforeJobExecuted(new ShardingContexts("foo_id", "foo_job", 1, "", Collections.emptyMap()));
        assertTrue(listener.isJobRunning("foo_job"));
    }
    
    @Test
    void assertBeforeJobExecutedTwice() {
        ShardingContexts shardingContexts = new ShardingContexts("foo_id", "foo_job", 1, "", Collections.emptyMap());
        listener.beforeJobExecuted(shardingContexts);
        listener.beforeJobExecuted(shardingContexts);
        assertTrue(listener.isJobRunning("foo_job"));
    }
    
    @Test
    void assertAfterJobExecuted() {
        ShardingContexts shardingContexts = new ShardingContexts("foo_id", "foo_job", 1, "", Collections.emptyMap());
        listener.beforeJobExecuted(shardingContexts);
        listener.afterJobExecuted(shardingContexts);
        assertFalse(listener.isJobRunning("foo_job"));
    }
}
