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

import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.constant.SQLParserOrder;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class DefaultSQLParserRuleConfigurationBuilderTest {
    
    @Test
    void assertBuild() {
        SQLParserRuleConfiguration actual = new DefaultSQLParserRuleConfigurationBuilder().build();
        assertThat(actual.getParseTreeCache().getInitialCapacity(), is(128));
        assertThat(actual.getParseTreeCache().getMaximumSize(), is(1024L));
        assertThat(actual.getSqlStatementCache().getInitialCapacity(), is(2000));
        assertThat(actual.getSqlStatementCache().getMaximumSize(), is(65535L));
    }
    
    @Test
    void assertGetOrder() {
        assertThat(new DefaultSQLParserRuleConfigurationBuilder().getOrder(), is(SQLParserOrder.ORDER));
    }
    
    @Test
    void assertGetTypeClass() {
        assertThat(new DefaultSQLParserRuleConfigurationBuilder().getTypeClass().toString(), is(SQLParserRuleBuilder.class.toString()));
    }
}
