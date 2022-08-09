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

package org.apache.shardingsphere.scaling.distsql.handler.query;

import org.apache.shardingsphere.data.pipeline.core.api.GovernanceRepositoryAPI;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.scaling.distsql.statement.ShowScalingCheckAlgorithmsStatement;
import org.apache.shardingsphere.scaling.distsql.util.PipelineContextUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mockStatic;

@RunWith(MockitoJUnitRunner.class)
public final class ShowScalingCheckAlgorithmsQueryResultSetTest {
    
    private static MockedStatic<PipelineAPIFactory> pipelineAPIFactory;
    
    @Mock
    private static GovernanceRepositoryAPI governanceRepositoryAPI;
    
    @Mock
    private ShardingSphereDatabase database;
    
    @Mock
    private ShowScalingCheckAlgorithmsStatement showScalingCheckAlgorithmsStatement;
    
    @BeforeClass
    public static void beforeClass() {
        PipelineContextUtil.mockModeConfig();
        pipelineAPIFactory = mockStatic(PipelineAPIFactory.class);
        pipelineAPIFactory.when(PipelineAPIFactory::getGovernanceRepositoryAPI).thenReturn(governanceRepositoryAPI);
    }
    
    @AfterClass
    public static void afterClass() {
        pipelineAPIFactory.close();
    }
    
    @Test
    public void assertGetRowData() {
        ShowScalingCheckAlgorithmsQueryResultSet resultSet = new ShowScalingCheckAlgorithmsQueryResultSet();
        resultSet.init(database, showScalingCheckAlgorithmsStatement);
        Collection<Object> algorithmTypes = new LinkedHashSet<>();
        while (resultSet.next()) {
            Collection<Object> actual = resultSet.getRowData();
            assertThat(actual.size(), is(3));
            Iterator<Object> rowData = actual.iterator();
            algorithmTypes.add(rowData.next());
        }
        assertTrue(algorithmTypes.contains("DATA_MATCH"));
        assertTrue(algorithmTypes.contains("CRC32_MATCH"));
    }
}
