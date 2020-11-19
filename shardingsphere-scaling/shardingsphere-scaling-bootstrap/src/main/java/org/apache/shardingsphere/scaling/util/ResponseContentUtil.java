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

package org.apache.shardingsphere.scaling.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.scaling.web.entity.ResponseContent;
import org.apache.shardingsphere.scaling.web.entity.ResponseCode;

/**
 * Http response util.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResponseContentUtil {
    
    /**
     * Build the successful response without data model.
     *
     * @return response result
     */
    public static ResponseContent<?> success() {
        return build(null);
    }
    
    /**
     * Build the successful response with data model.
     *
     * @param model data model
     * @param <T> data model type
     * @return response result
     */
    public static <T> ResponseContent<T> build(final T model) {
        ResponseContent<T> result = new ResponseContent<>();
        result.setSuccess(true);
        result.setModel(model);
        return result;
    }
    
    /**
     * Build the bad request response.
     *
     * @param errorMsg error message
     * @return response result
     */
    public static ResponseContent<?> handleBadRequest(final String errorMsg) {
        ResponseContent<?> result = new ResponseContent<>();
        result.setSuccess(false);
        result.setErrorCode(ResponseCode.BAD_REQUEST);
        result.setErrorMsg(errorMsg);
        return result;
    }
    
    /**
     * Build the error response of exception.
     *
     * @param errorMsg error message
     * @return response result
     */
    public static ResponseContent<?> handleException(final String errorMsg) {
        ResponseContent<?> result = new ResponseContent<>();
        result.setSuccess(false);
        result.setErrorCode(ResponseCode.SERVER_ERROR);
        result.setErrorMsg(errorMsg);
        return result;
    }
}
