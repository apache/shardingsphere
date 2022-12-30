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

import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.yaml.config.YamlSQLParserCacheOptionRuleConfiguration;
import org.apache.shardingsphere.parser.yaml.config.YamlSQLParserRuleConfiguration;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

public final class YamlSQLParserRuleConfigurationSwapperTest {
    
    @Test
    public void assertSwapToYamlConfiguration() {
        YamlSQLParserRuleConfiguration actual =
                new YamlSQLParserRuleConfigurationSwapper().swapToYamlConfiguration(new SQLParserRuleConfiguration(true, new CacheOption(2, 5), new CacheOption(4, 7)));
        assertTrue(actual.isSqlCommentParseEnabled());
        assertThat(actual.getParseTreeCache().getInitialCapacity(), is(2));
        assertThat(actual.getParseTreeCache().getMaximumSize(), is(5L));
        assertThat(actual.getSqlStatementCache().getInitialCapacity(), is(4));
        assertThat(actual.getSqlStatementCache().getMaximumSize(), is(7L));
    }
    
    @Test
    public void assertSwapToObjectWithDefaultConfig() {
        YamlSQLParserRuleConfiguration yamlConfig = new YamlSQLParserRuleConfiguration();
        yamlConfig.setSqlCommentParseEnabled(true);
        SQLParserRuleConfiguration actual = new YamlSQLParserRuleConfigurationSwapper().swapToObject(yamlConfig);
        assertThat(actual.getParseTreeCache().getInitialCapacity(), is(128));
        assertThat(actual.getParseTreeCache().getMaximumSize(), is(1024L));
        assertThat(actual.getSqlStatementCache().getInitialCapacity(), is(2000));
        assertThat(actual.getSqlStatementCache().getMaximumSize(), is(65535L));
    }
    
    @Test
    public void assertSwapToObject() {
        YamlSQLParserRuleConfiguration yamlConfig = new YamlSQLParserRuleConfiguration();
        yamlConfig.setSqlCommentParseEnabled(true);
        yamlConfig.setParseTreeCache(new YamlSQLParserCacheOptionRuleConfiguration());
        yamlConfig.getParseTreeCache().setInitialCapacity(2);
        yamlConfig.getParseTreeCache().setMaximumSize(5L);
        yamlConfig.setSqlStatementCache(new YamlSQLParserCacheOptionRuleConfiguration());
        yamlConfig.getSqlStatementCache().setInitialCapacity(4);
        yamlConfig.getSqlStatementCache().setMaximumSize(7L);
        SQLParserRuleConfiguration actual = new YamlSQLParserRuleConfigurationSwapper().swapToObject(yamlConfig);
        assertThat(actual.getParseTreeCache().getInitialCapacity(), is(2));
        assertThat(actual.getParseTreeCache().getMaximumSize(), is(5L));
        assertThat(actual.getSqlStatementCache().getInitialCapacity(), is(4));
        assertThat(actual.getSqlStatementCache().getMaximumSize(), is(7L));
    }
}
