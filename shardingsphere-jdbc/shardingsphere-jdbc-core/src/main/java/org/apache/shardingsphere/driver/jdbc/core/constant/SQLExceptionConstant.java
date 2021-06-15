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

package org.apache.shardingsphere.driver.jdbc.core.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * SQL exception constant.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLExceptionConstant {
    
    public static final String SQL_STRING_NULL_OR_EMPTY = "SQL String can not be NULL or empty.";
    
    public static final String COLUMN_INDEX_OUT_OF_RANGE = "Column index out of range.";
    
    public static final String OUT_OF_INDEX_SQL_STATE = "S1002";
}
