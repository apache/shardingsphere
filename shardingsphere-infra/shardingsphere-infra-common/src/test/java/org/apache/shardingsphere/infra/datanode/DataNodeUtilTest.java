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

package org.apache.shardingsphere.infra.datanode;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class DataNodeUtilTest {

    @Test
    public void assertGetDataNodeGroups() {
        Map<String, List<DataNode>> dataNodeGroups = DataNodeUtil.getDataNodeGroups(getDataNodes());
        assertThat(dataNodeGroups.size(), is(3));
        assertTrue(dataNodeGroups.containsKey("db0"));
        dataNodeGroups.keySet().containsAll(Lists.newArrayList("db0", "db1", "db2"));
        assertThat(dataNodeGroups.get("db0"), hasItems(new DataNode("db0.table_1"), new DataNode("db0.table_2")));
        assertThat(dataNodeGroups.get("db0").get(0).getDataSourceName(), is("db0"));
        assertThat(dataNodeGroups.get("db0").get(1).getDataSourceName(), is("db0"));
        assertThat(dataNodeGroups.get("db1"), hasItems(new DataNode("db1.table_3")));
        assertThat(dataNodeGroups.get("db2"), hasItems(new DataNode("db2.table_4")));
    }

    private Collection<DataNode> getDataNodes() {
        List<DataNode> dataNodes = new ArrayList<>(3);
        dataNodes.add(new DataNode("db0.table_1"));
        dataNodes.add(new DataNode("db0.table_2"));
        dataNodes.add(new DataNode("db1.table_3"));
        dataNodes.add(new DataNode("db2.table_4"));
        return dataNodes;
    }
}
