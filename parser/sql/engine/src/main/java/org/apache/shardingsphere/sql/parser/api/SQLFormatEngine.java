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

package org.apache.shardingsphere.sql.parser.api;

import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.api.visitor.format.SQLFormatVisitor;

import java.util.Properties;

/**
 * SQL format engine.
 */
@RequiredArgsConstructor
public final class SQLFormatEngine {
    
    private final String databaseType;
    
    private final CacheOption cacheOption;
    
    /**
     * Format SQL.
     * 
     * @param sql SQL to be formatted
     * @param useCache whether use cache
     * @param props properties
     * @return formatted SQL
     */
    @SuppressWarnings("unchecked")
    public String format(final String sql, final boolean useCache, final Properties props) {
        ParseTree parseTree = new SQLParserEngine(databaseType, cacheOption).parse(sql, useCache).getRootNode();
        return ((ParseTreeVisitor<String>) TypedSPILoader.getService(SQLFormatVisitor.class, databaseType, props)).visit(parseTree);
    }
}
