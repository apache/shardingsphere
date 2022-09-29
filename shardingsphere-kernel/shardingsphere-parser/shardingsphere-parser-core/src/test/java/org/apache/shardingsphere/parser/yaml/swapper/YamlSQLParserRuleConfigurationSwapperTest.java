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
import org.apache.shardingsphere.parser.constant.SQLParserOrder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

import org.apache.shardingsphere.parser.yaml.config.YamlSQLParserCacheOptionRuleConfiguration;
import org.apache.shardingsphere.parser.yaml.config.YamlSQLParserRuleConfiguration;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.junit.Test;

public final class YamlSQLParserRuleConfigurationSwapperTest {
    
    @Test
    public void assertSwapToYamlConfiguration() {
        YamlSQLParserRuleConfiguration actualResult =
                new YamlSQLParserRuleConfigurationSwapper().swapToYamlConfiguration(new SQLParserRuleConfiguration(true, new CacheOption(2, 5), new CacheOption(4, 7)));
        assertTrue(actualResult.isSqlCommentParseEnabled());
        assertThat(actualResult.getParseTreeCache().getInitialCapacity(), is(2));
        assertThat(actualResult.getParseTreeCache().getMaximumSize(), is(5L));
        assertThat(actualResult.getSqlStatementCache().getInitialCapacity(), is(4));
        assertThat(actualResult.getSqlStatementCache().getMaximumSize(), is(7L));
    }
    
    @Test
    public void assertSwapToObjectWithDefaultConfig() {
        YamlSQLParserRuleConfiguration configuration = new YamlSQLParserRuleConfiguration();
        configuration.setSqlCommentParseEnabled(true);
        SQLParserRuleConfiguration actualResult = new YamlSQLParserRuleConfigurationSwapper().swapToObject(configuration);
        assertThat(actualResult.getParseTreeCache().getInitialCapacity(), is(128));
        assertThat(actualResult.getParseTreeCache().getMaximumSize(), is(1024L));
        assertThat(actualResult.getSqlStatementCache().getInitialCapacity(), is(2000));
        assertThat(actualResult.getSqlStatementCache().getMaximumSize(), is(65535L));
    }
    
    @Test
    public void assertSwapToObject() {
        YamlSQLParserRuleConfiguration configuration = new YamlSQLParserRuleConfiguration();
        configuration.setSqlCommentParseEnabled(true);
        configuration.setParseTreeCache(new YamlSQLParserCacheOptionRuleConfiguration());
        configuration.getParseTreeCache().setInitialCapacity(2);
        configuration.getParseTreeCache().setMaximumSize(5L);
        configuration.setSqlStatementCache(new YamlSQLParserCacheOptionRuleConfiguration());
        configuration.getSqlStatementCache().setInitialCapacity(4);
        configuration.getSqlStatementCache().setMaximumSize(7L);
        SQLParserRuleConfiguration actualResult = new YamlSQLParserRuleConfigurationSwapper().swapToObject(configuration);
        assertThat(actualResult.getParseTreeCache().getInitialCapacity(), is(2));
        assertThat(actualResult.getParseTreeCache().getMaximumSize(), is(5L));
        assertThat(actualResult.getSqlStatementCache().getInitialCapacity(), is(4));
        assertThat(actualResult.getSqlStatementCache().getMaximumSize(), is(7L));
    }
    
    @Test
    public void assertGetTypeClass() {
        assertThat(new YamlSQLParserRuleConfigurationSwapper().getTypeClass().toString(), is(SQLParserRuleConfiguration.class.toString()));
    }
    
    @Test
    public void assertGetRuleTagName() {
        assertThat(new YamlSQLParserRuleConfigurationSwapper().getRuleTagName(), is("SQL_PARSER"));
    }
    
    @Test
    public void assertGetOrder() {
        assertThat(new YamlSQLParserRuleConfigurationSwapper().getOrder(), is(SQLParserOrder.ORDER));
    }
}
