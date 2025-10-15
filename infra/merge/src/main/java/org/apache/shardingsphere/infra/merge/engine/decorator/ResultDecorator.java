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

package org.apache.shardingsphere.infra.merge.engine.decorator;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import java.sql.SQLException;
import java.util.List;

/**
 * Result decorator.
 * 
 * @param <T> type of ShardingSphere rule
 */
public interface ResultDecorator<T extends ShardingSphereRule> {
    
    /**
     * Decorate merged result.
     *
     * @param mergedResult merged result
     * @param sqlStatementContext SQL statement context
     * @param parameters parameters
     * @param rule ShardingSphere rule
     * @return merged result
     * @throws SQLException SQL exception
     */
    MergedResult decorate(MergedResult mergedResult, SQLStatementContext sqlStatementContext, List<Object> parameters, T rule) throws SQLException;
}
