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

package org.apache.shardingsphere.core.rewrite.placeholder;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Table placeholder for rewrite.
 *
 * @author zhangliang
 * @author panjuan
 */
@RequiredArgsConstructor
@Getter
public final class TablePlaceholder implements ShardingPlaceholder {
    
    private final String logicTableName;
    
    private final String leftDelimiter;
    
    private final String rightDelimiter;
    
    @Override
    public String toString() {
        return hasDelimiter() ? leftDelimiter + logicTableName + rightDelimiter : logicTableName;
    }
    
    /**
     * Judge has delimiter or not.
     *
     * @return has delimiter or not
     */
    public boolean hasDelimiter() {
        return !(Strings.isNullOrEmpty(leftDelimiter) || Strings.isNullOrEmpty(rightDelimiter));
    }
}
