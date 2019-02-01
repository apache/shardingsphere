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

package io.shardingsphere.core.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * DataSource pool type enum.
 *
 * @author zhaojun
 */
@RequiredArgsConstructor
@Getter
public enum PoolType {
    
    HIKARI("com.zaxxer.hikari.HikariDataSource"),
    DRUID("com.alibaba.druid.pool.DruidDataSource"),
    DBCP2("org.apache.commons.dbcp2.BasicDataSource"),
    DBCP2_TOMCAT("org.apache.tomcat.dbcp.dbcp2.BasicDataSource"),
    UNKNOWN("");
    
    private final String className;
    
    /**
     * Find pool type by class name.
     *
     * @param className class name
     * @return pool type
     */
    public static PoolType find(final String className) {
        for (PoolType each : PoolType.values()) {
            if (className.equals(each.className)) {
                return each;
            }
        }
        return UNKNOWN;
    }
}

