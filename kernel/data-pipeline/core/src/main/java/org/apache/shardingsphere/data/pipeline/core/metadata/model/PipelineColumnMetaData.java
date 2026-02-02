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

package org.apache.shardingsphere.data.pipeline.core.metadata.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jspecify.annotations.NonNull;

/**
 * Column meta data.
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode(of = "name")
@ToString
public final class PipelineColumnMetaData implements Comparable<PipelineColumnMetaData> {
    
    private final int ordinalPosition;
    
    @NonNull
    private final String name;
    
    private final int dataType;
    
    private final String dataTypeName;
    
    private final boolean nullable;
    
    private final boolean primaryKey;
    
    private final boolean uniqueKey;
    
    @Override
    public int compareTo(final PipelineColumnMetaData o) {
        return Integer.compare(ordinalPosition, o.ordinalPosition);
    }
}
