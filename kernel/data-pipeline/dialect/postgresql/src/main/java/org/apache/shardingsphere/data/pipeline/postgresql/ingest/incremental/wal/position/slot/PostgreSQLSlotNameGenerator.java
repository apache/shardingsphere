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

package org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.position.slot;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * PostgreSQL slot name generator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLSlotNameGenerator {
    
    private static final String SLOT_NAME_PREFIX = "pipeline";
    
    /**
     * Get unique slot name by connection.
     *
     * @param connection connection
     * @param slotNameSuffix slot name suffix
     * @return unique slot name
     * @throws SQLException failed when get catalog
     */
    public static String getUniqueSlotName(final Connection connection, final String slotNameSuffix) throws SQLException {
        String slotName = DigestUtils.md5Hex(String.join("_", connection.getCatalog(), slotNameSuffix).getBytes());
        return String.format("%s_%s", SLOT_NAME_PREFIX, slotName);
    }
}
