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

package org.apache.shardingsphere.core.merge.dal.show;

import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * Merged result for show tableNames.
 *
 * @author zhangliang
 * @author panjuan
 * @author sunbufu
 */
@RequiredArgsConstructor
public final class ShowTablesMergedResult extends LocalMergedResultAdapter {
    
    private final List<String> tableNames;
    
    private final List<String> tableTypes;
    
    private int currentIndex;
    
    public ShowTablesMergedResult() {
        tableNames = Collections.emptyList();
        tableTypes = Collections.emptyList();
    }
    
    @Override
    public boolean next() {
        return currentIndex++ < tableNames.size();
    }
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) {
        if (1 == columnIndex) {
            return tableNames.get(currentIndex > 0 ? currentIndex - 1 : 0);
        } else {
            return tableTypes.get(currentIndex > 0 ? currentIndex - 1 : 0);
        }
    }
}
