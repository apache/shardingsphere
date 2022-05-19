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

package org.apache.shardingsphere.data.pipeline.core.metadata.loader;

import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineIndexMetaData;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
// TODO use H2 to do real test
public final class PipelineTableMetaDataLoaderTest {
    
    private static final String TEST_CATALOG = "catalog";
    
    private static final String ORDINAL_POSITION = "ORDINAL_POSITION";
    
    private static final String COLUMN_NAME = "COLUMN_NAME";
    
    private static final String DATA_TYPE = "DATA_TYPE";
    
    private static final String TABLE_NAME = "TABLE_NAME";
    
    private static final String INDEX_NAME = "INDEX_NAME";
    
    private static final String TEST_TABLE = "test";
    
    private static final String TEST_INDEX = "idx_test";
    
    private PipelineDataSourceWrapper dataSource;
    
    @Mock
    private Connection connection;
    
    @Mock
    private DatabaseMetaData databaseMetaData;
    
    @Mock
    private ResultSet primaryKeyResultSet;
    
    @Mock
    private ResultSet columnMetaDataResultSet;
    
    @Mock
    private ResultSet indexInfoResultSet;
    
    @Before
    public void setUp() throws SQLException {
        DataSource rawDataSource = mock(DataSource.class);
        dataSource = new PipelineDataSourceWrapper(rawDataSource, new H2DatabaseType());
        when(rawDataSource.getConnection()).thenReturn(connection);
        when(connection.getCatalog()).thenReturn(TEST_CATALOG);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getColumns(TEST_CATALOG, null, TEST_TABLE, "%")).thenReturn(columnMetaDataResultSet);
        when(databaseMetaData.getPrimaryKeys(TEST_CATALOG, null, TEST_TABLE)).thenReturn(primaryKeyResultSet);
        when(primaryKeyResultSet.next()).thenReturn(true, false);
        when(primaryKeyResultSet.getString(COLUMN_NAME)).thenReturn("id");
        when(columnMetaDataResultSet.next()).thenReturn(true, true, true, false);
        when(columnMetaDataResultSet.getString(TABLE_NAME)).thenReturn(TEST_TABLE);
        when(columnMetaDataResultSet.getInt(ORDINAL_POSITION)).thenReturn(1, 2, 3);
        when(columnMetaDataResultSet.getString(COLUMN_NAME)).thenReturn("id", "name", "age");
        when(columnMetaDataResultSet.getInt(DATA_TYPE)).thenReturn(Types.BIGINT, Types.VARCHAR, Types.INTEGER);
        when(databaseMetaData.getIndexInfo(TEST_CATALOG, null, TEST_TABLE, true, false)).thenReturn(indexInfoResultSet);
        when(indexInfoResultSet.next()).thenReturn(true, true, false);
        when(indexInfoResultSet.getString(INDEX_NAME)).thenReturn(TEST_INDEX);
        when(indexInfoResultSet.getString(COLUMN_NAME)).thenReturn("name", "id");
        when(indexInfoResultSet.getShort(ORDINAL_POSITION)).thenReturn((short) 2, (short) 1);
    }
    
    @Test
    public void assertGetTableMetaData() {
        PipelineTableMetaDataLoader metaDataLoader = new PipelineTableMetaDataLoader(dataSource);
        PipelineTableMetaData tableMetaData = metaDataLoader.getTableMetaData(null, TEST_TABLE);
        assertColumnMetaData(tableMetaData);
        assertPrimaryKeys(tableMetaData.getPrimaryKeyColumns());
        assertIndexMetaData(tableMetaData.getUniqueIndexes());
    }
    
    private void assertPrimaryKeys(final List<String> actual) {
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), is("id"));
    }
    
    private void assertColumnMetaData(final PipelineTableMetaData actual) {
        assertThat(actual.getColumnNames().size(), is(3));
        assertColumnMetaData(actual.getColumnMetaData(0), "id", Types.BIGINT);
        assertColumnMetaData(actual.getColumnMetaData(1), "name", Types.VARCHAR);
        assertColumnMetaData(actual.getColumnMetaData(2), "age", Types.INTEGER);
    }
    
    private void assertColumnMetaData(final PipelineColumnMetaData actual, final String expectedName, final int expectedType) {
        assertThat(actual.getName(), is(expectedName));
        assertThat(actual.getDataType(), is(expectedType));
    }
    
    private void assertIndexMetaData(final Collection<PipelineIndexMetaData> actualUniqueIndexes) {
        assertThat(actualUniqueIndexes.size(), is(1));
        PipelineIndexMetaData actualIndexMetaData = actualUniqueIndexes.iterator().next();
        assertThat(actualIndexMetaData.getName(), is(TEST_INDEX));
        assertThat(actualIndexMetaData.getColumns().size(), is(2));
        assertThat(actualIndexMetaData.getColumns().get(0).getName(), is("id"));
        assertThat(actualIndexMetaData.getColumns().get(1).getName(), is("name"));
    }
    
    @Test(expected = RuntimeException.class)
    public void assertGetTableMetaDataFailure() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException(""));
        new PipelineTableMetaDataLoader(dataSource).getTableMetaData(null, TEST_TABLE);
    }
}
