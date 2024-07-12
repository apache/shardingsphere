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

package org.apache.shardingsphere.mode.manager.cluster.workerid;

import org.apache.shardingsphere.infra.instance.workerid.WorkerIdGenerator;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClusterWorkerIdGeneratorTest {
    
    @Test
    void assertGenerateWithExistedWorkerId() {
        ClusterPersistRepository repository = mock(ClusterPersistRepository.class);
        when(repository.query("/nodes/compute_nodes/worker_id/foo_id")).thenReturn("10");
        assertThat(new ClusterWorkerIdGenerator(repository, "foo_id").generate(PropertiesBuilder.build(new Property(WorkerIdGenerator.WORKER_ID_KEY, "1"))), is(10));
    }
    
    @Test
    void assertGenerateWithoutExistedWorkerId() {
        ClusterPersistRepository repository = mock(ClusterPersistRepository.class);
        doAnswer((Answer<Object>) invocation -> Boolean.TRUE).when(repository).persistExclusiveEphemeral("/reservation/worker_id/0", "foo_id");
        assertThat(new ClusterWorkerIdGenerator(repository, "foo_id").generate(new Properties()), is(0));
    }
}
