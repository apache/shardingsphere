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

package org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.type;

import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.PrimaryKeyIngestPosition;

/**
 * String primary key ingest position.
 */
@Getter
public final class StringPrimaryKeyIngestPosition implements PrimaryKeyIngestPosition<String> {
    
    private final String lowerBound;
    
    private final String upperBound;
    
    public StringPrimaryKeyIngestPosition(final String lowerBound, final String upperBound) {
        this.lowerBound = Strings.emptyToNull(lowerBound);
        this.upperBound = Strings.emptyToNull(upperBound);
    }
    
    @Override
    public char getType() {
        return 's';
    }
    
    @Override
    public String toString() {
        return String.format("%s,%s,%s", getType(), null == lowerBound ? "" : lowerBound, null == upperBound ? "" : upperBound);
    }
}
