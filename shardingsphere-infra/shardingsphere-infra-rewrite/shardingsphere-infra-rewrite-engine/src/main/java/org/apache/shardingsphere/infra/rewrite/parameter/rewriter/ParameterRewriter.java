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

package org.apache.shardingsphere.infra.rewrite.parameter.rewriter;

import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.ParameterBuilder;

import java.util.List;

/**
 * Parameter rewriter.
 * 
 * @param <T> type of SQL statement context
 */
public interface ParameterRewriter<T extends SQLStatementContext> {
    
    /**
     * Judge whether need rewrite.
     *
     * @param sqlStatementContext SQL statement context
     * @return is need rewrite or not
     */
    boolean isNeedRewrite(SQLStatementContext sqlStatementContext);
    
    /**
     * Rewrite SQL parameters.
     * 
     * @param parameterBuilder parameter builder
     * @param sqlStatementContext SQL statement context
     * @param parameters SQL parameters
     */
    void rewrite(ParameterBuilder parameterBuilder, T sqlStatementContext, List<Object> parameters);
}
