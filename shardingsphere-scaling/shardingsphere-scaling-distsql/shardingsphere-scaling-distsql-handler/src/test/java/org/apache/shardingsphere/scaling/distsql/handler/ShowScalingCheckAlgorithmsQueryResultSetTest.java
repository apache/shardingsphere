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

package org.apache.shardingsphere.scaling.distsql.handler;

import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;
import org.apache.shardingsphere.scaling.distsql.statement.ShowScalingCheckAlgorithmsStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShowScalingCheckAlgorithmsQueryResultSetTest {
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    @Mock
    private ShowScalingCheckAlgorithmsStatement showScalingCheckAlgorithmsStatement;
    
    @Before
    public void before() {
        ModeConfiguration modeConfiguration = mock(ModeConfiguration.class);
        when(modeConfiguration.getType()).thenReturn("Cluster");
        ServerConfiguration serverConfiguration = mock(ServerConfiguration.class);
        when(serverConfiguration.getModeConfiguration()).thenReturn(modeConfiguration);
        when(serverConfiguration.getWorkerThread()).thenReturn(1);
        ScalingContext.getInstance().init(serverConfiguration);
    }
    
    @Test
    public void assertGetRowData() {
        DistSQLResultSet resultSet = new ShowScalingCheckAlgorithmsQueryResultSet();
        resultSet.init(shardingSphereMetaData, showScalingCheckAlgorithmsStatement);
        Collection<Object> actual = resultSet.getRowData();
        assertThat(actual.size(), is(4));
        Iterator<Object> rowData = actual.iterator();
        assertThat(rowData.next(), is("DEFAULT"));
        assertThat(rowData.next(), is("Default implementation with CRC32 of all records."));
        assertThat(rowData.next(), is("MySQL,PostgreSQL"));
        assertThat(rowData.next(), is("ShardingSphere"));
    }
}
