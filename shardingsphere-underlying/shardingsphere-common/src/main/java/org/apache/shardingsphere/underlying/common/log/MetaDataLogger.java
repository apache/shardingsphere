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

package org.apache.shardingsphere.underlying.common.log;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Meta data logger.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j(topic = "ShardingSphere-MetaData")
public class MetaDataLogger {
    
    /**
     * Log table meta data loading information.
     *
     * @param catalog catalog
     * @param table table
     */
    public static void logTableMetaData(final String catalog, final String table) {
        logTableMetaData(catalog, null, table);
    }
    
    /**
     * Log table meta data loading information.
     *
     * @param catalog catalog
     * @param schema schema
     * @param table table
     */
    public static void logTableMetaData(final String catalog, final String schema, final String table) {
        StringBuilder logTemplate = new StringBuilder("Loading table meta data ");
        List<Object> arguments = new ArrayList<>(3);
        if (null != catalog) {
            logTemplate.append("catalog: {}, ");
            arguments.add(catalog);
        }
        if (null != schema) {
            logTemplate.append("schema: {}, ");
            arguments.add(schema);
        }
        logTemplate.append("table: {}.");
        arguments.add(table);
        log(logTemplate.toString(), arguments.toArray());
    }
    
    /**
     * Log.
     *
     * @param pattern patten
     * @param arguments arguments
     */
    public static void log(final String pattern, final Object... arguments) {
        log.info(pattern, arguments);
    }
}
