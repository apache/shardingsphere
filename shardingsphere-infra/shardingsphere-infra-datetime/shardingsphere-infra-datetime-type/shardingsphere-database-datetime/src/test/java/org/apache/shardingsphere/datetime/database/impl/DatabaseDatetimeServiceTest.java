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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DatabaseDatetimeServiceTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataSource dataSource;
    
    private final String sql = "SELECT NOW()";
    
    @Test
    public void assertMySQLDateTime() throws SQLException {
        when(dataSource.getConnection().prepareStatement(sql).executeQuery().getObject(1)).thenReturn(new Date());
        DatetimeService datetimeService = new DatabaseDatetimeService(dataSource, sql);
        assertFalse(datetimeService.isDefault());
        assertNotNull(datetimeService.getDatetime());
    }
    
    @Test
    public void assertNoExceptionInDateTimeService() throws SQLException {
        when(dataSource.getConnection().prepareStatement(sql)).thenThrow(new SQLException());
        DatetimeService datetimeService = new DatabaseDatetimeService(dataSource, sql);
        assertFalse(datetimeService.isDefault());
        assertNotNull(datetimeService.getDatetime());
    }
}
