/**
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

package com.dangdang.ddframe.rdb.sharding.constants;

import com.dangdang.ddframe.rdb.sharding.exception.DatabaseTypeUnsupportedException;

/**
 * 支持的数据库类型.
 * 
 * @author zhangliang
 */
public enum DatabaseType {
    
    H2, MySQL, Oracle, SQLServer, DB2;
    
    /**
     * 获取数据库类型枚举.
     * 
     * @param databaseProductName 数据库类型
     * @return 数据库类型枚举
     */
    public static DatabaseType valueFrom(final String databaseProductName) {
        try {
            return DatabaseType.valueOf(databaseProductName);
        } catch (final IllegalArgumentException ex) {
            throw new DatabaseTypeUnsupportedException(databaseProductName);
        }
    }
}
