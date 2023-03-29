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

import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowComputeNodesStatement;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.state.instance.InstanceStateContext;
import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepositoryConfiguration;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowComputeNodeInfoExecutorTest {
    
    @Test
    void assertGetColumns() {
        ShowComputeNodeInfoExecutor executor = new ShowComputeNodeInfoExecutor();
        Collection<String> actual = executor.getColumnNames();
        assertThat(actual.size(), is(8));
        Iterator<String> iterator = actual.iterator();
        assertThat(iterator.next(), is("instance_id"));
        assertThat(iterator.next(), is("host"));
        assertThat(iterator.next(), is("port"));
        assertThat(iterator.next(), is("status"));
        assertThat(iterator.next(), is("mode_type"));
        assertThat(iterator.next(), is("worker_id"));
        assertThat(iterator.next(), is("labels"));
        assertThat(iterator.next(), is("version"));
    }
    
    @Test
    void assertExecute() throws SQLException {
        ShowComputeNodesExecutor executor = new ShowComputeNodesExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(createInstanceContext(), mock(ShowComputeNodesStatement.class));
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(1), is("127.0.0.1@3309"));
        assertThat(row.getCell(2), is("127.0.0.1"));
        assertThat(row.getCell(3), is(3309));
        assertThat(row.getCell(4), is("OK"));
        assertThat(row.getCell(5), is("Standalone"));
        assertThat(row.getCell(6), is(0));
        assertThat(row.getCell(7), is(""));
        assertThat(row.getCell(8), is("foo_version"));
    }
    
    private InstanceContext createInstanceContext() {
        InstanceContext result = mock(InstanceContext.class, RETURNS_DEEP_STUBS);
        when(result.getInstance().getMetaData()).thenReturn(new ProxyInstanceMetaData("127.0.0.1@3309", "127.0.0.1@3309", "foo_version"));
        when(result.getInstance().getState()).thenReturn(new InstanceStateContext());
        when(result.getModeConfiguration()).thenReturn(new ModeConfiguration("Standalone", new StandalonePersistRepositoryConfiguration("H2", new Properties())));
        when(result.getInstance().getWorkerId()).thenReturn(0);
        return result;
    }
}
