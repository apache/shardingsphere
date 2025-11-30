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

package org.apache.shardingsphere.driver.yaml;

import org.apache.shardingsphere.authority.yaml.config.YamlAuthorityRuleConfiguration;
import org.apache.shardingsphere.globalclock.yaml.config.YamlGlobalClockRuleConfiguration;
import org.apache.shardingsphere.parser.yaml.config.YamlSQLParserRuleConfiguration;
import org.apache.shardingsphere.sqlfederation.yaml.config.YamlSQLFederationRuleConfiguration;
import org.apache.shardingsphere.sqltranslator.yaml.config.YamlSQLTranslatorRuleConfiguration;
import org.apache.shardingsphere.transaction.yaml.config.YamlTransactionRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class YamlJDBCConfigurationTest {
    
    @Test
    void assertRebuild() {
        YamlJDBCConfiguration actual = new YamlJDBCConfiguration();
        YamlAuthorityRuleConfiguration authorityRuleConfig = new YamlAuthorityRuleConfiguration();
        actual.setAuthority(authorityRuleConfig);
        YamlSQLParserRuleConfiguration sqlParserRuleConfig = new YamlSQLParserRuleConfiguration();
        actual.setSqlParser(sqlParserRuleConfig);
        YamlTransactionRuleConfiguration transactionRuleConfig = new YamlTransactionRuleConfiguration();
        actual.setTransaction(transactionRuleConfig);
        YamlGlobalClockRuleConfiguration globalClockRuleConfig = new YamlGlobalClockRuleConfiguration();
        actual.setGlobalClock(globalClockRuleConfig);
        YamlSQLFederationRuleConfiguration sqlFederationRuleConfig = new YamlSQLFederationRuleConfiguration();
        actual.setSqlFederation(sqlFederationRuleConfig);
        YamlSQLTranslatorRuleConfiguration sqlTranslatorRuleConfig = new YamlSQLTranslatorRuleConfiguration();
        actual.setSqlTranslator(sqlTranslatorRuleConfig);
        actual.rebuild();
        assertThat(actual.getRules(), is(Arrays.asList(
                authorityRuleConfig, sqlParserRuleConfig, transactionRuleConfig, globalClockRuleConfig, sqlFederationRuleConfig, sqlTranslatorRuleConfig)));
    }
}
