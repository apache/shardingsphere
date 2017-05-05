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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.context;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * 表上下文.
 * 
 * @author gaohongtao
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
@ToString
public final class TableContext {
    
    private final String originalLiterals;
    
    private final String name;
    
    private final Optional<String> alias;
    
    public TableContext(final String name, final Optional<String> alias) {
        this(name, name, alias);
    }
}
