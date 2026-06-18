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

package org.apache.shardingsphere.readwritesplitting.checker;

import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.exception.actual.DuplicateReadwriteSplittingActualDataSourceException;
import org.apache.shardingsphere.readwritesplitting.exception.actual.MissingRequiredReadwriteSplittingActualDataSourceException;
import org.apache.shardingsphere.readwritesplitting.exception.actual.ReadwriteSplittingActualDataSourceNotFoundException;
import org.apache.shardingsphere.readwritesplitting.exception.logic.MissingRequiredReadwriteSplittingDataSourceRuleNameException;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReadwriteSplittingDataSourceRuleConfigurationCheckerTest {
    
    @Test
    void assertCheckWithEmptyGroupName() {
        ReadwriteSplittingDataSourceGroupRuleConfiguration config = new ReadwriteSplittingDataSourceGroupRuleConfiguration("", "write_ds", Arrays.asList("read_ds0", "read_ds1"), "foo_algo");
        assertThrows(MissingRequiredReadwriteSplittingDataSourceRuleNameException.class, () -> new ReadwriteSplittingDataSourceRuleConfigurationChecker(
                "foo_db", config, Collections.emptyMap()).check(Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
    }
    
    @Test
    void assertCheckWithEmptyWriteDataSourceName() {
        ReadwriteSplittingDataSourceGroupRuleConfiguration config = new ReadwriteSplittingDataSourceGroupRuleConfiguration("foo_group", "", Arrays.asList("read_ds0", "read_ds1"), "foo_algo");
        assertThrows(MissingRequiredReadwriteSplittingActualDataSourceException.class, () -> new ReadwriteSplittingDataSourceRuleConfigurationChecker(
                "foo_db", config, Collections.emptyMap()).check(Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
    }
    
    @Test
    void assertCheckWithEmptyReadDataSourceNames() {
        ReadwriteSplittingDataSourceGroupRuleConfiguration config = new ReadwriteSplittingDataSourceGroupRuleConfiguration("foo_group", "write_ds", Collections.emptyList(), "foo_algo");
        assertThrows(MissingRequiredReadwriteSplittingActualDataSourceException.class, () -> new ReadwriteSplittingDataSourceRuleConfigurationChecker(
                "foo_db", config, Collections.emptyMap()).check(Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
    }
    
    @Test
    void assertCheckWithNotExistedWriteDataSourceName() {
        ReadwriteSplittingDataSourceGroupRuleConfiguration config = new ReadwriteSplittingDataSourceGroupRuleConfiguration("foo_group", "write_ds", Arrays.asList("read_ds0", "read_ds1"), "foo_algo");
        assertThrows(ReadwriteSplittingActualDataSourceNotFoundException.class, () -> new ReadwriteSplittingDataSourceRuleConfigurationChecker(
                "foo_db", config, Collections.emptyMap()).check(Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
    }
    
    @Test
    void assertCheckWithDuplicatedWriteDataSourceName() {
        ReadwriteSplittingDataSourceGroupRuleConfiguration config = new ReadwriteSplittingDataSourceGroupRuleConfiguration("foo_group", "write_ds", Arrays.asList("read_ds0", "read_ds1"), "foo_algo");
        assertThrows(DuplicateReadwriteSplittingActualDataSourceException.class, () -> new ReadwriteSplittingDataSourceRuleConfigurationChecker("foo_db",
                config, Collections.singletonMap("write_ds", mock(DataSource.class))).check(new HashSet<>(Collections.singleton("write_ds")), Collections.emptyList(), Collections.emptyList()));
    }
    
    @Test
    void assertCheckSuccess() {
        ReadwriteSplittingDataSourceGroupRuleConfiguration config = new ReadwriteSplittingDataSourceGroupRuleConfiguration("foo_group", "write_ds", Arrays.asList("read_ds0", "read_ds1"), "foo_algo");
        assertDoesNotThrow(() -> new ReadwriteSplittingDataSourceRuleConfigurationChecker("foo_db", config,
                Collections.singletonMap("write_ds", mock(DataSource.class))).check(new HashSet<>(), new HashSet<>(), Arrays.asList(mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS), mockRule())));
    }
    
    private static ShardingSphereRule mockRule() {
        ShardingSphereRule result = mock(ShardingSphereRule.class);
        DataSourceMapperRuleAttribute dataSourceMapperRuleAttribute = mock(DataSourceMapperRuleAttribute.class, RETURNS_DEEP_STUBS);
        when(dataSourceMapperRuleAttribute.getDataSourceMapper().containsKey("read_ds0")).thenReturn(true);
        when(dataSourceMapperRuleAttribute.getDataSourceMapper().containsKey("read_ds1")).thenReturn(true);
        when(result.getAttributes()).thenReturn(new RuleAttributes(dataSourceMapperRuleAttribute));
        return result;
    }
}
