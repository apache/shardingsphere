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

package org.apache.shardingsphere.governance.core.facade.repository;

import org.apache.shardingsphere.governance.core.facade.fixture.TestAllRepository;
import org.apache.shardingsphere.governance.core.facade.util.FieldUtil;
import org.apache.shardingsphere.governance.repository.api.ConfigurationRepository;
import org.apache.shardingsphere.governance.repository.api.RegistryRepository;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceCenterConfiguration;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceConfiguration;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class GovernanceRepositoryFacadeTest {
    
    @Test
    public void assertNewWithoutAdditionalConfigurationRepository() {
        GovernanceRepositoryFacade actual = new GovernanceRepositoryFacade(
                new GovernanceConfiguration("test", new GovernanceCenterConfiguration("ALL", "127.0.0.1:8888", new Properties()), true));
        assertThat(actual.getRegistryRepository(), is(actual.getConfigurationRepository()));
    }
    
    @Test
    public void assertNewWithAdditionalConfigurationRepository() {
        GovernanceRepositoryFacade actual = new GovernanceRepositoryFacade(new GovernanceConfiguration("test", 
                new GovernanceCenterConfiguration("ALL", "127.0.0.1:8888", new Properties()), new GovernanceCenterConfiguration("CONFIG", "127.0.0.1:9999", new Properties()), true));
        assertThat(actual.getRegistryRepository(), not(actual.getConfigurationRepository()));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewWithoutAdditionalConfigurationRepositoryAndInvalidRegistryRepository() {
        new GovernanceRepositoryFacade(new GovernanceConfiguration("test", new GovernanceCenterConfiguration("REG", "127.0.0.1:8888", new Properties()), true));
    }
    
    @Test
    public void assertCloseWithoutAdditionalConfigurationRepository() {
        GovernanceRepositoryFacade actual = new GovernanceRepositoryFacade(
                new GovernanceConfiguration("test", new GovernanceCenterConfiguration("ALL", "127.0.0.1:8888", new Properties()), true));
        TestAllRepository repository = mock(TestAllRepository.class);
        FieldUtil.setField(actual, "registryRepository", repository);
        FieldUtil.setField(actual, "configurationRepository", repository);
        actual.close();
        verify(repository).close();
    }
    
    @Test
    public void assertCloseWithAdditionalConfigurationRepository() {
        GovernanceRepositoryFacade actual = new GovernanceRepositoryFacade(new GovernanceConfiguration("test",
                new GovernanceCenterConfiguration("ALL", "127.0.0.1:8888", new Properties()), new GovernanceCenterConfiguration("CONFIG", "127.0.0.1:9999", new Properties()), true));
        RegistryRepository registryRepository = mock(RegistryRepository.class);
        ConfigurationRepository configurationRepository = mock(ConfigurationRepository.class);
        FieldUtil.setField(actual, "registryRepository", registryRepository);
        FieldUtil.setField(actual, "configurationRepository", configurationRepository);
        actual.close();
        verify(registryRepository).close();
        verify(configurationRepository).close();
    }
}
