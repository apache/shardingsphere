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
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class JobDataNodeLineTest {
    
    @Test
    void assertMarshal() {
        String actual = new JobDataNodeLine(Arrays.asList(
                new JobDataNodeEntry("t_order", Arrays.asList(new DataNode("ds_0.t_order_0"), new DataNode("ds_0.t_order_1"))),
                new JobDataNodeEntry("t_order_item", Arrays.asList(new DataNode("ds_0.t_order_item_0"), new DataNode("ds_0.t_order_item_1"))))).marshal();
        String expected = "t_order:ds_0.t_order_0,ds_0.t_order_1|t_order_item:ds_0.t_order_item_0,ds_0.t_order_item_1";
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertUnmarshal() {
        String expected = "t_order:ds_0.t_order_0,ds_0.t_order_1|t_order_item:ds_0.t_order_item_0,ds_0.t_order_item_1";
        JobDataNodeLine actual = JobDataNodeLine.unmarshal(expected);
        assertThat(actual.getEntries().size(), is(2));
        List<JobDataNodeEntry> entries = new ArrayList<>(actual.getEntries());
        assertThat(entries.get(0).getLogicTableName(), is("t_order"));
        assertThat(entries.get(1).getLogicTableName(), is("t_order_item"));
    }
}
