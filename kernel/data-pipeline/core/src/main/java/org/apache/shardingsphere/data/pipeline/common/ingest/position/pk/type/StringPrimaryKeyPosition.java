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

import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.data.pipeline.common.ingest.position.pk.PrimaryKeyPosition;

/**
 * String primary key position.
 */
@Getter
public final class StringPrimaryKeyPosition implements PrimaryKeyPosition<String> {
    
    private final String beginValue;
    
    private final String endValue;
    
    public StringPrimaryKeyPosition(final String beginValue, final String endValue) {
        this.beginValue = Strings.emptyToNull(beginValue);
        this.endValue = Strings.emptyToNull(endValue);
    }
    
    @Override
    public String convert(final String value) {
        return value;
    }
    
    @Override
    public char getType() {
        return 's';
    }
    
    @Override
    public String toString() {
        return String.format("%s,%s,%s", getType(), null == beginValue ? "" : beginValue, null == endValue ? "" : endValue);
    }
}
