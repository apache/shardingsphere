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

package org.apache.shardingsphere.data.pipeline.common.ingest.position.pk.type;

import org.apache.shardingsphere.data.pipeline.common.ingest.position.pk.PrimaryKeyPosition;

/**
 * Integer primary key position.
 */
public final class IntegerPrimaryKeyPosition implements PrimaryKeyPosition<Long> {
    
    private final long beginValue;
    
    private final long endValue;
    
    public IntegerPrimaryKeyPosition(final long beginValue, final long endValue) {
        this.beginValue = beginValue;
        this.endValue = endValue;
    }
    
    @Override
    public Long getBeginValue() {
        return beginValue;
    }
    
    @Override
    public Long getEndValue() {
        return endValue;
    }
    
    @Override
    public Long convert(final String value) {
        return Long.parseLong(value);
    }
    
    @Override
    public char getType() {
        return 'i';
    }
    
    @Override
    public String toString() {
        return String.format("%s,%s,%s", getType(), beginValue, endValue);
    }
}
