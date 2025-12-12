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

package org.apache.shardingsphere.database.connector.core.metadata.data.loader.type;

import org.apache.shardingsphere.database.connector.core.metadata.data.model.IndexMetaData;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IndexMetaDataLoaderTest {
    
    @Test
    void assertLoad() throws SQLException {
        Collection<IndexMetaData> actual = IndexMetaDataLoader.load(mockConnection(), "tbl");
        assertThat(actual.size(), is(1));
        IndexMetaData indexMetaData = actual.iterator().next();
        assertThat(indexMetaData.getName(), is("my_index"));
    }
    
    private Connection mockConnection() throws SQLException {
        Connection result = mock(Connection.class, RETURNS_DEEP_STUBS);
        ResultSet resultSet = mockResultSet();
        when(result.getMetaData().getIndexInfo("catalog", null, "tbl", false, false)).thenReturn(resultSet);
        when(result.getCatalog()).thenReturn("catalog");
        return result;
    }
    
    private ResultSet mockResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, false);
        when(result.getString("INDEX_NAME")).thenReturn("my_index");
        return result;
    }
}
