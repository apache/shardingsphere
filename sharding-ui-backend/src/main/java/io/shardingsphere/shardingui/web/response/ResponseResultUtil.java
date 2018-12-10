/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingui.web.response;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Response result utility.
 *
 * @author chenqingyang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResponseResultUtil {
    
    /**
     * Build the successful response without data model.
     *
     * @return response result
     */
    public static ResponseResult success() {
        return build(null);
    }
    
    /**
     * Build the successful response with data model.
     *
     * @param model data model
     * @param <T> data model type
     * @return response result
     */
    public static <T> ResponseResult<T> build(final T model) {
        ResponseResult<T> t = new ResponseResult<>();
        t.setSuccess(true);
        t.setModel(model);
        return t;
    }
    
}
