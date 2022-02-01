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

package org.apache.shardingsphere.mode.manager.standalone;

import org.apache.shardingsphere.infra.config.mode.PersistRepositoryConfiguration;
import org.apache.shardingsphere.mode.manager.standalone.fixture.StandalonePersistRepositoryFixture;
import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepository;
import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepositoryConfiguration;
import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepositoryFactory;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.exception.ServiceProviderNotFoundException;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class StandalonePersistRepositoryFactoryTest {

    static {
        ShardingSphereServiceLoader.register(StandalonePersistRepositoryFixture.class);
    }

    @Test
    public void assertNewInstanceWithNoConfig() {
        StandalonePersistRepository standalonePersistRepository = StandalonePersistRepositoryFactory.newInstance(null);
        assertThat(standalonePersistRepository.getType(), is("File"));
    }

    @Test
    public void assertNewInstanceWithConfig() {
        PersistRepositoryConfiguration config = new StandalonePersistRepositoryConfiguration("File", new Properties());
        StandalonePersistRepository standalonePersistRepository = StandalonePersistRepositoryFactory.newInstance(config);
        assertNotNull(standalonePersistRepository);
    }

    @Test(expected = ServiceProviderNotFoundException.class)
    public void assertNewInstanceWhenTypeIsNotExist() {
        PersistRepositoryConfiguration config = new StandalonePersistRepositoryConfiguration("NOT_EXISTED", new Properties());
        StandalonePersistRepositoryFactory.newInstance(config);
    }
}
