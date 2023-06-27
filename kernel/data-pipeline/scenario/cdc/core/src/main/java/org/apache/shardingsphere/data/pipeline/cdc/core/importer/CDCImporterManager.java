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

package org.apache.shardingsphere.data.pipeline.cdc.core.importer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CDC importer manager.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CDCImporterManager {
    
    private static final Map<String, CDCImporter> IMPORTER_MAP = new ConcurrentHashMap<>();
    
    /**
     * Put importer.
     *
     * @param importer importer
     */
    public static void putImporter(final CDCImporter importer) {
        IMPORTER_MAP.put(importer.getImporterId(), importer);
    }
    
    /**
     * Get importer.
     *
     * @param id importer id
     * @return importer
     */
    public static CDCImporter getImporter(final String id) {
        return IMPORTER_MAP.get(id);
    }
    
    /**
     * Remove importer.
     *
     * @param id importer id
     */
    public static void removeImporter(final String id) {
        IMPORTER_MAP.remove(id);
    }
}
