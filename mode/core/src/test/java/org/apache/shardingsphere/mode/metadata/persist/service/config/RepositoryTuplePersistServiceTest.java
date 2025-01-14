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

package org.apache.shardingsphere.mode.metadata.persist.service.config;

import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.apache.shardingsphere.mode.node.tuple.RepositoryTuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryTuplePersistServiceTest {
    
    private RepositoryTuplePersistService persistService;
    
    @Mock
    private PersistRepository repository;
    
    @BeforeEach
    void setUp() {
        persistService = new RepositoryTuplePersistService(repository);
    }
    
    @Test
    void assertLoadWithChildrenPath() {
        when(repository.getChildrenKeys("root")).thenReturn(Collections.singletonList("foo/active_version"));
        when(repository.query("root/foo/active_version")).thenReturn("0");
        when(repository.query("root/foo/versions/0")).thenReturn("foo_content");
        Collection<RepositoryTuple> actual = persistService.load("root");
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getKey(), is("root/foo/versions/0"));
        assertThat(actual.iterator().next().getValue(), is("foo_content"));
    }
    
    @Test
    void assertLoadWithoutChildrenPath() {
        when(repository.getChildrenKeys("root")).thenReturn(Collections.emptyList());
        Collection<RepositoryTuple> actual = persistService.load("root");
        assertTrue(actual.isEmpty());
    }
}
