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

package org.apache.shardingsphere.ui.web.advice;

import org.apache.shardingsphere.ui.common.exception.ShardingSphereUIException;
import org.apache.shardingsphere.ui.web.response.ResponseResult;
import org.apache.shardingsphere.ui.web.response.ResponseResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Global exception handler.
 */
@Slf4j
@ControllerAdvice
public final class GlobalExceptionHandler {
    
    /**
     * Handle exception.
     * 
     * @param ex exception
     * @return response result
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseResult<?> handleException(final Exception ex) {
        log.error("controller error", ex);
        if (ex instanceof IllegalArgumentException) {
            return ResponseResultUtil.handleIllegalArgumentException(ex.getMessage());
        } else if (ex instanceof ShardingSphereUIException) {
            return ResponseResultUtil.handleShardingSphereUIException((ShardingSphereUIException) ex);
        }
        return ResponseResultUtil.handleUncaughtException(ex.getMessage());
    }
}
