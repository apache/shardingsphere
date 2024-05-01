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

package org.apache.shardingsphere.parser.it;

import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.parser.yaml.config.YamlSQLParserCacheOptionRuleConfiguration;
import org.apache.shardingsphere.parser.yaml.config.YamlSQLParserRuleConfiguration;
import org.apache.shardingsphere.test.it.yaml.YamlRuleConfigurationIT;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class SQLParserRuleConfigurationYamlIT extends YamlRuleConfigurationIT {
    
    SQLParserRuleConfigurationYamlIT() {
        super("yaml/sql-parser-rule.yaml");
    }
    
    @Override
    protected void assertYamlRootConfiguration(final YamlRootConfiguration actual) {
        assertSQLParserRule((YamlSQLParserRuleConfiguration) actual.getRules().iterator().next());
    }
    
    private void assertSQLParserRule(final YamlSQLParserRuleConfiguration actual) {
        assertSQLStatementCache(actual.getSqlStatementCache());
        assertSQLParseTreeCache(actual.getParseTreeCache());
    }
    
    private void assertSQLStatementCache(final YamlSQLParserCacheOptionRuleConfiguration actual) {
        assertThat(actual.getInitialCapacity(), is(256));
        assertThat(actual.getMaximumSize(), is(4096L));
    }
    
    private void assertSQLParseTreeCache(final YamlSQLParserCacheOptionRuleConfiguration actual) {
        assertThat(actual.getInitialCapacity(), is(128));
        assertThat(actual.getMaximumSize(), is(1024L));
    }
}
