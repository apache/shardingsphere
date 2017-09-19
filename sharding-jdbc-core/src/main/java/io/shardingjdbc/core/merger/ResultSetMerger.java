/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.merger;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.Calendar;

/**
 * ResultSet merger interface.
 *
 * @author zhangliang
 */
public interface ResultSetMerger {
    
    /**
     * iterate next data.
     * 
     * @return has next data
     * @throws SQLException SQL Exception
     */
    boolean next()throws SQLException;
    
    /**
     * Get data value.
     *
     * @param columnIndex column index
     * @param type class type of data value
     * @return data value
     * @throws SQLException SQL Exception
     */
    Object getValue(final int columnIndex, final Class<?> type) throws SQLException;
    
    /**
     * Get data value.
     *
     * @param columnLabel column label
     * @param type class type of data value
     * @return data value
     * @throws SQLException SQL Exception
     */
    Object getValue(final String columnLabel, final Class<?> type) throws SQLException;
    
    /**
     * Get calendar value.
     *
     * @param columnIndex column index
     * @param type class type of data value
     * @param calendar calendar
     * @return calendar value
     * @throws SQLException SQL Exception
     */
    Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) throws SQLException;
    
    /**
     * Get calendar value.
     *
     * @param columnLabel column label
     * @param type class type of data value
     * @param calendar calendar
     * @return calendar value
     * @throws SQLException SQL Exception
     */
    Object getCalendarValue(final String columnLabel, final Class<?> type, final Calendar calendar) throws SQLException;
    
    /**
     * Get InputStream.
     *
     * @param columnIndex column index
     * @param type class type of data value
     * @return InputStream
     * @throws SQLException SQL Exception
     */
    InputStream getInputStream(final int columnIndex, final String type) throws SQLException;
    
    /**
     * Get InputStream.
     *
     * @param columnLabel column label
     * @param type class type of data value
     * @return InputStream
     * @throws SQLException SQL Exception
     */
    InputStream getInputStream(final String columnLabel, final String type) throws SQLException;
    
    /**
     * Adjust ResultSet is null or not.
     * 
     * @return ResultSet is null or not
     * @throws SQLException SQL Exception
     */
    boolean wasNull() throws SQLException;
}
