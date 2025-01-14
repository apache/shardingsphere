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

package org.apache.shardingsphere.mode.metadata.persist.service.config.database;

import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.mode.metadata.persist.fixture.NoTupleRuleConfigurationFixture;
import org.apache.shardingsphere.mode.metadata.persist.fixture.MetaDataRuleConfigurationFixture;
import org.apache.shardingsphere.mode.metadata.persist.service.config.RepositoryTuplePersistService;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseRulePersistServiceTest {
    
    private DatabaseRulePersistService persistService;
    
    @Mock
    private PersistRepository repository;
    
    @Mock
    private RepositoryTuplePersistService repositoryTuplePersistService;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        persistService = new DatabaseRulePersistService(repository);
        Plugins.getMemberAccessor().set(DatabaseRulePersistService.class.getDeclaredField("repositoryTuplePersistService"), persistService, repositoryTuplePersistService);
    }
    
    @Test
    void assertLoad() {
        assertTrue(persistService.load("foo_db").isEmpty());
    }
    
    @Test
    void assertPersistWithoutActiveVersion() {
        Collection<MetaDataVersion> actual = persistService.persist("foo_db", Arrays.asList(new MetaDataRuleConfigurationFixture("test"), new NoTupleRuleConfigurationFixture("test")));
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getKey(), is("/metadata/foo_db/rules/fixture/fixture"));
        assertNull(actual.iterator().next().getCurrentActiveVersion());
        assertThat(actual.iterator().next().getNextActiveVersion(), is("0"));
    }
    
    @Test
    void assertPersistWithActiveVersion() {
        when(repository.query("/metadata/foo_db/rules/fixture/fixture/active_version")).thenReturn("10");
        when(repository.getChildrenKeys("/metadata/foo_db/rules/fixture/fixture/versions")).thenReturn(Collections.singletonList("10"));
        Collection<MetaDataVersion> actual = persistService.persist("foo_db", Collections.singleton(new MetaDataRuleConfigurationFixture("test")));
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getKey(), is("/metadata/foo_db/rules/fixture/fixture"));
        assertThat(actual.iterator().next().getCurrentActiveVersion(), is("10"));
        assertThat(actual.iterator().next().getNextActiveVersion(), is("11"));
    }
    
    @Test
    void assertDeleteWithRuleTypeName() {
        persistService.delete("foo_db", "fixture_rule");
        verify(repository).delete("/metadata/foo_db/rules/fixture_rule");
    }
    
    @Test
    void assertDeleteWithRuleConfigurations() {
        Collection<MetaDataVersion> actual = persistService.delete("foo_db", Arrays.asList(new MetaDataRuleConfigurationFixture("test"), new NoTupleRuleConfigurationFixture("test")));
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getKey(), is("/metadata/foo_db/rules/fixture/fixture"));
        assertThat(actual.iterator().next().getCurrentActiveVersion(), is(""));
        assertThat(actual.iterator().next().getNextActiveVersion(), is(""));
    }
}
