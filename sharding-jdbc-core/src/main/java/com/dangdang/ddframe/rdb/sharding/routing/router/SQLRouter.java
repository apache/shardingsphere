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

package com.dangdang.ddframe.rdb.sharding.routing.router;

import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.SQLStatement;
import com.dangdang.ddframe.rdb.sharding.routing.SQLRouteResult;

import java.util.List;

/**
 * SQL router interface.
 * 
 * @author zhangiang
 */
public interface SQLRouter {
    
    /**
     * Parse SQL.
     * 
     * @param logicSQL logic SQL
     * @param parametersSize parameters size
     * @return parse result
     */
    SQLStatement parse(String logicSQL, int parametersSize);
    
    /**
     * Route SQL.
     * 
     * @param logicSQL logic SQL
     * @param sqlStatement SQL statement
     * @param parameters parameters
     * @return parse result
     */
    SQLRouteResult route(String logicSQL, List<Object> parameters, SQLStatement sqlStatement);
}
