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

package org.apache.shardingsphere.shadow.spring.boot;

import org.apache.shardingsphere.shadow.algorithm.config.AlgorithmProvidedShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.algorithm.shadow.column.ColumnRegexMatchShadowAlgorithm;
import org.apache.shardingsphere.shadow.algorithm.shadow.note.SimpleSQLNoteShadowAlgorithm;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = PropertiesShadowSpringBootStarterTest.class)
@SpringBootApplication
@ActiveProfiles("shadow-properties")
public class PropertiesShadowSpringBootStarterTest {
    
    @Resource
    private AlgorithmProvidedShadowRuleConfiguration shadowRuleConfiguration;
    
    @Test
    public void assertShadowRuleConfiguration() {
        assertThat(shadowRuleConfiguration.isEnable(), is(false));
        assertBasicShadowRule(shadowRuleConfiguration.getColumn(), shadowRuleConfiguration.getSourceDataSourceNames(), shadowRuleConfiguration.getShadowDataSourceNames());
        assertShadowDataSources(shadowRuleConfiguration.getDataSources());
        assertShadowTables(shadowRuleConfiguration.getTables());
        assertShadowAlgorithms(shadowRuleConfiguration.getShadowAlgorithms());
    }
    
    private void assertShadowAlgorithms(final Map<String, ShadowAlgorithm> shadowAlgorithms) {
        assertThat(shadowAlgorithms.get("t-order-user-id-algorithm") instanceof ColumnRegexMatchShadowAlgorithm, is(true));
        assertThat(shadowAlgorithms.get("t-order-note-algorithm") instanceof SimpleSQLNoteShadowAlgorithm, is(true));
    }
    
    private void assertShadowTables(final Map<String, ShadowTableConfiguration> shadowTables) {
        assertThat(shadowTables.size(), is(2));
        assertThat(shadowTables.get("t_order").getShadowAlgorithmNames(), is(Arrays.asList("t-order-user-id-algorithm", "t-order-note-algorithm", "t-order-note-algorithm")));
        assertThat(shadowTables.get("t_user").getShadowAlgorithmNames(), is(Arrays.asList("t-order-user-id-algorithm", "t-order-note-algorithm")));
    }
    
    private void assertShadowDataSources(final Map<String, ShadowDataSourceConfiguration> dataSources) {
        assertThat(dataSources.size(), is(1));
        assertThat(dataSources.get("shadow-data-source").getSourceDataSourceName(), is("ds0"));
        assertThat(dataSources.get("shadow-data-source").getShadowDataSourceName(), is("ds0-shadow"));
    }
    
    private void assertBasicShadowRule(final String column, final List<String> sourceDataSourceNames, final List<String> shadowDataSourceNames) {
        assertThat(column, is("shadow"));
        assertThat(sourceDataSourceNames, is(Arrays.asList("ds", "ds1")));
        assertThat(shadowDataSourceNames, is(Arrays.asList("shadow_ds", "shadow_ds1")));
    }
}
