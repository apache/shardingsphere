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
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = YmlShadowDefaultSpringBootStarterTest.class)
@SpringBootApplication
@ActiveProfiles("shadow-default")
public class YmlShadowDefaultSpringBootStarterTest {
    
    @Resource
    private AlgorithmProvidedShadowRuleConfiguration shadowRuleConfiguration;
    
    @Test
    public void assertShadowRuleConfiguration() {
        assertThat(shadowRuleConfiguration.isEnable(), is(true));
        assertShadowDataSources(shadowRuleConfiguration.getDataSources());
        assertShadowTables(shadowRuleConfiguration.getTables());
        assertDefaultShadowAlgorithm(shadowRuleConfiguration.getDefaultShadowAlgorithmName());
        assertShadowAlgorithms(shadowRuleConfiguration.getShadowAlgorithms());
    }
    
    private void assertDefaultShadowAlgorithm(final String defaultShadowAlgorithmName) {
        assertThat("simple-note-algorithm".equals(defaultShadowAlgorithmName), is(true));
    }
    
    private void assertShadowAlgorithms(final Map<String, ShadowAlgorithm> shadowAlgorithms) {
        ShadowAlgorithm simpleNoteAlgorithm = shadowAlgorithms.get("simple-note-algorithm");
        assertThat(simpleNoteAlgorithm instanceof SimpleSQLNoteShadowAlgorithm, is(true));
        assertThat(simpleNoteAlgorithm.getType(), is("SIMPLE_HINT"));
        assertThat(simpleNoteAlgorithm.getProps().get("shadow"), is(true));
        assertThat(simpleNoteAlgorithm.getProps().get("foo"), is("bar"));
    }
    
    private void assertShadowTables(final Map<String, ShadowTableConfiguration> shadowTables) {
        assertThat(shadowTables.size(), is(0));
    }
    
    private void assertShadowDataSources(final Map<String, ShadowDataSourceConfiguration> dataSources) {
        assertThat(dataSources.size(), is(1));
        assertThat(dataSources.get("shadow-data-source").getSourceDataSourceName(), is("ds"));
        assertThat(dataSources.get("shadow-data-source").getShadowDataSourceName(), is("ds-shadow"));
    }
}
