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

package org.apache.shardingsphere.readwritesplitting.route.qualified;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingDataSourceRule;

/**
 * Qualified data source router for readwrite-splitting.
 */
public interface QualifiedReadwriteSplittingDataSourceRouter {
    
    /**
     * Judge whether qualified to route.
     *
     * @param sqlStatementContext SQL statement context
     * @param rule readwrite splitting datasource rule
     * @param hintValueContext hint value context
     * 
     * @return qualified to route or not
     */
    boolean isQualified(SQLStatementContext sqlStatementContext, ReadwriteSplittingDataSourceRule rule, HintValueContext hintValueContext);
    
    /**
     * Route to data source.
     *
     * @param rule Readwrite-splitting data source rule
     * @return routed data source name
     */
    String route(ReadwriteSplittingDataSourceRule rule);
}
