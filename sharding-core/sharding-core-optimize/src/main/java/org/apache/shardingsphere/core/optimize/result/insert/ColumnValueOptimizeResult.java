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

package org.apache.shardingsphere.core.optimize.result.insert;

import org.apache.shardingsphere.core.parse.old.parser.expression.SQLExpression;

import java.util.Collection;

/**
 * Column value optimize result.
 *
 * @author panjuan
 * @author zhangliang
 */
public final class ColumnValueOptimizeResult extends InsertOptimizeResultUnit {
    
    public ColumnValueOptimizeResult(final Collection<String> columnNames, final SQLExpression[] values, final Object[] parameters) {
        super(columnNames, values, parameters);
    }
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("(");
        for (int i = 0; i < getColumnNames().size(); i++) {
            result.append(getColumnSQLExpressionValue(i)).append(", ");
        }
        result.delete(result.length() - 2, result.length()).append(")");
        return result.toString();
    }
}
