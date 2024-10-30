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

import org.apache.shardingsphere.distsql.handler.executor.rql.resource.InUsedStorageUnitRetriever;
import org.apache.shardingsphere.distsql.statement.rql.rule.database.ShowRulesUsedStorageUnitStatement;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mask.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.rule.MaskRule;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InUsedMaskStorageUnitRetrieverTest {
    
    @SuppressWarnings("unchecked")
    private final InUsedStorageUnitRetriever<MaskRule> retriever = TypedSPILoader.getService(InUsedStorageUnitRetriever.class, MaskRule.class);
    
    @Test
    void assertGetInUsedResources() {
        ShowRulesUsedStorageUnitStatement sqlStatement = new ShowRulesUsedStorageUnitStatement(null, null);
        MaskRule rule = mockRule();
        assertThat(retriever.getInUsedResources(sqlStatement, rule), is(Collections.singletonList("foo_tbl")));
    }
    
    private MaskRule mockRule() {
        MaskRule result = mock(MaskRule.class, RETURNS_DEEP_STUBS);
        when(result.getConfiguration().getTables()).thenReturn(Collections.singleton(new MaskTableRuleConfiguration("foo_tbl", Collections.emptyList())));
        return result;
    }
}
