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

package org.apache.shardingsphere.metadata.persist.service.config.database;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.metadata.persist.service.config.database.rule.DatabaseRulePersistService;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseRulePersistServiceTest {
    
    @Mock
    private PersistRepository repository;
    
    @Test
    void assertLoadWithoutExistedNode() {
        assertTrue(new DatabaseRulePersistService(repository).load("foo_db").isEmpty());
    }
    
    @Test
    void assertLoadWithExistedNode() {
        when(repository.getDirectly("/metadata/foo_db/active_version")).thenReturn("0");
        when(repository.getDirectly("/metadata/foo_db/versions/0/rules")).thenReturn(readYAML());
        Collection<RuleConfiguration> actual = new DatabaseRulePersistService(repository).load("foo_db");
        assertThat(actual.size(), is(1));
    }
    
    @SneakyThrows({IOException.class, URISyntaxException.class})
    private String readYAML() {
        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource("yaml/persist/data-database-rule.yaml").toURI()))
                .stream().map(each -> each + System.lineSeparator()).collect(Collectors.joining());
    }
}
