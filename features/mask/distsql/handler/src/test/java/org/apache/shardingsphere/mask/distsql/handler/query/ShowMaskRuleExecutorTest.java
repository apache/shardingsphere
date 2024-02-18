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

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.distsql.statement.ShowMaskRulesStatement;
import org.apache.shardingsphere.mask.rule.MaskRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowMaskRuleExecutorTest {
    
    @Test
    void assertGetRowData() {
        ShowMaskRuleExecutor executor = new ShowMaskRuleExecutor();
        MaskRule rule = mock(MaskRule.class);
        when(rule.getConfiguration()).thenReturn(getRuleConfiguration());
        executor.setRule(rule);
        Collection<LocalDataQueryResultRow> actual = executor.getRows(mock(ShowMaskRulesStatement.class), mock(ContextManager.class));
        assertThat(actual.size(), is(1));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("t_mask"));
        assertThat(row.getCell(2), is("user_id"));
        assertThat(row.getCell(3), is("md5"));
        assertThat(row.getCell(4), is(""));
    }
    
    private MaskRuleConfiguration getRuleConfiguration() {
        MaskColumnRuleConfiguration maskColumnRuleConfig = new MaskColumnRuleConfiguration("user_id", "t_mask_user_id_md5");
        MaskTableRuleConfiguration maskTableRuleConfig = new MaskTableRuleConfiguration("t_mask", Collections.singleton(maskColumnRuleConfig));
        AlgorithmConfiguration algorithmConfig = new AlgorithmConfiguration("md5", new Properties());
        return new MaskRuleConfiguration(Collections.singleton(maskTableRuleConfig), Collections.singletonMap("t_mask_user_id_md5", algorithmConfig));
    }
}
