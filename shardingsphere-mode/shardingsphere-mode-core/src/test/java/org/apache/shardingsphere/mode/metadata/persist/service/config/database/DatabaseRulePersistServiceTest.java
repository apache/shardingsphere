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

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.mode.persist.PersistRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DatabaseRulePersistServiceTest {
    
    @Mock
    private PersistRepository repository;
    
    @Test
    public void assertLoadWithoutExistedNode() {
        assertTrue(new DatabaseRulePersistService(repository).load("foo_db").isEmpty());
    }
    
    @Test
    public void assertLoadWithExistedNode() {
        when(repository.get("/metadata/foo_db/active_version")).thenReturn("0");
        when(repository.get("/metadata/foo_db/versions/0/rules")).thenReturn(readYAML());
        Collection<RuleConfiguration> actual = new DatabaseRulePersistService(repository).load("foo_db");
        assertThat(actual.size(), is(1));
    }
    
    @Test
    public void assertIsExisted() {
        when(repository.get("/metadata/foo_db/active_version")).thenReturn("0");
        when(repository.get("/metadata/foo_db/versions/0/rules")).thenReturn(readYAML());
        DatabaseRulePersistService databaseRulePersistService = new DatabaseRulePersistService(repository);
        assertTrue(databaseRulePersistService.isExisted("foo_db"));
        assertFalse(databaseRulePersistService.isExisted("foo_db_1"));
    }
    
    @SneakyThrows({IOException.class, URISyntaxException.class})
    private String readYAML() {
        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource("yaml/persist/data-database-rule.yaml").toURI()))
                .stream().map(each -> each + System.lineSeparator()).collect(Collectors.joining());
    }
}
