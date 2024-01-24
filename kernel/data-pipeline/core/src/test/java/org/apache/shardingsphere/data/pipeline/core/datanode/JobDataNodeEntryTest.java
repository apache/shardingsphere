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

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JobDataNodeEntryTest {
    
    @Test
    void assertMarshal() {
        String actual = new JobDataNodeEntry("t_order", Arrays.asList(new DataNode("ds_0.t_order_0"), new DataNode("ds_0.t_order_1"))).marshal();
        String expected = "t_order:ds_0.t_order_0,ds_0.t_order_1";
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertUnmarshalWithSchema() {
        JobDataNodeEntry actual = JobDataNodeEntry.unmarshal("t_order:ds_0.public.t_order_0,ds_1.test.t_order_1");
        assertThat(actual.getLogicTableName(), is("t_order"));
        assertNotNull(actual.getDataNodes());
        assertThat(actual.getDataNodes().size(), is(2));
        DataNode first = actual.getDataNodes().get(0);
        assertThat(first.getDataSourceName(), is("ds_0"));
        assertThat(first.getSchemaName(), is("public"));
        assertThat(first.getTableName(), is("t_order_0"));
        DataNode second = actual.getDataNodes().get(1);
        assertThat(second.getDataSourceName(), is("ds_1"));
        assertThat(second.getSchemaName(), is("test"));
        assertThat(second.getTableName(), is("t_order_1"));
    }
}
