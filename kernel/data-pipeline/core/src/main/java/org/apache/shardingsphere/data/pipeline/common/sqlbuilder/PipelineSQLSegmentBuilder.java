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

package org.apache.shardingsphere.data.pipeline.common.sqlbuilder;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;

/**
 * Pipeline SQL segment builder.
 */
@RequiredArgsConstructor
public final class PipelineSQLSegmentBuilder {
    
    private final DatabaseType databaseType;
    
    /**
     * Get escaped identifier.
     *
     * @param identifier identifier to be processed
     * @return escaped identifier
     */
    public String getEscapedIdentifier(final String identifier) {
        return databaseType.isReservedWord(identifier) ? databaseType.getQuoteCharacter().wrap(identifier) : identifier;
    }
    
    /**
     * Get qualified table name.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @return qualified table name
     */
    public String getQualifiedTableName(final String schemaName, final String tableName) {
        StringBuilder result = new StringBuilder();
        if (databaseType.isSchemaAvailable() && !Strings.isNullOrEmpty(schemaName)) {
            result.append(getEscapedIdentifier(schemaName)).append('.');
        }
        result.append(getEscapedIdentifier(tableName));
        return result.toString();
    }
}
