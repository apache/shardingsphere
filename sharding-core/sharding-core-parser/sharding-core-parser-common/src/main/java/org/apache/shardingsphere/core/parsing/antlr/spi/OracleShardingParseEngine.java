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

package org.apache.shardingsphere.core.parsing.antlr.spi;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.parsing.antlr.autogen.OracleStatementLexer;
import org.apache.shardingsphere.core.parsing.antlr.parser.impl.dialect.OracleParser;
import org.apache.shardingsphere.core.parsing.spi.ShardingParseEngine;

/**
 * Sharding parse engine for Oracle.
 *
 * @author zhangliang
 */
public final class OracleShardingParseEngine implements ShardingParseEngine {
    
    @Override
    public String getDatabaseType() {
        return DatabaseType.Oracle.name();
    }
    
    @Override
    public OracleParser createSQLParser(final String sql) {
        return new OracleParser(new CommonTokenStream(new OracleStatementLexer(CharStreams.fromString(sql))));
    }
}
