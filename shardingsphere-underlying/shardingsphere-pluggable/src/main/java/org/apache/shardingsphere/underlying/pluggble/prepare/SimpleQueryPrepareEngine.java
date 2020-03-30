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

package org.apache.shardingsphere.underlying.pluggble.prepare;

import org.apache.shardingsphere.sql.parser.SQLParserEngine;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.underlying.common.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.underlying.common.rule.BaseRule;
import org.apache.shardingsphere.underlying.route.DataNodeRouter;
import org.apache.shardingsphere.underlying.route.context.RouteContext;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Prepare engine for simple query.
 * 
 * <pre>
 *     Simple query:  
 *       for JDBC is Statement; 
 *       for MyQL is COM_QUERY; 
 *       for PostgreSQL is Simple Query;
 * </pre>
 */
public final class SimpleQueryPrepareEngine extends BasePrepareEngine {
    
    public SimpleQueryPrepareEngine(final Collection<BaseRule> rules, final ConfigurationProperties properties, final ShardingSphereMetaData metaData, final SQLParserEngine sqlParserEngine) {
        super(rules, properties, metaData, sqlParserEngine);
    }
    
    @Override
    protected List<Object> cloneParameters(final List<Object> parameters) {
        return Collections.emptyList();
    }
    
    @Override
    protected RouteContext route(final DataNodeRouter dataNodeRouter, final String sql, final List<Object> parameters) {
        return dataNodeRouter.route(sql, Collections.emptyList(), false);
    }
}
