/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.rewrite;

import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.optimize.statement.SQLStatementContext;

import java.util.List;

/**
 * SQL rewrite decorator.
 *
 * @author zhangliang
 */
public interface SQLRewriteDecorator {
    
    /**
     * Decorate SQL rewrite engine.
     *
     * @param sqlRewriteEngine SQL rewrite engine to be decorated
     * @param tableMetas table metas
     * @param sqlStatementContext SQL statement context
     * @param parameters SQL parameters
     */
    void decorate(SQLRewriteEngine sqlRewriteEngine, TableMetas tableMetas, SQLStatementContext sqlStatementContext, List<Object> parameters);
}
