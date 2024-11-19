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

package org.apache.shardingsphere.shadow.route.retriever.dml.table.hint;

import org.apache.shardingsphere.shadow.algorithm.shadow.hint.SQLHintShadowAlgorithm;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.spi.ShadowOperationType;
import org.apache.shardingsphere.shadow.spi.hint.HintShadowAlgorithm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShadowTableHintDataSourceMappingsRetrieverTest {
    
    @Mock
    private ShadowRule rule;
    
    @Test
    void assertRetrieveWithDefaultAlgorithmAndWithShadow() {
        HintShadowAlgorithm<?> shadowAlgorithm = new SQLHintShadowAlgorithm();
        when(rule.getDefaultShadowAlgorithm()).thenReturn(Optional.of(shadowAlgorithm));
        when(rule.getAllShadowDataSourceMappings()).thenReturn(Collections.singletonMap("prod_ds", "shadow_ds"));
        Map<String, String> actual = new ShadowTableHintDataSourceMappingsRetriever(ShadowOperationType.HINT_MATCH, true).retrieve(rule, Collections.emptyList());
        assertThat(actual, is(Collections.singletonMap("prod_ds", "shadow_ds")));
    }
    
    @Test
    void assertRetrieveWithDefaultAlgorithmAndWithNotShadow() {
        HintShadowAlgorithm<?> shadowAlgorithm = new SQLHintShadowAlgorithm();
        when(rule.getDefaultShadowAlgorithm()).thenReturn(Optional.of(shadowAlgorithm));
        Map<String, String> actual = new ShadowTableHintDataSourceMappingsRetriever(ShadowOperationType.HINT_MATCH, false).retrieve(rule, Collections.emptyList());
        assertTrue(actual.isEmpty());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertRetrieveWithSQLHintsAndWithShadow() {
        HintShadowAlgorithm<?> shadowAlgorithm = new SQLHintShadowAlgorithm();
        when(rule.getHintShadowAlgorithms("foo_tbl")).thenReturn(Collections.singleton((HintShadowAlgorithm<Comparable<?>>) shadowAlgorithm));
        when(rule.getShadowDataSourceMappings("foo_tbl")).thenReturn(Collections.singletonMap("prod_ds", "shadow_ds"));
        Map<String, String> actual = new ShadowTableHintDataSourceMappingsRetriever(ShadowOperationType.HINT_MATCH, true).retrieve(rule, Collections.singleton("foo_tbl"));
        assertThat(actual, is(Collections.singletonMap("prod_ds", "shadow_ds")));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertRetrieveWithSQLHintsAndWithNotShadow() {
        HintShadowAlgorithm<?> shadowAlgorithm = new SQLHintShadowAlgorithm();
        when(rule.getHintShadowAlgorithms("foo_tbl")).thenReturn(Collections.singleton((HintShadowAlgorithm<Comparable<?>>) shadowAlgorithm));
        Map<String, String> actual = new ShadowTableHintDataSourceMappingsRetriever(ShadowOperationType.HINT_MATCH, false).retrieve(rule, Collections.singleton("foo_tbl"));
        assertTrue(actual.isEmpty());
    }
    
    @Test
    void assertRetrieveWithEmptyTableAndWithoutDefaultAlgorithm() {
        Map<String, String> actual = new ShadowTableHintDataSourceMappingsRetriever(ShadowOperationType.HINT_MATCH, true).retrieve(rule, Collections.emptyList());
        assertTrue(actual.isEmpty());
    }
}
