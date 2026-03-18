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

package org.apache.shardingsphere.distsql.parser.engine.api;

import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.apache.shardingsphere.distsql.parser.core.featured.DistSQLParserEngine;
import org.apache.shardingsphere.distsql.parser.core.kernel.KernelDistSQLStatementParserEngine;
import org.apache.shardingsphere.distsql.parser.core.utility.UtilityDistSQLStatementParserEngine;
import org.apache.shardingsphere.sql.parser.engine.exception.SQLParsingException;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;

/**
 * DistSQL statement parser engine.
 */
public final class DistSQLStatementParserEngine {
    
    /**
     * Parse SQL.
     *
     * @param sql SQL to be parsed
     * @return SQL statement
     */
    public SQLStatement parse(final String sql) {
        try {
            return new KernelDistSQLStatementParserEngine().parse(sql);
        } catch (final ParseCancellationException | SQLParsingException ignored) {
            try {
                return new DistSQLParserEngine().parse(sql);
            } catch (final SQLParsingException ignoredToo) {
                return new UtilityDistSQLStatementParserEngine().parse(sql);
            }
        }
    }
}
