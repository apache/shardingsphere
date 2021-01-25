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

package org.apache.shardingsphere.infra.merge.result.impl.decorator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.merge.result.MergedResult;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.Calendar;

/**
 * Decorator merged result.
 */
@RequiredArgsConstructor
@Getter
public abstract class DecoratorMergedResult implements MergedResult {
    
    private final MergedResult mergedResult;
    
    @Override
    public final Object getValue(final int columnIndex, final Class<?> type) throws SQLException {
        return mergedResult.getValue(columnIndex, type);
    }
    
    @Override
    public final Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) throws SQLException {
        return mergedResult.getCalendarValue(columnIndex, type, calendar);
    }
    
    @Override
    public final InputStream getInputStream(final int columnIndex, final String type) throws SQLException {
        return mergedResult.getInputStream(columnIndex, type);
    }
    
    @Override
    public final boolean wasNull() throws SQLException {
        return mergedResult.wasNull();
    }
}
