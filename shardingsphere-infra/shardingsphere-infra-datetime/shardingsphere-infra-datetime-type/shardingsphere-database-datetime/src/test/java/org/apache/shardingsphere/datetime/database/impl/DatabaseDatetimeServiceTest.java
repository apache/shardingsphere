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

package org.apache.shardingsphere.datetime.database.impl;

import org.apache.shardingsphere.infra.datetime.DatetimeService;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DatabaseDatetimeServiceTest {

    private static DataSource dataSource;

    private static PreparedStatement preparedStatement;

    private static ResultSet resultSet;

    private static String sql = "SELECT NOW()";

    @BeforeClass
    public static void init() {
        dataSource = mock(DataSource.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
    }

    @Test
    public void assertMySQLDateTime() throws SQLException {
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when((Date) resultSet.getObject(1)).thenReturn(new Date());
        DatetimeService datetimeService = new DatabaseDatetimeService(dataSource, sql);
        Assert.assertFalse(datetimeService.isDefault());
        Assert.assertNotNull(datetimeService.getDatetime());
    }

    @Test
    public void assertNoExceptionInDateTimeService() throws SQLException {
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenThrow(new SQLException());
        DatetimeService datetimeService = new DatabaseDatetimeService(dataSource, sql);
        Assert.assertFalse(datetimeService.isDefault());
        Assert.assertNotNull(datetimeService.getDatetime());
    }
}
