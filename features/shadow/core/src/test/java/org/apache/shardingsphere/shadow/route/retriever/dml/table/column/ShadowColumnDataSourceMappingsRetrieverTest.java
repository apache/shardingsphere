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

package org.apache.shardingsphere.shadow.route.retriever.dml.table.column;

import org.apache.shardingsphere.shadow.route.determiner.ColumnShadowAlgorithmDeterminer;
import org.apache.shardingsphere.shadow.route.retriever.dml.table.column.fixture.ShadowColumnDataSourceMappingsRetrieverFixture;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.spi.ShadowOperationType;
import org.apache.shardingsphere.shadow.spi.column.ColumnShadowAlgorithm;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ColumnShadowAlgorithmDeterminer.class)
class ShadowColumnDataSourceMappingsRetrieverTest {
    
    @Mock
    private ShadowRule rule;
    
    @SuppressWarnings("unchecked")
    @Test
    void assertRetrieveWithShadow() {
        when(rule.getShadowColumnNames(ShadowOperationType.SELECT, "foo_tbl")).thenReturn(Collections.singleton("foo_col"));
        ColumnShadowAlgorithm<Comparable<?>> shadowAlgorithm = mock(ColumnShadowAlgorithm.class);
        when(rule.getColumnShadowAlgorithms(ShadowOperationType.SELECT, "foo_tbl", "foo_col")).thenReturn(Collections.singleton(shadowAlgorithm));
        when(ColumnShadowAlgorithmDeterminer.isShadow(eq(shadowAlgorithm), any())).thenReturn(true);
        when(rule.getShadowDataSourceMappings("foo_tbl")).thenReturn(Collections.singletonMap("prod_ds", "shadow_ds"));
        Map<String, String> actual = new ShadowColumnDataSourceMappingsRetrieverFixture().retrieve(rule, Arrays.asList("foo_tbl", "bar_tbl"));
        assertThat(actual, is(Collections.singletonMap("prod_ds", "shadow_ds")));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertRetrieveWithNotShadow() {
        when(rule.getShadowColumnNames(ShadowOperationType.SELECT, "foo_tbl")).thenReturn(Collections.singleton("foo_col"));
        ColumnShadowAlgorithm<Comparable<?>> shadowAlgorithm = mock(ColumnShadowAlgorithm.class);
        when(rule.getColumnShadowAlgorithms(ShadowOperationType.SELECT, "foo_tbl", "foo_col")).thenReturn(Collections.singleton(shadowAlgorithm));
        Map<String, String> actual = new ShadowColumnDataSourceMappingsRetrieverFixture().retrieve(rule, Arrays.asList("foo_tbl", "bar_tbl"));
        assertThat(actual, is(Collections.emptyMap()));
    }
    
    @Test
    void assertRetrieveWithEmptyShadowAlgorithm() {
        when(rule.getShadowColumnNames(ShadowOperationType.SELECT, "foo_tbl")).thenReturn(Collections.singleton("foo_col"));
        when(rule.getColumnShadowAlgorithms(ShadowOperationType.SELECT, "foo_tbl", "foo_col")).thenReturn(Collections.emptyList());
        Map<String, String> actual = new ShadowColumnDataSourceMappingsRetrieverFixture().retrieve(rule, Arrays.asList("foo_tbl", "bar_tbl"));
        assertThat(actual, is(Collections.emptyMap()));
    }
}
