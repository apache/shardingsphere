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

package org.apache.shardingsphere.data.pipeline.core.datanode;

import org.apache.shardingsphere.infra.datanode.DataNode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class JobDataNodeLineConvertUtilsTest {
    
    @Test
    void assertConvertDataNodesToLines() {
        Map<String, List<DataNode>> mockedTableAndDataNodesMap = new LinkedHashMap<>(2, 1F);
        List<DataNode> dataNodes = Arrays.asList(new DataNode("ds_0", (String) null, "t_order_0"), new DataNode("ds_0", (String) null, "t_order_1"));
        List<DataNode> itemDataNodes = Collections.singletonList(new DataNode("ds_0", (String) null, "t_order_item_0"));
        mockedTableAndDataNodesMap.put("t_order", dataNodes);
        mockedTableAndDataNodesMap.put("t_order_item", itemDataNodes);
        List<JobDataNodeLine> jobDataNodeLines = JobDataNodeLineConvertUtils.convertDataNodesToLines(mockedTableAndDataNodesMap);
        assertThat(jobDataNodeLines.size(), is(1));
        List<JobDataNodeEntry> actualNodeEntry = new ArrayList<>(jobDataNodeLines.get(0).getEntries());
        assertThat(actualNodeEntry.get(0).getLogicTableName(), is("t_order"));
        assertThat(actualNodeEntry.get(0).getDataNodes().size(), is(2));
        assertThat(actualNodeEntry.get(1).getLogicTableName(), is("t_order_item"));
        assertThat(actualNodeEntry.get(1).getDataNodes().size(), is(1));
    }
    
    @Test
    void assertConvertDataNodesToLinesWithMultipleDataSource() {
        List<DataNode> dataNodes = Arrays.asList(new DataNode("ds_0", (String) null, "t_order_0"),
                new DataNode("ds_0", (String) null, "t_order_2"), new DataNode("ds_1", (String) null, "t_order_1"), new DataNode("ds_1", (String) null, "t_order_3"));
        List<JobDataNodeLine> jobDataNodeLines = JobDataNodeLineConvertUtils.convertDataNodesToLines(Collections.singletonMap("t_order", dataNodes));
        assertThat(jobDataNodeLines.size(), is(2));
        JobDataNodeEntry jobDataNodeEntry = jobDataNodeLines.get(0).getEntries().iterator().next();
        assertThat(jobDataNodeEntry.getDataNodes().stream().map(DataNode::getTableName).collect(Collectors.toList()), is(Arrays.asList("t_order_0", "t_order_2")));
        jobDataNodeEntry = jobDataNodeLines.get(1).getEntries().iterator().next();
        assertThat(jobDataNodeEntry.getDataNodes().stream().map(DataNode::getTableName).collect(Collectors.toList()), is(Arrays.asList("t_order_1", "t_order_3")));
    }
}
