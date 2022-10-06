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

package org.apache.shardingsphere.data.pipeline.api.datanode;

import org.apache.shardingsphere.infra.datanode.DataNode;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;

public final class JobDataNodeEntryTest {
    
    @Test
    public void assertSerialization() {
        String text = "t_order:ds_0.t_order_0,ds_0.t_order_1";
        JobDataNodeEntry actual = JobDataNodeEntry.unmarshal(text);
        assertNotNull(actual);
        assertThat(actual.marshal(), is(text));
        assertThat(actual.getLogicTableName(), is("t_order"));
        List<DataNode> dataNodes = actual.getDataNodes();
        assertNotNull(dataNodes);
        assertThat(dataNodes.size(), is(2));
        assertThat(dataNodes.get(0).format(), is("ds_0.t_order_0"));
        assertThat(dataNodes.get(1).format(), is("ds_0.t_order_1"));
    }
}
