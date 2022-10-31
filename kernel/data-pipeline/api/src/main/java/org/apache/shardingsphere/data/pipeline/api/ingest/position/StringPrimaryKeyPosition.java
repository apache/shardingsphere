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

package org.apache.shardingsphere.data.pipeline.api.ingest.position;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * String primary key position.
 */
@RequiredArgsConstructor
@Getter
public final class StringPrimaryKeyPosition extends PrimaryKeyPosition<String> implements IngestPosition<StringPrimaryKeyPosition> {
    
    private final String beginValue;
    
    private final String endValue;
    
    @Override
    protected String convert(final String value) {
        return value;
    }
    
    @Override
    protected char getType() {
        return 's';
    }
    
    @Override
    public int compareTo(final StringPrimaryKeyPosition position) {
        return null == position ? 1 : beginValue.compareTo(position.getBeginValue());
    }
}
