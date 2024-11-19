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

package org.apache.shardingsphere.readwritesplitting.rule.attribute;

import org.apache.shardingsphere.infra.rule.attribute.exportable.constant.ExportableConstants;
import org.apache.shardingsphere.infra.rule.attribute.exportable.constant.ExportableItemConstants;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingDataSourceGroupRule;
import org.apache.shardingsphere.readwritesplitting.transaction.TransactionalReadQueryStrategy;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class ReadwriteSplittingExportableRuleAttributeTest {
    
    @Test
    void assertGetExportData() {
        Map<String, ReadwriteSplittingDataSourceGroupRule> dataSourceGroupRules = new HashMap<>();
        dataSourceGroupRules.put("ignored_group", mock(ReadwriteSplittingDataSourceGroupRule.class));
        ReadwriteSplittingDataSourceGroupRuleConfiguration config = new ReadwriteSplittingDataSourceGroupRuleConfiguration("foo_group", "write_ds", Arrays.asList("read_ds0", "read_ds1"), "foo_algo");
        dataSourceGroupRules.put("foo_group", new ReadwriteSplittingDataSourceGroupRule(config, TransactionalReadQueryStrategy.FIXED, null));
        ReadwriteSplittingExportableRuleAttribute ruleAttribute = new ReadwriteSplittingExportableRuleAttribute(dataSourceGroupRules);
        Map<String, Object> actual = ruleAttribute.getExportData();
        assertThat(actual.size(), is(1));
        assertStaticDataSources(actual);
    }
    
    @SuppressWarnings("unchecked")
    private void assertStaticDataSources(final Map<String, Object> actual) {
        Map<String, Map<String, String>> actualStaticDataSources = (Map<String, Map<String, String>>) actual.get(ExportableConstants.EXPORT_STATIC_READWRITE_SPLITTING_RULE);
        assertThat(actualStaticDataSources.size(), is(1));
        assertThat(actualStaticDataSources.get("foo_group").size(), is(2));
        assertThat(actualStaticDataSources.get("foo_group").get(ExportableItemConstants.PRIMARY_DATA_SOURCE_NAME), is("write_ds"));
        assertThat(actualStaticDataSources.get("foo_group").get(ExportableItemConstants.REPLICA_DATA_SOURCE_NAMES), is("read_ds0,read_ds1"));
    }
}
