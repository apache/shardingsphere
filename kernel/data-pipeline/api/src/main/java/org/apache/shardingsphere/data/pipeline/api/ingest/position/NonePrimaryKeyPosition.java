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

import lombok.RequiredArgsConstructor;

/**
 * None primary key position.
 */
@RequiredArgsConstructor
public final class NonePrimaryKeyPosition extends PrimaryKeyPosition<Integer> implements IngestPosition<NonePrimaryKeyPosition> {
    
    private final int offset;
    
    @Override
    public Integer getBeginValue() {
        return offset;
    }
    
    @Override
    public Integer getEndValue() {
        return Integer.MAX_VALUE;
    }
    
    @Override
    protected Integer convert(final String value) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    protected char getType() {
        return 'n';
    }
    
    @Override
    public int compareTo(final NonePrimaryKeyPosition position) {
        return 0;
    }
}
