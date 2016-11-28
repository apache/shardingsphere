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

package com.dangdang.ddframe.rdb.sharding.util;

import java.sql.SQLException;

/**
 * 可抛出{@linkplain java.sql.SQLException}的方法.
 * 
 * @author gaohongtao.
 */
public interface ThrowableSQLExceptionMethod<T> {
    
    /**
     * 调用对象中的方法可能抛出{@linkplain java.sql.SQLException}.
     * 
     * @param object 调用方法的对象
     * @throws SQLException 访问数据库错误可以抛出该异常
     */
    void apply(T object) throws SQLException;
}
