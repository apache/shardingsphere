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

package org.apache.shardingsphere.sql.parser.statement.core.statement;

import com.cedarsoftware.util.CaseInsensitiveSet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.CommentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.ParameterMarkerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;

/**
 * SQL statement.
 */
@RequiredArgsConstructor
@Getter
public class SQLStatement implements ASTNode {
    
    private final Collection<Integer> uniqueParameterIndexes = new LinkedHashSet<>();
    
    private final Collection<ParameterMarkerSegment> parameterMarkers = new LinkedHashSet<>();
    
    private final Collection<String> variableNames = new CaseInsensitiveSet<>();
    
    private final Collection<CommentSegment> comments = new LinkedList<>();
    
    private final DatabaseType databaseType;
    
    private SQLStatementAttributes attributes;
    
    /**
     * Get count of parameters.
     *
     * @return count of parameters
     */
    public final int getParameterCount() {
        return uniqueParameterIndexes.size();
    }
    
    /**
     * Add parameter marker segments.
     *
     * @param segments parameter marker segments
     */
    public final void addParameterMarkers(final Collection<ParameterMarkerSegment> segments) {
        for (ParameterMarkerSegment each : segments) {
            parameterMarkers.add(each);
            uniqueParameterIndexes.add(each.getParameterIndex());
        }
    }
    
    /**
     * Build SQL statement attributes.
     */
    public void buildAttributes() {
        attributes = new SQLStatementAttributes();
    }
}
