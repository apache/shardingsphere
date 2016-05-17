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

package com.dangdang.ddframe.rdb.sharding.merger.component;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 管道化组件接口.
 * 
 * @param <T> 前置组件类型
 * 
 * @author gaohongtao
 */
public interface ComponentResultSet<T> extends ResultSet {
    
    /**
     * 初始化管道组件.
     * 
     * @param preComponent 前置管道组件
     * @return 返回初始化完成的管道组件
     * @throws SQLException 访问组件可能抛出异常
     */
    ComponentResultSet init(T preComponent) throws SQLException;
}
