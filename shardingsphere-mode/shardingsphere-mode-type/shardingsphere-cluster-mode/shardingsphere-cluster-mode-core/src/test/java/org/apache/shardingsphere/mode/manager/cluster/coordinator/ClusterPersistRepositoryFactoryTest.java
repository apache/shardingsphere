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

package org.apache.shardingsphere.mode.manager.cluster.coordinator;

import org.apache.shardingsphere.mode.manager.cluster.coordinator.fixture.ClusterPersistRepositoryFixture;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryFactory;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ClusterPersistRepositoryFactoryTest {

    static {
        ShardingSphereServiceLoader.register(ClusterPersistRepositoryFixture.class);
    }

    @Test
    public void assertNewInstance() {
        ClusterPersistRepositoryConfiguration config = new ClusterPersistRepositoryConfiguration("TEST", "", "", new Properties());
        ClusterPersistRepository clusterPersistRepository = ClusterPersistRepositoryFactory.newInstance(config);
        assertThat(clusterPersistRepository.getType(), is("TEST"));
    }
}
