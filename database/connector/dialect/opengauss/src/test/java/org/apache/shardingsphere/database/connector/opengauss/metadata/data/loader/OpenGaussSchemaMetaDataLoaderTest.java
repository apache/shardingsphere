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

package org.apache.shardingsphere.database.connector.opengauss.metadata.data.loader;

import com.cedarsoftware.util.CaseInsensitiveSet;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.type.SchemaMetaDataLoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OpenGaussSchemaMetaDataLoaderTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataSource dataSource;
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "openGauss");
    
    @SuppressWarnings("JDBCResourceOpenedButNotSafelyClosed")
    @BeforeEach
    void setUp() throws SQLException {
        ResultSet tableResultSet = mockTableResultSet();
        when(dataSource.getConnection().getMetaData().getTables("catalog", "public", null, new String[]{"TABLE", "PARTITIONED TABLE", "VIEW", "SYSTEM TABLE", "SYSTEM VIEW"}))
                .thenReturn(tableResultSet);
        when(dataSource.getConnection().getCatalog()).thenReturn("catalog");
        when(dataSource.getConnection().getSchema()).thenReturn("public");
        ResultSet schemaResultSet = mockSchemaResultSet();
        when(dataSource.getConnection().getMetaData().getSchemas()).thenReturn(schemaResultSet);
    }
    
    private ResultSet mockTableResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, true, true, true, false);
        when(result.getString("TABLE_NAME")).thenReturn("tbl", "$tbl", "/tbl", "##tbl", "partitioned_tbl");
        return result;
    }
    
    private ResultSet mockSchemaResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, true, true, false);
        when(result.getString("TABLE_SCHEM")).thenReturn("information_schema", "public", "schema_1", "schema_2");
        return result;
    }
    
    @Test
    void assertLoadSchemaTableNames() throws SQLException {
        assertThat(new SchemaMetaDataLoader(databaseType).loadSchemaTableNames("foo_db", dataSource, Collections.emptyList()), is(createSchemaTableNames()));
    }
    
    private Map<String, Collection<String>> createSchemaTableNames() {
        Map<String, Collection<String>> result = new LinkedHashMap<>(3, 1F);
        result.put("public", new CaseInsensitiveSet<>(Arrays.asList("tbl", "partitioned_tbl")));
        result.put("schema_1", Collections.emptySet());
        result.put("schema_2", Collections.emptySet());
        return result;
    }
    
    @Test
    void assertLoadSchemaNames() throws SQLException {
        assertThat(new SchemaMetaDataLoader(databaseType).loadSchemaNames(dataSource.getConnection()), is(Arrays.asList("public", "schema_1", "schema_2")));
    }
}
