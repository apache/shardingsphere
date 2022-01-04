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

import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Job data node line.
 */
@Getter
@RequiredArgsConstructor
@ToString
public final class JobDataNodeLine {
    
    @NonNull
    private final List<JobDataNodeEntry> entries;
    
    /**
     * Unmarshal from text.
     *
     * @param text marshalled line
     * @return line
     */
    public static JobDataNodeLine unmarshal(final String text) {
        List<String> segments = Splitter.on('|').omitEmptyStrings().splitToList(text);
        List<JobDataNodeEntry> entries = new ArrayList<>(segments.size());
        for (String each : segments) {
            entries.add(JobDataNodeEntry.unmarshal(each));
        }
        return new JobDataNodeLine(entries);
    }
    
    /**
     * Marshal to text.
     *
     * @return text, format: entry1|entry2, e.g. t_order:ds_0.t_order_0,ds_0.t_order_1|t_order_item:ds_0.t_order_item_0,ds_0.t_order_item_1
     */
    public String marshal() {
        StringBuilder result = new StringBuilder(getMarshalledTextEstimatedLength());
        for (JobDataNodeEntry each : entries) {
            result.append(each.marshal()).append('|');
        }
        if (!entries.isEmpty()) {
            result.setLength(result.length() - 1);
        }
        return result.toString();
    }
    
    private int getMarshalledTextEstimatedLength() {
        return entries.stream().mapToInt(JobDataNodeEntry::getMarshalledTextEstimatedLength).sum() + entries.size();
    }
}
