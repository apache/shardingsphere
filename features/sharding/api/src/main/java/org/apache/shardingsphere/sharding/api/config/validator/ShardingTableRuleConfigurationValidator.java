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

package org.apache.shardingsphere.sharding.api.config.validator;

import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.expr.entry.InlineExpressionParserFactory;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Sharding table rule configuration validator.
 */
public final class ShardingTableRuleConfigurationValidator implements ConstraintValidator<ValidShardingTableRuleConfiguration, ShardingTableRuleConfiguration> {
    
    @Override
    public boolean isValid(final ShardingTableRuleConfiguration value, final ConstraintValidatorContext context) {
        Collection<String> actualDataNodes = InlineExpressionParserFactory.newInstance(value.getActualDataNodes()).splitAndEvaluate();
        Map<String, String> schemaNamesByDataSource = new HashMap<>(actualDataNodes.size(), 1F);
        for (String each : actualDataNodes) {
            DataNode dataNode = new DataNode(each);
            if (null == dataNode.getSchemaName()) {
                continue;
            }
            String configuredSchemaName = schemaNamesByDataSource.putIfAbsent(dataNode.getDataSourceName(), dataNode.getSchemaName());
            if (null != configuredSchemaName && !configuredSchemaName.equalsIgnoreCase(dataNode.getSchemaName())) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(String.format("contains multiple schemas for storage unit `%s` in sharding table `%s`",
                        dataNode.getDataSourceName(), value.getLogicTable())).addPropertyNode("actualDataNodes").addConstraintViolation();
                return false;
            }
        }
        return true;
    }
}
