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

package org.apache.shardingsphere.ui.web.response;

import com.google.gson.Gson;
import org.apache.shardingsphere.ui.common.exception.ShardingSphereUIException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Response result utility.
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
        ResponseResult<T> result = new ResponseResult<>();
        result.setSuccess(true);
        result.setModel(model);
        return result;
    }
    
    /**
     * Build the response from json.
     *
     * @param responseResultJson response result json string
     * @return response result
     */
    public static ResponseResult buildFromJson(final String responseResultJson) {
        return new Gson().fromJson(responseResultJson, ResponseResult.class);
    }
    
    /**
     * Build the error response of illegal argument exception.
     *
     * @param errorMsg error message
     * @return response result
     */
    public static ResponseResult handleIllegalArgumentException(final String errorMsg) {
        ResponseResult result = new ResponseResult<>();
        result.setSuccess(false);
        result.setErrorCode(ShardingSphereUIException.INVALID_PARAM);
        result.setErrorMsg(errorMsg);
        return result;
    }
    
    /**
     * Build the error response of unauthorized exception.
     *
     * @param errorMsg error message
     * @return response result
     */
    public static ResponseResult handleUnauthorizedException(final String errorMsg) {
        ResponseResult result = new ResponseResult<>();
        result.setSuccess(false);
        result.setErrorCode(ShardingSphereUIException.NO_RIGHT);
        result.setErrorMsg(errorMsg);
        return result;
    }
    
    /**
     * Build the error response of ShardingSphere UI exception.
     *
     * @param exception ShardingSphere UI exception
     * @return response result
     */
    public static ResponseResult handleShardingSphereUIException(final ShardingSphereUIException exception) {
        ResponseResult result = new ResponseResult<>();
        result.setSuccess(false);
        result.setErrorCode(exception.getErrorCode());
        result.setErrorMsg(exception.getMessage());
        return result;
    }
    
    /**
     * Build the error response of uncaught exception.
     *
     * @param errorMsg error message
     * @return response result
     */
    public static ResponseResult handleUncaughtException(final String errorMsg) {
        ResponseResult result = new ResponseResult<>();
        result.setSuccess(false);
        result.setErrorCode(ShardingSphereUIException.SERVER_ERROR);
        result.setErrorMsg(errorMsg);
        return result;
    }
    
}
