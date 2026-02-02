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

package org.apache.shardingsphere.data.pipeline.cdc.core.ack;

import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.data.pipeline.cdc.util.RandomStrings;

import java.util.List;

/**
 * CDC ack id.
 */
@RequiredArgsConstructor
@Getter
public final class CDCAckId {
    
    private final String importerId;
    
    private final String random;
    
    /**
     * Build ack id.
     *
     * @param importerId importer id
     * @return ack id
     */
    public static CDCAckId build(final String importerId) {
        return new CDCAckId(importerId, RandomStrings.randomAlphanumeric(16));
    }
    
    /**
     * Marshal ack id.
     *
     * @return ack id
     */
    public String marshal() {
        return importerId + "_" + random;
    }
    
    /**
     * Unmarshal ack id from text.
     *
     * @param text text
     * @return ack id
     */
    public static CDCAckId unmarshal(final String text) {
        List<String> parts = Splitter.on('_').trimResults().omitEmptyStrings().splitToList(text);
        return new CDCAckId(parts.get(0), parts.get(1));
    }
}
