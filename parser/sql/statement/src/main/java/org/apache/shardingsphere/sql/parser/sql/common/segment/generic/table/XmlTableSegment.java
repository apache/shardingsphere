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

package org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.XmlTableFunctionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Optional;

/**
 * XML table segment.
 */
@RequiredArgsConstructor
@Getter
@Setter
public final class XmlTableSegment implements TableSegment {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private final String tableName;
    
    private final String tableNameAlias;
    
    private final XmlTableFunctionSegment xmlTableFunction;
    
    private final String xmlTableFunctionAlias;
    
    @Override
    public Optional<String> getAliasName() {
        return Optional.empty();
    }
    
    @Override
    public Optional<IdentifierValue> getAlias() {
        return Optional.empty();
    }
    
    @Override
    public void setAlias(final AliasSegment alias) {
    }
}
