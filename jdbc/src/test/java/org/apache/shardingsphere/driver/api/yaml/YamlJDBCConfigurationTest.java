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

package org.apache.shardingsphere.driver.api.yaml;

import org.apache.shardingsphere.authority.yaml.config.YamlAuthorityRuleConfiguration;
import org.apache.shardingsphere.globalclock.core.yaml.config.YamlGlobalClockRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.mode.YamlModeConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.mode.YamlPersistRepositoryConfiguration;
import org.apache.shardingsphere.logging.yaml.config.YamlLoggingRuleConfiguration;
import org.apache.shardingsphere.parser.yaml.config.YamlSQLParserRuleConfiguration;
import org.apache.shardingsphere.sqlfederation.yaml.config.YamlSQLFederationRuleConfiguration;
import org.apache.shardingsphere.sqltranslator.yaml.config.YamlSQLTranslatorRuleConfiguration;
import org.apache.shardingsphere.traffic.yaml.config.YamlTrafficRuleConfiguration;
import org.apache.shardingsphere.transaction.yaml.config.YamlTransactionRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class YamlJDBCConfigurationTest {
    
    @Test
    void assertRebuildYamlJDBCConfiguration() {
        YamlJDBCConfiguration config = new YamlJDBCConfiguration();
        config.setDatabaseName("test");
        Map<String, Object> dataSourceProps = new HashMap<>();
        dataSourceProps.put("url", "jdbc:mysql://localhost:3306/demo_ds");
        dataSourceProps.put("username", "root");
        dataSourceProps.put("password", "root");
        config.getDataSources().put("ds", dataSourceProps);
        YamlModeConfiguration mode = new YamlModeConfiguration();
        mode.setType("Local");
        YamlPersistRepositoryConfiguration repository = new YamlPersistRepositoryConfiguration();
        repository.setType("MySQL");
        mode.setRepository(repository);
        config.setMode(mode);
        YamlAuthorityRuleConfiguration authority = new YamlAuthorityRuleConfiguration();
        config.setAuthority(authority);
        YamlSQLParserRuleConfiguration sqlParser = new YamlSQLParserRuleConfiguration();
        config.setSqlParser(sqlParser);
        YamlTransactionRuleConfiguration transaction = new YamlTransactionRuleConfiguration();
        config.setTransaction(transaction);
        YamlGlobalClockRuleConfiguration globalClock = new YamlGlobalClockRuleConfiguration();
        config.setGlobalClock(globalClock);
        YamlSQLFederationRuleConfiguration sqlFederation = new YamlSQLFederationRuleConfiguration();
        config.setSqlFederation(sqlFederation);
        YamlSQLTranslatorRuleConfiguration sqlTranslator = new YamlSQLTranslatorRuleConfiguration();
        config.setSqlTranslator(sqlTranslator);
        YamlTrafficRuleConfiguration traffic = new YamlTrafficRuleConfiguration();
        config.setTraffic(traffic);
        YamlLoggingRuleConfiguration logging = new YamlLoggingRuleConfiguration();
        config.setLogging(logging);
        Properties props = new Properties();
        props.setProperty("sql.show", "true");
        config.setProps(props);
        config.rebuild();
        
        assertThat(config.getDatabaseName(), is("test"));
        assertThat(config.getDataSources().get("ds"), is(dataSourceProps));
        assertThat(config.getMode().getType(), is("Local"));
        assertThat(config.getMode().getRepository(), is(repository));
        assertIterableEquals(config.getRules(), Arrays.asList(authority, sqlParser, transaction, globalClock,
                sqlFederation, sqlTranslator, traffic, logging));
    }
}
