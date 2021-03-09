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

import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class DataNodeTest {

    @Test
    public void assertNewDateNodeWithPointDelimiter() {
        DataNode dataNode = new DataNode("db.table_1");
        assertThat(dataNode.getDataSourceName(), is("db"));
        assertThat(dataNode.getTableName(), is("table_1"));
    }

    @Test(expected = ShardingSphereConfigurationException.class)
    public void assertNewDateNodeWithoutDelimiter() {
        DataNode dataNode = new DataNode("db");
    }

    @Test(expected = ShardingSphereConfigurationException.class)
    public void assertNewDateNodeWithInvalidDelimiter() {
        DataNode dataNode = new DataNode("db,table_1");
    }

    @Test(expected = ShardingSphereConfigurationException.class)
    public void assertNewDateNodeWithGreaterThenOneValidDelimiter() {
        DataNode dataNode = new DataNode("schema.db.table_1");
    }

    @Test
    public void assertNewDateNodeWithNoTableNameOrDbName() {
        DataNode dataNodeWithNoTableName = new DataNode("db.");
        assertThat(dataNodeWithNoTableName.getDataSourceName(), is("db"));
        assertThat(dataNodeWithNoTableName.getTableName(), is(""));
        DataNode dataNodeWithNoDbName = new DataNode(".table");
        assertThat(dataNodeWithNoDbName.getDataSourceName(), is(""));
        assertThat(dataNodeWithNoDbName.getTableName(), is("table"));
    }

    @Test
    public void assertDateNodeEqualsAndHashCode() {
        DataNode dataNode1 = new DataNode("db.table_1");
        DataNode dataNode2 = new DataNode("db.table_1");
        assertThat(dataNode1, is(dataNode2));
        assertTrue(dataNode1.equals(dataNode2));
        assertTrue(dataNode1.hashCode() == dataNode2.hashCode());
        DataNode dataNode3 = new DataNode("db.table_3");
        assertFalse(dataNode1.equals(dataNode3));
    }

    @Test
    public void assertDateNodeToString() {
        DataNode dataNode1 = new DataNode("db.table_1");
        assertThat(dataNode1.toString(), is("DataNode(dataSourceName=db, tableName=table_1)"));
    }
}
