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

package org.apache.shardingsphere.core.parsing.parser.sql.dml.insert;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parsing.lexer.LexerEngine;
import org.apache.shardingsphere.core.parsing.parser.dialect.mysql.sql.MySQLInsertParser;
import org.apache.shardingsphere.core.parsing.parser.dialect.oracle.sql.OracleInsertParser;
import org.apache.shardingsphere.core.parsing.parser.dialect.postgresql.sql.PostgreSQLInsertParser;
import org.apache.shardingsphere.core.parsing.parser.dialect.sqlserver.sql.SQLServerInsertParser;
import org.apache.shardingsphere.core.rule.ShardingRule;

/**
 * Insert parser factory.
 *
 * @author zhangliang
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InsertParserFactory {
    
    /**
     * Create insert parser instance.
     *
     * @param dbType database type
     * @param shardingRule databases and tables sharding rule
     * @param lexerEngine lexical analysis engine
     * @param shardingTableMetaData sharding meta data
     * @return insert parser instance
     */
    public static AbstractInsertParser newInstance(final DatabaseType dbType, final ShardingRule shardingRule, final LexerEngine lexerEngine, final ShardingTableMetaData shardingTableMetaData) {
        switch (dbType) {
            case H2:
            case MySQL:
                return new MySQLInsertParser(shardingRule, lexerEngine, shardingTableMetaData);
            case Oracle:
                return new OracleInsertParser(shardingRule, lexerEngine, shardingTableMetaData);
            case SQLServer:
                return new SQLServerInsertParser(shardingRule, lexerEngine, shardingTableMetaData);
            case PostgreSQL:
                return new PostgreSQLInsertParser(shardingRule, lexerEngine, shardingTableMetaData);
            default:
                throw new UnsupportedOperationException(String.format("Cannot support database [%s].", dbType));
        }
    }
}
