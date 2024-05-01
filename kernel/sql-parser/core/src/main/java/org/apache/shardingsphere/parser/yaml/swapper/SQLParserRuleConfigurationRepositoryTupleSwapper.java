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

import org.apache.shardingsphere.mode.path.GlobalNodePath;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.datanode.RepositoryTuple;
import org.apache.shardingsphere.mode.spi.RepositoryTupleSwapper;
import org.apache.shardingsphere.parser.constant.SQLParserOrder;
import org.apache.shardingsphere.parser.yaml.config.YamlSQLParserRuleConfiguration;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * SQL parser rule configuration repository tuple swapper.
 */
public final class SQLParserRuleConfigurationRepositoryTupleSwapper implements RepositoryTupleSwapper<YamlSQLParserRuleConfiguration> {
    
    @Override
    public Collection<RepositoryTuple> swapToRepositoryTuples(final YamlSQLParserRuleConfiguration yamlRuleConfig) {
        return Collections.singleton(new RepositoryTuple(getRuleTypeName(), YamlEngine.marshal(yamlRuleConfig)));
    }
    
    @Override
    public Optional<YamlSQLParserRuleConfiguration> swapToObject(final Collection<RepositoryTuple> repositoryTuples) {
        for (RepositoryTuple each : repositoryTuples) {
            if (GlobalNodePath.getVersion(getRuleTypeName(), each.getKey()).isPresent()) {
                return Optional.of(YamlEngine.unmarshal(each.getValue(), YamlSQLParserRuleConfiguration.class));
            }
        }
        return Optional.empty();
    }
    
    @Override
    public Class<YamlSQLParserRuleConfiguration> getTypeClass() {
        return YamlSQLParserRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTypeName() {
        return "sql_parser";
    }
    
    @Override
    public int getOrder() {
        return SQLParserOrder.ORDER;
    }
}
