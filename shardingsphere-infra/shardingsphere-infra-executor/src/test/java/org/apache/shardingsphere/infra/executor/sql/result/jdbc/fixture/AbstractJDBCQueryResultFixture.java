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

package org.apache.shardingsphere.infra.executor.sql.result.jdbc.fixture;

import org.apache.shardingsphere.infra.executor.sql.result.jdbc.AbstractJDBCQueryResult;

import java.io.InputStream;
import java.sql.ResultSetMetaData;
import java.util.Calendar;
import java.util.Date;

import static org.mockito.Mockito.mock;

public final class AbstractJDBCQueryResultFixture extends AbstractJDBCQueryResult {
    
    public AbstractJDBCQueryResultFixture(final ResultSetMetaData resultSetMetaData) {
        super(resultSetMetaData);
    }
    
    @Override
    public boolean next() {
        return false;
    }
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) {
        return "";
    }
    
    @Override
    public Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) {
        return new Date(0L);
    }
    
    @Override
    public InputStream getInputStream(final int columnIndex, final String type) {
        return mock(InputStream.class);
    }
    
    @Override
    public boolean wasNull() {
        return false;
    }
}
