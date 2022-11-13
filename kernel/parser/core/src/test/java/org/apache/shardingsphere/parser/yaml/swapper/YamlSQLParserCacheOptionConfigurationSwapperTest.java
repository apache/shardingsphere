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

package org.apache.shardingsphere.parser.yaml.swapper;

import org.apache.shardingsphere.parser.yaml.config.YamlSQLParserCacheOptionRuleConfiguration;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public final class YamlSQLParserCacheOptionConfigurationSwapperTest {
    
    @Test
    public void assertSwapToYamlConfiguration() {
        YamlSQLParserCacheOptionRuleConfiguration actual = new YamlSQLParserCacheOptionConfigurationSwapper().swapToYamlConfiguration(new CacheOption(2, 5));
        assertThat(actual.getInitialCapacity(), is(2));
        assertThat(actual.getMaximumSize(), is(5L));
    }
    
    @Test
    public void assertSwapToObject() {
        YamlSQLParserCacheOptionRuleConfiguration cacheOptionRuleConfig = new YamlSQLParserCacheOptionRuleConfiguration();
        cacheOptionRuleConfig.setInitialCapacity(2);
        cacheOptionRuleConfig.setMaximumSize(5L);
        CacheOption actual = new YamlSQLParserCacheOptionConfigurationSwapper().swapToObject(cacheOptionRuleConfig);
        assertThat(actual.getInitialCapacity(), is(2));
        assertThat(actual.getMaximumSize(), is(5L));
    }
}
