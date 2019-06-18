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

import io.shardingsphere.shardingui.common.exception.ShardingUIException;
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
        ResponseResult<T> result = new ResponseResult<>();
        result.setSuccess(true);
        result.setModel(model);
        return result;
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
        result.setErrorCode(ShardingUIException.INVALID_PARAM);
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
        result.setErrorCode(ShardingUIException.NO_RIGHT);
        result.setErrorMsg(errorMsg);
        return result;
    }
    
    /**
     * Build the error response of sharding UI exception.
     *
     * @param exception sharding UI exception
     * @return response result
     */
    public static ResponseResult handleShardingUIException(final ShardingUIException exception) {
        ResponseResult result = new ResponseResult<>();
        result.setSuccess(false);
        result.setErrorCode(exception.getErrCode());
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
        result.setErrorCode(ShardingUIException.SERVER_ERROR);
        result.setErrorMsg(errorMsg);
        return result;
    }
    
}
