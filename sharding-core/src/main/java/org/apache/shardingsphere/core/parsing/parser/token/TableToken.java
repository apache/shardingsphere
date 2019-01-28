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

package org.apache.shardingsphere.core.parsing.parser.token;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.ToString;

/**
 * Table token.
 *
 * @author zhangliang
 * @author panjuan
 */
@Getter
@ToString
public final class TableToken extends SQLToken {
    
    private final int skippedSchemaNameLength;
    
    private final String tableName;
    
    private final String leftDelimiter;
    
    private final String rightDelimiter;
    
    public TableToken(final int startIndex, final int skippedSchemaNameLength, final String tableName, final String leftDelimiter, final String rightDelimiter) {
        super(startIndex);
        this.skippedSchemaNameLength = skippedSchemaNameLength;
        this.tableName = tableName;
        this.leftDelimiter = leftDelimiter;
        this.rightDelimiter = rightDelimiter;
    }
    
    /**
     * Judge has delimiter or not.
     * 
     * @return has delimiter or not
     */
    public boolean hasDelimiter() {
        return !(Strings.isNullOrEmpty(leftDelimiter) || Strings.isNullOrEmpty(rightDelimiter));
    }
}
