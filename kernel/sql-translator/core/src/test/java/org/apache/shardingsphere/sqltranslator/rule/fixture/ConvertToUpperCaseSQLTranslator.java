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

package org.apache.shardingsphere.sqltranslator.rule.fixture;

import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sqltranslator.spi.SQLTranslator;

import java.util.Locale;

public final class ConvertToUpperCaseSQLTranslator implements SQLTranslator {
    
    @Override
    public String translate(final String sql, final SQLStatement sqlStatement, final DatabaseType protocolType, final DatabaseType storageType) {
        return sql.toUpperCase(Locale.ROOT);
    }
    
    @Override
    public String getType() {
        return "CONVERT_TO_UPPER_CASE";
    }
}
