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

package org.apache.shardingsphere.infra.optimizer.sql;

import org.apache.calcite.sql.SqlDynamicParam;
import org.apache.calcite.sql.SqlWriter;
import org.apache.calcite.sql.parser.SqlParserPos;

public final class SqlDynamicValueParam<T> extends SqlDynamicParam {
    
    private final T original;
    
    private T actual;
    
    public SqlDynamicValueParam(final T original, final int index, final SqlParserPos pos) {
        super(index, pos);
        this.original = original;
        this.actual = original;
    }
    
    @Override
    public void unparse(final SqlWriter writer, final int leftPrec, final int rightPrec) {
        writer.print(String.valueOf(actual));
        writer.setNeedWhitespace(true);
    }
    
    /**
     * Set the actual value.
     * @param value actual value to use
     */
    public void setActual(final T value) {
        this.actual = value;
    }
    
    /**
     * Get the original value.
     * @return original value
     */
    public T getOriginal() {
        return this.original;
    }
}
