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

package org.apache.shardingsphere.parser.rule.builder;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.constant.SQLParserOrder;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.junit.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

public final class SQLParserRuleBuilderTest {
    
    @Mock
    private InstanceContext instanceContext;
    
    @Test
    public void assertBuild() {
        SQLParserRuleConfiguration ruleConfig = new SQLParserRuleConfiguration(true, new CacheOption(2, 5), new CacheOption(4, 7));
        SQLParserRule actualResult = new SQLParserRuleBuilder().build(ruleConfig, new HashMap<>(), instanceContext, new ConfigurationProperties(new Properties()));
        assertThat(actualResult.getConfiguration(), is(ruleConfig));
        assertTrue(actualResult.isSqlCommentParseEnabled());
        assertThat(actualResult.getSqlStatementCache().getInitialCapacity(), is(4));
        assertThat(actualResult.getSqlStatementCache().getMaximumSize(), is(7L));
        assertThat(actualResult.getParseTreeCache().getInitialCapacity(), is(2));
        assertThat(actualResult.getParseTreeCache().getMaximumSize(), is(5L));
    }
    
    @Test
    public void assertGetOrder() {
        assertThat(new SQLParserRuleBuilder().getOrder(), is(SQLParserOrder.ORDER));
    }
    
    @Test
    public void assertGetTypeClass() {
        assertThat(new SQLParserRuleBuilder().getTypeClass().toString(), is(SQLParserRuleConfiguration.class.toString()));
    }
}
