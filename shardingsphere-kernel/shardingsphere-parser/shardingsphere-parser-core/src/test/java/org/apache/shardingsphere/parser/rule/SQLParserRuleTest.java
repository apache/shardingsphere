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

package org.apache.shardingsphere.parser.rule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.junit.Before;
import org.junit.Test;

public final class SQLParserRuleTest {
    
    private SQLParserRule sqlParserRule;
    
    @Before
    public void setup() {
        sqlParserRule = new SQLParserRule(new SQLParserRuleConfiguration(true, new CacheOption(2, 4), new CacheOption(3, 7)));
    }
    
    @Test
    public void assertGetSQLParserEngine() {
        assertNotNull(sqlParserRule.getSQLParserEngine("H2"));
    }
    
    @Test
    public void assertGetType() {
        assertThat(sqlParserRule.getType(), is(SQLParserRule.class.getSimpleName()));
    }
    
    @Test
    public void assertFields() {
        assertTrue(sqlParserRule.isSqlCommentParseEnabled());
        assertTrue(sqlParserRule.getConfiguration().isSqlCommentParseEnabled());
        assertThat(sqlParserRule.getConfiguration().getParseTreeCache().getInitialCapacity(), is(2));
        assertThat(sqlParserRule.getConfiguration().getParseTreeCache().getMaximumSize(), is(4L));
        assertThat(sqlParserRule.getConfiguration().getSqlStatementCache().getInitialCapacity(), is(3));
        assertThat(sqlParserRule.getConfiguration().getSqlStatementCache().getMaximumSize(), is(7L));
        assertThat(sqlParserRule.getParseTreeCache().getInitialCapacity(), is(2));
        assertThat(sqlParserRule.getParseTreeCache().getMaximumSize(), is(4L));
        assertThat(sqlParserRule.getSqlStatementCache().getInitialCapacity(), is(3));
        assertThat(sqlParserRule.getSqlStatementCache().getMaximumSize(), is(7L));
    }
}
