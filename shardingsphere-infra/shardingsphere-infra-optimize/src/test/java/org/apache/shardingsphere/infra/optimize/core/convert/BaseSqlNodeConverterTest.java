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

package org.apache.shardingsphere.infra.optimize.core.convert;

import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.validate.SqlConformanceEnum;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;

/**
 * Base sql node converter test.
 */
public abstract class BaseSqlNodeConverterTest {

    /**
     * parse by Calcite sql parser with default database dialect.
     * @param sql sql
     * @return Calcite SqlNode.
     */
    protected SqlNode parseByCalciteParser(final String sql) {
        SqlParser parser = SqlParser.create(sql);
        return realParse(parser);
    }

    /**
     * parse by Calcite sql parser with arbitrary database dialect.
     * @param sql sql
     * @param databaseType database type.
     * @return Calcite SqlNode.
     */
    protected SqlNode parseByCalciteParser(final String sql, final DatabaseType databaseType) {
        if (databaseType == null) {
            return parseByCalciteParser(sql);
        }
        SqlConformanceEnum sqlConformance = getConformance(databaseType);
        SqlParser.Config parserConfig = SqlParser.Config.DEFAULT.withConformance(sqlConformance);
        SqlParser parser = SqlParser.create(sql, parserConfig);
        return realParse(parser);
    }

    private SqlNode realParse(final SqlParser parser) {
        try {
            return parser.parseQuery();
        } catch (SqlParseException e) {
            throw new RuntimeException(e);
        }
    }

    private SqlConformanceEnum getConformance(final DatabaseType databaseType) {
        if (databaseType instanceof MySQLDatabaseType) {
            return SqlConformanceEnum.MYSQL_5;
        }
        return SqlConformanceEnum.DEFAULT;
    }
}
