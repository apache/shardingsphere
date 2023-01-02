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

package org.apache.shardingsphere.mask.distsql.handler.query;

import org.apache.shardingsphere.distsql.handler.resultset.DatabaseDistSQLResultSet;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.distsql.parser.statement.ShowMaskRulesStatement;
import org.apache.shardingsphere.mask.rule.MaskRule;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class MaskRuleResultSetTest {
    
    @Test
    public void assertGetRowData() {
        ShardingSphereDatabase database = mockDatabase();
        DatabaseDistSQLResultSet resultSet = new MaskRuleResultSet();
        resultSet.init(database, mock(ShowMaskRulesStatement.class));
        assertColumns(resultSet.getColumnNames());
        Collection<Object> actual = resultSet.getRowData();
        assertTrue(actual.contains("t_mask"));
        assertTrue(actual.contains("user_id"));
        assertTrue(actual.contains("md5"));
    }
    
    @Test
    public void assertGetRowDataWithoutMaskRule() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        DatabaseDistSQLResultSet resultSet = new MaskRuleResultSet();
        resultSet.init(database, mock(ShowMaskRulesStatement.class));
        assertFalse(resultSet.next());
    }
    
    private void assertColumns(final Collection<String> actual) {
        assertThat(actual.size(), is(4));
        assertTrue(actual.containsAll(Arrays.asList("table", "column", "algorithm_type", "algorithm_props")));
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        MaskRule rule = mock(MaskRule.class);
        when(rule.getConfiguration()).thenReturn(getRuleConfiguration());
        when(result.getRuleMetaData().findSingleRule(MaskRule.class)).thenReturn(Optional.of(rule));
        return result;
    }
    
    private RuleConfiguration getRuleConfiguration() {
        MaskColumnRuleConfiguration maskColumnRuleConfig = new MaskColumnRuleConfiguration("user_id", "t_mask_user_id_md5");
        MaskTableRuleConfiguration maskTableRuleConfig = new MaskTableRuleConfiguration("t_mask", Collections.singleton(maskColumnRuleConfig));
        AlgorithmConfiguration algorithmConfig = new AlgorithmConfiguration("md5", new Properties());
        return new MaskRuleConfiguration(Collections.singleton(maskTableRuleConfig), Collections.singletonMap("t_mask_user_id_md5", algorithmConfig));
    }
}
