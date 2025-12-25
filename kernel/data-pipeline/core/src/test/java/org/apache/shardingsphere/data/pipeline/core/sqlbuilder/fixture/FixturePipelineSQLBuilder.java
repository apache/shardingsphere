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

package org.apache.shardingsphere.data.pipeline.core.sqlbuilder.fixture;

import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.dialect.DialectPipelineSQLBuilder;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public final class FixturePipelineSQLBuilder implements DialectPipelineSQLBuilder {
    
    @Override
    public String buildCheckEmptyTableSQL(final String qualifiedTableName) {
        return String.format("SELECT * FROM %s LIMIT 1", qualifiedTableName);
    }
    
    @Override
    public Optional<String> buildCRC32SQL(final String qualifiedTableName, final String columnName) {
        return Optional.of(String.format("SELECT CRC32(%s) FROM %s", columnName, qualifiedTableName));
    }
    
    @Override
    public String buildSplitByUniqueKeyRangedSubqueryClause(final String qualifiedTableName, final String uniqueKey, final boolean hasLowerValue) {
        return "";
    }
    
    @Override
    public Collection<String> buildCreateTableSQLs(final DataSource dataSource, final String schemaName, final String tableName) {
        return Collections.emptyList();
    }
    
    @Override
    public String wrapWithPageQuery(final String sql) {
        return sql + " LIMIT ?";
    }
    
    @Override
    public String getDatabaseType() {
        return "FIXTURE";
    }
}
