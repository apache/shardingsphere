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

package org.apache.shardingsphere.data.pipeline.api.datanode;

import lombok.RequiredArgsConstructor;

import java.util.Collection;

/**
 * Job data node line.
 */
@RequiredArgsConstructor
public final class JobDataNodeLine {
    
    private final Collection<JobDataNodeEntry> entries;
    
    /**
     * Marshal to text.
     *
     * @return marshaled text, format: entry1|entry2, e.g. t_order:ds_0.t_order_0,ds_0.t_order_1|t_order_item:ds_0.t_order_item_0,ds_0.t_order_item_1
     */
    public String marshal() {
        StringBuilder result = new StringBuilder(entries.stream().mapToInt(JobDataNodeEntry::getMarshalledTextEstimatedLength).sum() + entries.size());
        for (JobDataNodeEntry each : entries) {
            result.append(each.marshal()).append('|');
        }
        if (!entries.isEmpty()) {
            result.setLength(result.length() - 1);
        }
        return result.toString();
    }
}
