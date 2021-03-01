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

package org.apache.shardingsphere.infra.rule;

import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datanode.DataNodeUtil;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class DataNodeUtilTest {
    
    @Test
    public void assertGetDataNodeGroups() {
        Map<String, List<DataNode>> expected = new LinkedHashMap<>(2, 1);
        expected.put("ds_0", Arrays.asList(new DataNode("ds_0.tbl_0"), new DataNode("ds_0.tbl_1")));
        expected.put("ds_1", Arrays.asList(new DataNode("ds_1.tbl_0"), new DataNode("ds_1.tbl_1")));
        List<DataNode> dataNodes = new LinkedList<>();
        expected.values().forEach(dataNodes::addAll);
        Map<String, List<DataNode>> actual = DataNodeUtil.getDataNodeGroups(dataNodes);
        assertThat(actual, is(expected));
    }
}
