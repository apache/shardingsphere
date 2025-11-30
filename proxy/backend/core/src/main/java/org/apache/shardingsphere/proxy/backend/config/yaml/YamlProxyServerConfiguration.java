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

package org.apache.shardingsphere.proxy.backend.config.yaml;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.authority.yaml.config.YamlAuthorityRuleConfiguration;
import org.apache.shardingsphere.globalclock.yaml.config.YamlGlobalClockRuleConfiguration;
import org.apache.shardingsphere.infra.util.yaml.YamlConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.mode.YamlModeConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.parser.yaml.config.YamlSQLParserRuleConfiguration;
import org.apache.shardingsphere.sqlfederation.yaml.config.YamlSQLFederationRuleConfiguration;
import org.apache.shardingsphere.sqltranslator.yaml.config.YamlSQLTranslatorRuleConfiguration;
import org.apache.shardingsphere.transaction.yaml.config.YamlTransactionRuleConfiguration;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

/**
 * YAML server configuration for ShardingSphere-Proxy.
 */
@Getter
@Setter
public final class YamlProxyServerConfiguration implements YamlConfiguration {
    
    private YamlModeConfiguration mode;
    
    private YamlAuthorityRuleConfiguration authority;
    
    private YamlTransactionRuleConfiguration transaction;
    
    private YamlSQLParserRuleConfiguration sqlParser;
    
    private YamlSQLTranslatorRuleConfiguration sqlTranslator;
    
    private YamlGlobalClockRuleConfiguration globalClock;
    
    private YamlSQLFederationRuleConfiguration sqlFederation;
    
    private Map<String, YamlProxyDataSourceConfiguration> dataSources = new HashMap<>();
    
    private Collection<YamlRuleConfiguration> rules = new LinkedList<>();
    
    private Properties props = new Properties();
    
    private Collection<String> labels;
    
    /**
     * Set rules if the param rules is not null.
     *
     * @param rules the rules to set
     */
    public void setRules(final Collection<YamlRuleConfiguration> rules) {
        if (null != rules) {
            this.rules = rules;
        }
    }
    
    /**
     * Set props if the param props is not null.
     *
     * @param props the props to set
     */
    public void setProps(final Properties props) {
        if (null != props) {
            this.props = props;
        }
    }
}
