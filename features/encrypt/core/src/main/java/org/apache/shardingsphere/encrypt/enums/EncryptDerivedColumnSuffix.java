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

package org.apache.shardingsphere.encrypt.enums;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContext;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

/**
 * Encrypt derived column suffix.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum EncryptDerivedColumnSuffix {
    
    CIPHER("_C"),
    ASSISTED_QUERY("_A"),
    LIKE_QUERY("_L");
    
    private final String suffix;
    
    /**
     * Get derived column name.
     *
     * @param columnName column name
     * @param identifierContext database identifier context
     * @return derived column name
     */
    public String getDerivedColumnName(final String columnName, final DatabaseIdentifierContext identifierContext) {
        return String.format("%s%s", columnName, identifierContext.normalizeStorage(IdentifierScope.COLUMN, new IdentifierValue(suffix)));
    }
}
