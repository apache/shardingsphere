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

package com.dangdang.ddframe.rdb.sharding.id.generator;

/**
 * Id 生成接口.
 * 
 * @author gaohongtao
 */
public interface IdGenerator {
    
    /**
     * 生成Id.
     * 
     * @return 返回生成的Id,返回值应为@{@link Number}对象或者为@{@link String}对象
     */
    Number generateId();
}
