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

package org.apache.shardingsphere.dbtest.cases.assertion.root;

import lombok.Getter;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * SQL value.
 */
@Getter
public final class SQLValue {
    
    private final Object value;
    
    private final int index;
    
    public SQLValue(final String value, final String type, final int index) throws ParseException {
        this.value = getValue(value, type);
        this.index = index;
    }
    
    private Object getValue(final String value, final String type) throws ParseException {
        if (null == type || "varchar".equals(type) || "char".equals(type) || "String".equals(type) || "json".equals(type)) {
            return value;
        }
        if ("int".equals(type)) {
            return Integer.valueOf(value);
        }
        if ("numeric".equals(type) && !value.contains("//.")) {
            return Long.valueOf(value);
        }
        if ("numeric".equals(type) && value.contains("//.")) {
            return Double.valueOf(value);
        }
        if ("datetime".equals(type)) {
            return new Date(new SimpleDateFormat("yyyy-MM-dd").parse(value).getTime());
        }
        throw new UnsupportedOperationException(String.format("Cannot support type: '%s'", type));
    }
    
    @Override
    public String toString() {
        if (value instanceof String) {
            return "'" + value + "'";
        }
        if (value instanceof Date) {
            return new SimpleDateFormat("yyyy-MM-dd").format(value);
        }
        return value.toString();
    }
}
