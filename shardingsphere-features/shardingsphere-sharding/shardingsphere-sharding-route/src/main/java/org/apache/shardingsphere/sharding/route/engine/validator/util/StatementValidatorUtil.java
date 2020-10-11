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

package org.apache.shardingsphere.sharding.route.engine.validator.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sharding.route.engine.exception.NoSuchTableException;
import org.apache.shardingsphere.sharding.route.engine.exception.TableExistsException;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;

import java.util.Collection;
import java.util.Map;

/**
 * Statement validator utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StatementValidatorUtil {
    
    /**
     * Validate sharding table.
     *
     * @param metaData meta data
     * @param tables tables
     */
    public static void validateShardingTable(final ShardingSphereMetaData metaData, final Collection<SimpleTableSegment> tables) {
        for (SimpleTableSegment each : tables) {
            String tableName = each.getTableName().getIdentifier().getValue();
            if (metaData.getRuleSchemaMetaData().getConfiguredSchemaMetaData().getAllTableNames().contains(tableName)) {
                throw new ShardingSphereException("Can not support sharding table '%s'.", tableName);
            }
        }
    }
    
    /**
     * Validate table exist.
     *
     * @param metaData meta data
     * @param tables tables
     */
    public static void validateTableExist(final ShardingSphereMetaData metaData, final Collection<SimpleTableSegment> tables) {
        for (SimpleTableSegment each : tables) {
            String tableName = each.getTableName().getIdentifier().getValue();
            for (Map.Entry<String, Collection<String>> entry : metaData.getRuleSchemaMetaData().getUnconfiguredSchemaMetaDataMap().entrySet()) {
                if (!entry.getValue().contains(tableName)) {
                    throw new NoSuchTableException(entry.getKey(), tableName);
                }
            }
        }
    }
    
    /**
     * Validate table not exist.
     *
     * @param metaData meta data
     * @param tables tables
     */
    public static void validateTableNotExist(final ShardingSphereMetaData metaData, final Collection<SimpleTableSegment> tables) {
        for (SimpleTableSegment each : tables) {
            String tableName = each.getTableName().getIdentifier().getValue();
            if (metaData.getRuleSchemaMetaData().getAllTableNames().contains(tableName)) {
                throw new TableExistsException(tableName);
            }
        }
    }
}
