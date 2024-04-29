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

package org.apache.shardingsphere.sql.parser.sql.common.statement;

import lombok.Getter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.CommentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.ParameterMarkerSegment;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;

/**
 * SQL statement abstract class.
 */
@Getter
public abstract class AbstractSQLStatement implements SQLStatement {
    
    private final Collection<ParameterMarkerSegment> parameterMarkerSegments = new LinkedHashSet<>();
    
    private final Collection<Integer> uniqueParameterIndexes = new HashSet<>();
    
    private final Collection<CommentSegment> commentSegments = new LinkedList<>();
    
    private final Collection<String> variableNames = new HashSet<>();
    
    @Override
    public int getParameterCount() {
        return uniqueParameterIndexes.size();
    }
    
    /**
     * Add parameter marker segment.
     * 
     * @param parameterMarkerSegments parameter marker segment collection
     */
    public void addParameterMarkerSegments(final Collection<ParameterMarkerSegment> parameterMarkerSegments) {
        for (ParameterMarkerSegment each : parameterMarkerSegments) {
            this.parameterMarkerSegments.add(each);
            uniqueParameterIndexes.add(each.getParameterIndex());
        }
    }
}
