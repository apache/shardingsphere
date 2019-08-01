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

package org.apache.shardingsphere.core.rewrite.token.pojo;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Insert generated key name token.
 *
 * @author panjuan
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public final class InsertGeneratedKeyNameToken extends SQLToken implements Attachable {
    
    private final String column;
    
    private final boolean isToAppendCloseParenthesis;
    
    public InsertGeneratedKeyNameToken(final int startIndex, final String column, final boolean isToAppendCloseParenthesis) {
        super(startIndex);
        this.column = column;
        this.isToAppendCloseParenthesis = isToAppendCloseParenthesis;
    }
    
    @Override
    public String toString() {
        if (null == column) {
            return "";
        }
        if (isToAppendCloseParenthesis) {
            return String.format(", %s)", column);
        }
        return String.format(", %s", column);
    }
}
