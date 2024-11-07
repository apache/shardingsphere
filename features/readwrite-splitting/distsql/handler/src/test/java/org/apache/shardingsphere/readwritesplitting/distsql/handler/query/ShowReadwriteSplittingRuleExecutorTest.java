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

package org.apache.shardingsphere.readwritesplitting.distsql.handler.query;

import org.apache.shardingsphere.distsql.statement.DistSQLStatement;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.scope.DatabaseRuleConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.exportable.constant.ExportableConstants;
import org.apache.shardingsphere.readwritesplitting.config.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.ShowReadwriteSplittingRulesStatement;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.apache.shardingsphere.readwritesplitting.rule.attribute.ReadwriteSplittingExportableRuleAttribute;
import org.apache.shardingsphere.test.it.distsql.handler.engine.query.DistSQLDatabaseRuleQueryExecutorTest;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowReadwriteSplittingRuleExecutorTest extends DistSQLDatabaseRuleQueryExecutorTest {
    
    ShowReadwriteSplittingRuleExecutorTest() {
        super(mockRule());
    }
    
    private static ReadwriteSplittingRule mockRule() {
        ReadwriteSplittingRule result = mock(ReadwriteSplittingRule.class);
        ReadwriteSplittingExportableRuleAttribute ruleAttribute = mock(ReadwriteSplittingExportableRuleAttribute.class);
        when(ruleAttribute.getExportData()).thenReturn(createExportedData());
        when(result.getAttributes()).thenReturn(new RuleAttributes(ruleAttribute));
        return result;
    }
    
    private static Map<String, Object> createExportedData() {
        Map<String, Object> result = new HashMap<>();
        result.put(ExportableConstants.EXPORT_STATIC_READWRITE_SPLITTING_RULE, Collections.emptyMap());
        return result;
    }
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertExecuteQuery(final String name, final DatabaseRuleConfiguration ruleConfig, final DistSQLStatement sqlStatement,
                            final Collection<LocalDataQueryResultRow> expected) throws SQLException {
        assertQueryResultRows(ruleConfig, sqlStatement, expected);
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            return Stream.of(Arguments.arguments("withNull", createRuleConfiguration(), new ShowReadwriteSplittingRulesStatement(null, null),
                    Collections.singleton(new LocalDataQueryResultRow("readwrite_ds", "ds_primary", "ds_slave_0,ds_slave_1", "DYNAMIC", "random", "{\"read_weight\":\"2:1\"}"))),
                    Arguments.arguments("withSpecifiedRuleName", createRuleConfiguration(), new ShowReadwriteSplittingRulesStatement("readwrite_ds", null),
                            Collections.singleton(new LocalDataQueryResultRow("readwrite_ds", "ds_primary", "ds_slave_0,ds_slave_1", "DYNAMIC", "random", "{\"read_weight\":\"2:1\"}"))),
                    Arguments.arguments("withoutLoadBalancer", createRuleConfigurationWithoutLoadBalancer(), new ShowReadwriteSplittingRulesStatement("readwrite_ds", null),
                            Collections.singleton(new LocalDataQueryResultRow("readwrite_ds", "write_ds", "read_ds_0,read_ds_1", "DYNAMIC", "", ""))));
        }
        
        private ReadwriteSplittingRuleConfiguration createRuleConfiguration() {
            ReadwriteSplittingDataSourceGroupRuleConfiguration dataSourceGroupConfig =
                    new ReadwriteSplittingDataSourceGroupRuleConfiguration("readwrite_ds", "ds_primary", Arrays.asList("ds_slave_0", "ds_slave_1"), "test");
            return new ReadwriteSplittingRuleConfiguration(
                    Collections.singleton(dataSourceGroupConfig), Collections.singletonMap("test", new AlgorithmConfiguration("random", PropertiesBuilder.build(new Property("read_weight", "2:1")))));
        }
        
        private ReadwriteSplittingRuleConfiguration createRuleConfigurationWithoutLoadBalancer() {
            ReadwriteSplittingDataSourceGroupRuleConfiguration dataSourceGroupConfig =
                    new ReadwriteSplittingDataSourceGroupRuleConfiguration("readwrite_ds", "write_ds", Arrays.asList("read_ds_0", "read_ds_1"), null);
            return new ReadwriteSplittingRuleConfiguration(Collections.singleton(dataSourceGroupConfig), null);
        }
    }
}
