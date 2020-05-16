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

package org.apache.shardingsphere.scaling.core.execute.executor.record;

import java.util.ArrayList;
import java.util.List;

/**
 * Record util.
 */
public final class RecordUtil {
    
    /**
     * Extract primary columns from data record.
     *
     * @param dataRecord data record
     * @return primary columns
     */
    public static List<Column> extractPrimaryColumns(final DataRecord dataRecord) {
        List<Column> result = new ArrayList<>();
        for (Column each : dataRecord.getColumns()) {
            if (each.isPrimaryKey()) {
                result.add(each);
            }
        }
        return result;
    }
    
    /**
     * Extract updated columns from data record.
     *
     * @param dataRecord data record
     * @return updated columns
     */
    public static List<Column> extractUpdatedColumns(final DataRecord dataRecord) {
        List<Column> result = new ArrayList<>();
        for (Column each : dataRecord.getColumns()) {
            if (each.isUpdated()) {
                result.add(each);
            }
        }
        return result;
    }
}
