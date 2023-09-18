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

package org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl;

import org.apache.shardingsphere.sql.parser.sql.common.value.literal.LiteralValue;

/**
 * Date time literal value.
 */
public final class DateTimeLiteralValue implements LiteralValue<String> {
    
    private final String dateTimeType;
    
    private final String dateTimeValue;
    
    private final boolean containsBrace;
    
    public DateTimeLiteralValue(final String dateTimeType, final String dateTimeValue, final boolean containsBrace) {
        this.dateTimeType = dateTimeType;
        this.dateTimeValue = containsBrace ? dateTimeValue.substring(1, dateTimeValue.length() - 1) : dateTimeValue;
        this.containsBrace = containsBrace;
    }
    
    @Override
    public String getValue() {
        if (containsBrace) {
            return "{" + dateTimeType + " " + dateTimeValue + "}";
        }
        return dateTimeType + " " + dateTimeValue;
    }
}
