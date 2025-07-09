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

package org.apache.shardingsphere.data.pipeline.cdc.util;

import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CDCDataNodeUtilsTest {
    
    @Test
    void assertBuildTableAndDataNodesMap() {
        RuleMetaData ruleMetaData = mock(RuleMetaData.class);
        DataNodeRuleAttribute dataNodeRuleAttribute = mock(DataNodeRuleAttribute.class);
        when(dataNodeRuleAttribute.getAllDataNodes()).thenReturn(Collections.singletonMap("foo_tbl", Collections.singletonList(new DataNode("foo_ds.foo_tbl"))));
        when(ruleMetaData.getAttributes(DataNodeRuleAttribute.class)).thenReturn(Collections.singleton(dataNodeRuleAttribute));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getRuleMetaData()).thenReturn(ruleMetaData);
        Map<String, List<DataNode>> actual = CDCDataNodeUtils.buildTableAndDataNodesMap(database, Collections.singleton("foo_tbl"));
        assertThat(actual, is(Collections.singletonMap("foo_tbl", Collections.singletonList(new DataNode("foo_ds.foo_tbl")))));
    }
}
