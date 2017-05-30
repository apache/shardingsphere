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

package com.dangdang.ddframe.rdb.sharding.routing.type.database;

import com.dangdang.ddframe.rdb.sharding.rewrite.SQLBuilder;
import com.dangdang.ddframe.rdb.sharding.routing.RoutingResult;
import com.dangdang.ddframe.rdb.sharding.routing.SQLExecutionUnit;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

/**
 * 库路由结果.
 * 
 * @author gaohongtao
 */
@RequiredArgsConstructor
public final class DatabaseRoutingResult implements RoutingResult {
    
    private final Collection<String> routedDatabaseNames;
    
    @Override
    public Collection<SQLExecutionUnit> getSQLExecutionUnits(final SQLBuilder sqlBuilder) {
        return Collections2.transform(routedDatabaseNames, new Function<String, SQLExecutionUnit>() {
            
            @Override
            public SQLExecutionUnit apply(final String input) {
                return new SQLExecutionUnit(input, sqlBuilder);
            }
        });
    }
}
