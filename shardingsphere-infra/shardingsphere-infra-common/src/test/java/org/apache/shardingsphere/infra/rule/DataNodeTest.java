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

import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class DataNodeTest {
    
    @Test
    public void assertNewValidDataNode() {
        DataNode dataNode = new DataNode("ds_0.tbl_0");
        assertThat(dataNode.getDataSourceName(), is("ds_0"));
        assertThat(dataNode.getTableName(), is("tbl_0"));
    }
    
    @Test(expected = ShardingSphereConfigurationException.class)
    public void assertNewInValidDataNodeWithoutDelimiter() {
        new DataNode("ds_0tbl_0");
    }
    
    @Test(expected = ShardingSphereConfigurationException.class)
    public void assertNewInValidDataNodeWithTwoDelimiters() {
        new DataNode("ds_0.tbl_0.tbl_1");
    }
    
    @Test(expected = ShardingSphereConfigurationException.class)
    public void assertNewValidDataNodeWithInvalidDelimiter() {
        new DataNode("ds_0,tbl_0");
    }
    
    @Test
    public void assertEquals() {
        DataNode dataNode = new DataNode("ds_0.tbl_0");
        assertThat(dataNode, is(new DataNode("ds_0.tbl_0")));
        assertThat(dataNode, is(dataNode));
        assertThat(dataNode, not(new DataNode("ds_0.tbl_1")));
        assertFalse(dataNode.equals(null));
        assertNotNull(dataNode);
    }
    
    @Test
    public void assertHashCode() {
        assertThat(new DataNode("ds_0.tbl_0").hashCode(), is(new DataNode("ds_0.tbl_0").hashCode()));
    }
    
    @Test
    public void assertToString() {
        assertThat(new DataNode("ds_0.tbl_0").toString(), is("DataNode(dataSourceName=ds_0, tableName=tbl_0)"));
    }
}
