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

package org.apache.shardingsphere.sqltranslator.yaml.swapper;

import org.apache.shardingsphere.infra.config.nodepath.GlobalNodePath;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.datanode.YamlDataNode;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.NewYamlGlobalRuleConfigurationSwapper;
import org.apache.shardingsphere.sqltranslator.api.config.SQLTranslatorRuleConfiguration;
import org.apache.shardingsphere.sqltranslator.constant.SQLTranslatorOrder;
import org.apache.shardingsphere.sqltranslator.yaml.config.YamlSQLTranslatorRuleConfiguration;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * TODO Rename YamlSQLTranslatorRuleConfigurationSwapper when metadata structure adjustment completed. #25485
 * YAML SQL translator rule configuration swapper.
 */
public final class NewYamlSQLTranslatorRuleConfigurationSwapper implements NewYamlGlobalRuleConfigurationSwapper<SQLTranslatorRuleConfiguration> {
    
    @Override
    public Collection<YamlDataNode> swapToDataNodes(final SQLTranslatorRuleConfiguration data) {
        return Collections.singletonList(new YamlDataNode(getRuleTagName().toLowerCase(), YamlEngine.marshal(swapToYamlConfiguration(data))));
    }
    
    private YamlSQLTranslatorRuleConfiguration swapToYamlConfiguration(final SQLTranslatorRuleConfiguration data) {
        YamlSQLTranslatorRuleConfiguration result = new YamlSQLTranslatorRuleConfiguration();
        result.setType(data.getType());
        result.setUseOriginalSQLWhenTranslatingFailed(data.isUseOriginalSQLWhenTranslatingFailed());
        return result;
    }
    
    @Override
    public Optional<SQLTranslatorRuleConfiguration> swapToObject(final Collection<YamlDataNode> dataNodes) {
        for (YamlDataNode each : dataNodes) {
            Optional<String> version = GlobalNodePath.getVersion(getRuleTagName().toLowerCase(), each.getKey());
            if (!version.isPresent()) {
                continue;
            }
            return Optional.of(swapToObject(YamlEngine.unmarshal(each.getValue(), YamlSQLTranslatorRuleConfiguration.class)));
        }
        return Optional.empty();
    }
    
    private SQLTranslatorRuleConfiguration swapToObject(final YamlSQLTranslatorRuleConfiguration yamlConfig) {
        return new SQLTranslatorRuleConfiguration(yamlConfig.getType(), yamlConfig.isUseOriginalSQLWhenTranslatingFailed());
    }
    
    @Override
    public Class<SQLTranslatorRuleConfiguration> getTypeClass() {
        return SQLTranslatorRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "SQL_TRANSLATOR";
    }
    
    @Override
    public int getOrder() {
        return SQLTranslatorOrder.ORDER;
    }
}
