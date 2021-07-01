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

package org.apache.shardingsphere.infra.binder.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.SchemaAvailable;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.SchemaSegment;

import java.util.Optional;

/**
 * SQL statement context utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLStatementContextUtil {
    
    /**
     * Get schema name.
     *
     * @param sqlStatementContext SQL statement context
     * @return schema name
     */
    public static String getSchemaName(final SQLStatementContext<?> sqlStatementContext) {
        Optional<SchemaSegment> schema = sqlStatementContext instanceof SchemaAvailable ? ((SchemaAvailable) sqlStatementContext).getSchemas().stream().findFirst() : Optional.empty();
        return schema.map(each -> each.getIdentifier().getValue()).orElse(null);
    }
}
