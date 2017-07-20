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

package com.dangdang.ddframe.rdb.sharding.merger;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.Calendar;

/**
 * 归并结果集接口.
 *
 * @author zhangliang
 */
public interface ResultSetMerger {
    
    /**
     * 遍历下一个结果数据.
     * 
     * @return 是否有下一个结果数据
     * @throws SQLException SQL异常
     */
    boolean next()throws SQLException;
    
    /**
     * 获取值.
     *
     * @param columnIndex 列索引
     * @param type 值类型
     * @return 值
     * @throws SQLException SQL异常
     */
    Object getValue(final int columnIndex, final Class<?> type) throws SQLException;
    
    /**
     * 获取值.
     *
     * @param columnLabel 列标签
     * @param type 值类型
     * @return 值
     * @throws SQLException SQL异常
     */
    Object getValue(final String columnLabel, final Class<?> type) throws SQLException;
    
    /**
     * 获取值.
     *
     * @param columnIndex 列索引
     * @param type 值类型
     * @param calendar 日期
     * @return 值
     * @throws SQLException SQL异常
     */
    Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) throws SQLException;
    
    /**
     * 获取值.
     *
     * @param columnLabel 列标签
     * @param type 值类型
     * @param calendar 日期
     * @return 值
     * @throws SQLException SQL异常
     */
    Object getCalendarValue(final String columnLabel, final Class<?> type, final Calendar calendar) throws SQLException;
    
    /**
     * 获取值.
     *
     * @param columnIndex 列索引
     * @param type 类型
     * @return 值
     * @throws SQLException SQL异常
     */
    InputStream getInputStream(final int columnIndex, final String type) throws SQLException;
    
    /**
     * 获取值.
     *
     * @param columnLabel 列标签
     * @param type 类型
     * @return 值
     * @throws SQLException SQL异常
     */
    InputStream getInputStream(final String columnLabel, final String type) throws SQLException;
}
