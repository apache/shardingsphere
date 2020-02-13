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
import org.apache.shardingsphere.underlying.common.metadata.table.TableMetaData;

import java.util.Map;

/**
 * meta data logger.
 *
 * @author yuzel
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j(topic = "ShardingSphere-MetaData")
public class MetaDataLogger {
    
    /**
     * table meta log.
     *
     * @param tableMetaDataMap table metadata map
     */
    public static void logTableMetaData(final Map<String, TableMetaData> tableMetaDataMap) {
        log("load all tables MetaData.");
        for (Map.Entry<String, TableMetaData> each : tableMetaDataMap.entrySet()) {
            log("tableName:{}, tableInfo:{}", each.getKey(), each.getValue());
        }
        log("load {} tables.", tableMetaDataMap.keySet().size());
    }
    
    /**
     * log.
     *
     * @param pattern patten
     * @param arguments arguments
     */
    public static void log(final String pattern, final Object... arguments) {
        log.info(pattern, arguments);
    }
}
