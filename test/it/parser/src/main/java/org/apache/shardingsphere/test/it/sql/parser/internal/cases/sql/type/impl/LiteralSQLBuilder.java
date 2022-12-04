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

package org.apache.shardingsphere.test.it.sql.parser.internal.cases.sql.type.impl;

import org.apache.shardingsphere.sql.parser.sql.common.enums.ParameterMarkerType;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.sql.type.CaseTypedSQLBuilder;

import java.util.List;

/**
 * Literal SQL builder.
 */
public final class LiteralSQLBuilder implements CaseTypedSQLBuilder {
    
    @Override
    public String build(final String sql, final List<?> params) {
        StringBuilder result = new StringBuilder(sql);
        int currentCharIndex = 0;
        for (Object each : params) {
            currentCharIndex = result.indexOf(ParameterMarkerType.QUESTION.getMarker(), currentCharIndex);
            result.replace(currentCharIndex, ++currentCharIndex, each.toString());
        }
        return result.toString();
    }
}
