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

import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.SQLContext;
import com.dangdang.ddframe.rdb.sharding.routing.SQLRouteResult;

import java.util.List;

/**
 * SQL路由器.
 * 
 * @author zhangiang
 */
public interface SQLRouter {
    
    /**
     * SQL解析.
     * 
     * @param logicSQL 逻辑SQL
     * @param parametersSize 参数个数
     * @return 解析结果
     */
    SQLContext parse(String logicSQL, int parametersSize);
    
    /**
     * SQL路由.
     * 
     * @param logicSQL 逻辑SQL
     * @param sqlContext 解析结果
     * @param parameters 参数
     * @return 路由结果
     */
    SQLRouteResult route(String logicSQL, List<Object> parameters, SQLContext sqlContext);
}
