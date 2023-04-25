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

package org.apache.shardingsphere.test.e2e.agent.jdbc.project.vo.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Http result.
 * 
 * @param <T> type of data
 */
@NoArgsConstructor
@Getter
public class HttpResult<T> implements Serializable {
    
    private static final long serialVersionUID = -7263644459975356117L;
    
    private int code;
    
    private String message;
    
    private T data;
    
    @Builder(toBuilder = true)
    public HttpResult(final int code, final String message, final T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
