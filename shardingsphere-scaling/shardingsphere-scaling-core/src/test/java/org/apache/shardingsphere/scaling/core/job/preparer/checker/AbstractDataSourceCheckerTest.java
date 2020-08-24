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

package org.apache.shardingsphere.scaling.core.job.preparer.checker;

import org.apache.shardingsphere.scaling.core.exception.PrepareFailedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AbstractDataSourceCheckerTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    private AbstractDataSourceChecker dataSourceChecker;

    private Collection<DataSource> dataSources;

    @Before
    public void setUp() {
        dataSourceChecker = new AbstractDataSourceChecker() {
            @Override
            public void checkPrivilege(final Collection<? extends DataSource> dataSources) {
            }
            
            @Override
            public void checkVariable(final Collection<? extends DataSource> dataSources) {
            }
        };
        dataSources = new LinkedList<>();
        dataSources.add(dataSource);
    }

    @Test
    public void assertCheckConnection() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        dataSourceChecker.checkConnection(dataSources);
        verify(dataSource).getConnection();
    }

    @Test(expected = PrepareFailedException.class)
    public void assertCheckConnectionFailed() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException("error"));
        dataSourceChecker.checkConnection(dataSources);
    }
}
