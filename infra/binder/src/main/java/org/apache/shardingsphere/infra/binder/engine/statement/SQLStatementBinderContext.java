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

package org.apache.shardingsphere.infra.binder.engine.statement;

import com.cedarsoftware.util.CaseInsensitiveMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.engine.segment.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

/**
 * SQL statement binder context.
 */
@RequiredArgsConstructor
@Getter
public final class SQLStatementBinderContext {
    
    private final ShardingSphereMetaData metaData;
    
    private final String currentDatabaseName;
    
    private final DatabaseType databaseType;
    
    private final Collection<String> variableNames;
    
    private final Collection<String> usingColumnNames = new HashSet<>();
    
    private final Collection<ProjectionSegment> joinTableProjectionSegments = new LinkedList<>();
    
    private final Map<String, TableSegmentBinderContext> externalTableBinderContexts = new CaseInsensitiveMap<>();
    
    private final Collection<String> pivotColumnNames = new HashSet<>();
    
    public SQLStatementBinderContext(final SQLStatement sqlStatement, final ShardingSphereMetaData metaData, final String currentDatabaseName) {
        this.metaData = metaData;
        this.currentDatabaseName = currentDatabaseName;
        databaseType = sqlStatement.getDatabaseType();
        variableNames = sqlStatement.getVariableNames();
    }
}
