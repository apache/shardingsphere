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

package org.apache.shardingsphere.core.parse.sql.segment.dml.item;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.parse.sql.segment.SQLSegment;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Select items segment.
 * 
 * @author duhongjun
 * @author panjuan
 */
@RequiredArgsConstructor
@Getter
public final class SelectItemsSegment implements SQLSegment {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private final boolean distinctRow;
    
    private final Collection<SelectItemSegment> selectItems = new LinkedList<>();
    
    /**
     * Find select item segments.
     * 
     * @param selectItemSegmentType select item segment type
     * @param <T> select item segment
     * @return select item segments
     */
    @SuppressWarnings("unchecked")
    public <T extends SelectItemSegment> Collection<T> findSelectItemSegments(final Class<T> selectItemSegmentType) {
        Collection<T> result = new LinkedList<>();
        for (SelectItemSegment each : selectItems) {
            if (each.getClass().equals(selectItemSegmentType)) {
                result.add((T) each);
            }
        }
        return result;
    }
}
