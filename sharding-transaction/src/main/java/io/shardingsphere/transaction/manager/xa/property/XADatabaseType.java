/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.transaction.manager.xa.property;

import io.shardingsphere.transaction.manager.xa.convert.PoolType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * XA database type enum.
 *
 * @author zhaojun
 */
@RequiredArgsConstructor
@Getter
public enum XADatabaseType {
    
    H2("H2"),
    
    MySQL("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource"),
    
    Oracle("Oracle"),
    
    SQLServer("Microsoft SQL Server"),
    
    PostgreSQL("PostgreSQL");
    
    private final String className;
    
    /**
     * Find pool type by class name.
     *
     * @param className class name
     * @return pool type
     */
    public static XADatabaseType find(final String className) {
        for (XADatabaseType each : XADatabaseType.values()) {
            if (className.equals(each.className)) {
                return each;
            }
        }
        throw new UnsupportedOperationException(String.format("Cannot find XA database type of [%s]", className));
    }
}
