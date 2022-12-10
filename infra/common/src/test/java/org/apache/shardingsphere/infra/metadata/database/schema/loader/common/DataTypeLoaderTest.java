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

package org.apache.shardingsphere.infra.metadata.database.schema.loader.common;

import org.apache.shardingsphere.infra.fixture.InfraDatabaseTypeFixture;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.spi.DataTypeLoaderFactory;
import org.junit.Test;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DataTypeLoaderTest {
    
    @Test
    public void assertLoad() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("TYPE_NAME")).thenReturn("int", "varchar");
        when(resultSet.getInt("DATA_TYPE")).thenReturn(Types.INTEGER, Types.VARCHAR);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(databaseMetaData.getTypeInfo()).thenReturn(resultSet);
        Map<String, Integer> actual = DataTypeLoaderFactory.getInstance(new InfraDatabaseTypeFixture()).load(databaseMetaData);
        assertThat(actual.size(), is(2));
        assertThat(actual.get("INT"), is(Types.INTEGER));
        assertThat(actual.get("VARCHAR"), is(Types.VARCHAR));
    }
}
