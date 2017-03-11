/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.parser.sql.context;

import com.google.common.base.Optional;
import lombok.Getter;

/**
 * 分页上下文.
 *
 * @author zhangliang
 */
@Getter
public final class LimitContext {
    
    private final int rowCount;
    
    private final Optional<Integer> offset;
    
    private final int rowCountParameterIndex;
    
    private final int offsetParameterIndex;
    
    public LimitContext(final int rowCount, final int rowCountParameterIndex) {
        this.rowCount = rowCount;
        offset = Optional.absent();
        this.offsetParameterIndex = -1;
        this.rowCountParameterIndex = rowCountParameterIndex;
    }
    
    public LimitContext(final int offset, final int rowCount, final int offsetParameterIndex, final int rowCountParameterIndex) {
        this.offset = Optional.of(offset);
        this.rowCount = rowCount;
        this.offsetParameterIndex = offsetParameterIndex;
        this.rowCountParameterIndex = rowCountParameterIndex;
    }
}
