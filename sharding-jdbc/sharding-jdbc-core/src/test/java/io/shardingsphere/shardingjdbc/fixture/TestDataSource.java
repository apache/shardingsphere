/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingjdbc.fixture;

import io.shardingsphere.shardingjdbc.jdbc.adapter.AbstractDataSourceAdapter;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import org.mockito.Mockito;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collections;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@Setter
@EqualsAndHashCode(callSuper = false)
public final class TestDataSource extends AbstractDataSourceAdapter {
    
    private final String name;
    
    private boolean throwExceptionWhenClosing;
    
    public TestDataSource(final String name) throws SQLException {
        super(Collections.singletonMap("test", getDataSource()));
        this.name = name;
    }
    
    private static DataSource getDataSource() throws SQLException {
        DataSource result = Mockito.mock(DataSource.class);
        Connection connection = Mockito.mock(Connection.class);
        DatabaseMetaData metaData = Mockito.mock(DatabaseMetaData.class);
        when(metaData.getDatabaseProductName()).thenReturn("H2");
        when(connection.getMetaData()).thenReturn(metaData);
        when(result.getConnection()).thenReturn(connection);
        return result;
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        Connection result = Mockito.mock(Connection.class);
        DatabaseMetaData metaData = Mockito.mock(DatabaseMetaData.class);
        when(metaData.getDatabaseProductName()).thenReturn("H2");
        when(result.getMetaData()).thenReturn(metaData);
        when(result.getMetaData().getURL()).thenReturn("jdbc:h2:mem:demo_ds;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        if (throwExceptionWhenClosing) {
            doThrow(SQLException.class).when(result).close();
        }
        return result;
    }
}
