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

package org.apache.shardingsphere.mode.manager.cluster.persist.builder;

import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.cluster.persist.service.ClusterMetaDataManagerPersistService;
import org.apache.shardingsphere.mode.manager.cluster.persist.service.ClusterProcessPersistService;
import org.apache.shardingsphere.mode.metadata.MetaDataContextManager;
import org.apache.shardingsphere.mode.persist.service.divided.PersistServiceBuilder;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class ClusterPersistServiceBuilderTest {
    
    private final PersistServiceBuilder persistServiceBuilder = TypedSPILoader.getService(PersistServiceBuilder.class, "Cluster");
    
    @Test
    void assertBuildMetaDataManagerPersistService() {
        assertThat(persistServiceBuilder.buildMetaDataManagerPersistService(mock(PersistRepository.class), mock(MetaDataContextManager.class)),
                instanceOf(ClusterMetaDataManagerPersistService.class));
    }
    
    @Test
    void assertBuildProcessPersistService() {
        assertThat(persistServiceBuilder.buildProcessPersistService(mock(PersistRepository.class)), instanceOf(ClusterProcessPersistService.class));
    }
}
