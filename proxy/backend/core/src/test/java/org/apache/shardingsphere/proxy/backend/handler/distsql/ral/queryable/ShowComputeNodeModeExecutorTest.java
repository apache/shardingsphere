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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable;

import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowComputeNodeModeStatement;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowComputeNodeModeExecutorTest {
    
    @Test
    void assertExecute() throws SQLException {
        ShowComputeNodeModeExecutor executor = new ShowComputeNodeModeExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(createInstanceContext(), new ShowComputeNodeModeStatement());
        assertThat(actual.size(), is(1));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("Cluster"));
        assertThat(row.getCell(2), is("ZooKeeper"));
        assertThat(row.getCell(3), is("{\"key\":\"value1,value2\"}"));
    }
    
    @Test
    void assertGetColumnNames() {
        ShowComputeNodeModeExecutor executor = new ShowComputeNodeModeExecutor();
        Collection<String> columns = executor.getColumnNames();
        assertThat(columns.size(), is(3));
        Iterator<String> iterator = columns.iterator();
        assertThat(iterator.next(), is("type"));
        assertThat(iterator.next(), is("repository"));
        assertThat(iterator.next(), is("props"));
    }
    
    private InstanceContext createInstanceContext() {
        InstanceContext result = mock(InstanceContext.class, RETURNS_DEEP_STUBS);
        when(result.getInstance().getMetaData().getId()).thenReturn("127.0.0.1@3309");
        when(result.getModeConfiguration()).thenReturn(new ModeConfiguration("Cluster",
                new ClusterPersistRepositoryConfiguration("ZooKeeper", "governance_ds", "127.0.0.1:2181", PropertiesBuilder.build(new Property("key", "value1,value2")))));
        return result;
    }
}
