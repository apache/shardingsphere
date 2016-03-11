/**
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

package com.dangdang.ddframe.rdb.sharding.config.common.exception;

import com.dangdang.ddframe.rdb.sharding.config.common.internal.ConfigUtil;
import com.google.common.base.Strings;
import groovy.lang.MissingMethodException;
import lombok.RequiredArgsConstructor;
import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * 缺失配置异常.
 * 
 * @author gaohongtao
 */
@RequiredArgsConstructor
public class MissingConfigNodeException extends RuntimeException {
    
    private final MissingMethodException mme;
    
    private final String suggestion;
    
    public String getMessage() {
        StringBuilder messageBuilder = new StringBuilder();
        if (Strings.isNullOrEmpty(suggestion)) {
            messageBuilder.append("Unsupported method: ").append(mme.getType().getName())
                    .append(".").append(mme.getMethod());
        } else {
            messageBuilder.append("No signature of method: ")
                    .append(mme.getType().getName())
                    .append(".")
                    .append(mme.getMethod())
                    .append("() is applicable for argument types: (")
                    .append(InvokerHelper.toTypeString(mme.getArguments()))
                    .append(") values: ")
                    .append(InvokerHelper.toArrayString(mme.getArguments(), 60, true))
                    .append(ConfigUtil.LINE_SEPARATOR)
                    .append("Standard syntax is ").append(mme.getMethod()).append("(").append(suggestion).append(")");
        }
        return messageBuilder.toString();
    }
    
}
