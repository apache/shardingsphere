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

import org.apache.shardingsphere.infra.config.nodepath.GlobalNodePath;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.datanode.YamlDataNode;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.NewYamlGlobalRuleConfigurationSwapper;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.constant.SQLParserOrder;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.parser.yaml.config.YamlSQLParserRuleConfiguration;
import org.apache.shardingsphere.sql.parser.api.CacheOption;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * TODO Rename YamlSQLTranslatorRuleConfigurationSwapper when metadata structure adjustment completed. #25485
 * YAML SQL parser rule configuration swapper.
 */
public final class NewYamlSQLParserRuleConfigurationSwapper implements NewYamlGlobalRuleConfigurationSwapper<SQLParserRuleConfiguration> {
    
    private final YamlSQLParserCacheOptionConfigurationSwapper cacheOptionSwapper = new YamlSQLParserCacheOptionConfigurationSwapper();
    
    @Override
    public Collection<YamlDataNode> swapToDataNodes(final SQLParserRuleConfiguration data) {
        return Collections.singletonList(new YamlDataNode(getRuleTagName().toLowerCase(), YamlEngine.marshal(swapToYamlConfiguration(data))));
    }
    
    private YamlSQLParserRuleConfiguration swapToYamlConfiguration(final SQLParserRuleConfiguration data) {
        YamlSQLParserRuleConfiguration result = new YamlSQLParserRuleConfiguration();
        result.setSqlCommentParseEnabled(data.isSqlCommentParseEnabled());
        result.setParseTreeCache(cacheOptionSwapper.swapToYamlConfiguration(data.getParseTreeCache()));
        result.setSqlStatementCache(cacheOptionSwapper.swapToYamlConfiguration(data.getSqlStatementCache()));
        return result;
    }
    
    @Override
    public Optional<SQLParserRuleConfiguration> swapToObject(final Collection<YamlDataNode> dataNodes) {
        for (YamlDataNode each : dataNodes) {
            Optional<String> version = GlobalNodePath.getVersion(getRuleTagName().toLowerCase(), each.getKey());
            if (!version.isPresent()) {
                continue;
            }
            return Optional.of(swapToObject(YamlEngine.unmarshal(each.getValue(), YamlSQLParserRuleConfiguration.class)));
        }
        return Optional.empty();
    }
    
    private SQLParserRuleConfiguration swapToObject(final YamlSQLParserRuleConfiguration yamlConfig) {
        CacheOption parseTreeCacheOption = null == yamlConfig.getParseTreeCache()
                ? DefaultSQLParserRuleConfigurationBuilder.PARSE_TREE_CACHE_OPTION
                : cacheOptionSwapper.swapToObject(yamlConfig.getParseTreeCache());
        CacheOption sqlStatementCacheOption = null == yamlConfig.getSqlStatementCache()
                ? DefaultSQLParserRuleConfigurationBuilder.SQL_STATEMENT_CACHE_OPTION
                : cacheOptionSwapper.swapToObject(yamlConfig.getSqlStatementCache());
        return new SQLParserRuleConfiguration(yamlConfig.isSqlCommentParseEnabled(), parseTreeCacheOption, sqlStatementCacheOption);
    }
    
    @Override
    public Class<SQLParserRuleConfiguration> getTypeClass() {
        return SQLParserRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "SQL_PARSER";
    }
    
    @Override
    public int getOrder() {
        return SQLParserOrder.ORDER;
    }
}
