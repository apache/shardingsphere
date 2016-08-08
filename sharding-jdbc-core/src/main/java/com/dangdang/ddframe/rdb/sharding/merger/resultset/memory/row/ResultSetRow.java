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

package com.dangdang.ddframe.rdb.sharding.merger.resultset.memory.row;

/**
 * 结果集数据行接口.
 * 
 * <p>每个数据行表示结果集的一行数据.</p>
 * 
 * @author zhangliang
 */
public interface ResultSetRow {
    
    /**
     * 设置数据行数据.
     * 
     * @param columnIndex 列索引, 与JDBC保持一致, 从1开始计数
     * @param value 数据行数据
     */
    void setCell(int columnIndex, Object value);
    
    /**
     * 通过列索引访问数据行数据.
     * 
     * @param columnIndex 列索引, 与JDBC保持一致, 从1开始计数
     * @return 数据行数据
     */
    Object getCell(int columnIndex);
    
    /**
     * 判断列索引是否在数据行范围.
     * 
     * @param columnIndex 列索引, 与JDBC保持一致, 从1开始计数
     * @return 列索引是否在数据行范围
     */
    boolean inRange(int columnIndex);
}
