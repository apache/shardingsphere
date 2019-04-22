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

package org.apache.shardingsphere.core.parse.antlr.parser;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.parse.api.SQLParser;
import org.apache.shardingsphere.core.parse.spi.ShardingParseEngine;
import org.apache.shardingsphere.core.spi.NewInstanceServiceLoader;

/**
 * SQL parser factory.
 * 
 * @author duhongjun
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLParserFactory {
    
    static {
        NewInstanceServiceLoader.register(ShardingParseEngine.class);
    }
    
    /** 
     * New instance of SQL parser.
     * 
     * @param databaseType database type
     * @param sql SQL
     * @return SQL parser
     */
    public static SQLParser newInstance(final DatabaseType databaseType, final String sql) {
        for (ShardingParseEngine each : NewInstanceServiceLoader.newServiceInstances(ShardingParseEngine.class)) {
            if (DatabaseType.valueOf(each.getDatabaseType()) == databaseType) {
                return each.createSQLParser(sql);
            }
        }
        throw new UnsupportedOperationException(String.format("Cannot support database type '%s'", databaseType));
    }
}
