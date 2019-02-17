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

import lombok.Getter;
import org.apache.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.rule.DataNode;

import java.util.LinkedList;
import java.util.List;

/**
 * Insert values token.
 *
 * @author maxiaoguang
 * @author panjuan
 */
@Getter
public final class InsertValuesToken extends SQLToken {
    
    private final DefaultKeyword type;
    
    private final List<String> columnNames = new LinkedList<>();
    
    private final List<InsertColumnValue> columnValues = new LinkedList<>();
    
    public InsertValuesToken(final int startIndex, final DefaultKeyword type) {
        super(startIndex);
        this.type = type;
    }
    
    /**
     * Add insert column value.
     *
     * @param columnValues column values
     * @param columnParameters  column parameters
     */
    public void addInsertColumnValue(final List<SQLExpression> columnValues, final List<Object> columnParameters) {
        InsertColumnValue result = new InsertColumnValue();
        result.values.addAll(columnValues);
        result.parameters.addAll(columnParameters);
        this.columnValues.add(result);
    }
    
    @Getter
    public final class InsertColumnValue {
    
        private final List<SQLExpression> values = new LinkedList<>();
        
        private final List<Object> parameters = new LinkedList<>();
    
        private final List<DataNode> dataNodes = new LinkedList<>();
    }
}
