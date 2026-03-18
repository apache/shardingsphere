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

package org.apache.shardingsphere.sql.parser.statement.oracle.ddl;

import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.type.TypeDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.type.TypeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.type.CreateTypeStatement;

import java.util.Collection;

/**
 * Create object type statement for Oracle.
 */
@Getter
public final class OracleCreateObjectTypeStatement extends CreateTypeStatement {
    
    private final boolean isReplace;
    
    private final boolean isEditionable;
    
    private final boolean isFinal;
    
    private final boolean isInstantiable;
    
    private final boolean isPersistable;
    
    private final TypeSegment typeSegment;
    
    private final Collection<TypeDefinitionSegment> typeDefinitions;
    
    public OracleCreateObjectTypeStatement(final DatabaseType databaseType,
                                           final boolean isReplace, final boolean isEditionable, final boolean isFinal, final boolean isInstantiable,
                                           final boolean isPersistable, final TypeSegment typeSegment, final Collection<TypeDefinitionSegment> typeDefinitions) {
        super(databaseType);
        this.isReplace = isReplace;
        this.isEditionable = isEditionable;
        this.isFinal = isFinal;
        this.isInstantiable = isInstantiable;
        this.isPersistable = isPersistable;
        this.typeSegment = typeSegment;
        this.typeDefinitions = typeDefinitions;
    }
}
