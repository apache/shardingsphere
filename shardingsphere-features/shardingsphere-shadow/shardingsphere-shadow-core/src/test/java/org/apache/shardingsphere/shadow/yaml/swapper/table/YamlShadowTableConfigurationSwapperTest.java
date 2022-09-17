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

package org.apache.shardingsphere.shadow.yaml.swapper.table;

import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.yaml.config.table.YamlShadowTableConfiguration;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class YamlShadowTableConfigurationSwapperTest {
    
    @Test
    public void assertSwapToYamlConfiguration() {
        YamlShadowTableConfiguration actual = new YamlShadowTableConfigurationSwapper().swapToYamlConfiguration(
                new ShadowTableConfiguration(Collections.singleton("shadow-data-source"), Arrays.asList("user-id-match-algorithm", "note-algorithm")));
        assertThat(actual.getDataSourceNames(), is(Collections.singleton("shadow-data-source")));
        assertThat(actual.getShadowAlgorithmNames(), is(Arrays.asList("user-id-match-algorithm", "note-algorithm")));
    }
    
    @Test
    public void assertSwapToObject() {
        YamlShadowTableConfiguration yamlConfig = new YamlShadowTableConfiguration();
        yamlConfig.setDataSourceNames(Collections.singleton("shadow-data-source"));
        yamlConfig.setShadowAlgorithmNames(Arrays.asList("user-id-match-algorithm", "note-algorithm"));
        ShadowTableConfiguration actual = new YamlShadowTableConfigurationSwapper().swapToObject(yamlConfig);
        assertThat(actual.getDataSourceNames(), is(Collections.singleton("shadow-data-source")));
        assertThat(actual.getShadowAlgorithmNames(), is(Arrays.asList("user-id-match-algorithm", "note-algorithm")));
    }
}
