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

import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.CommentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.ParameterMarkerSegment;

import java.util.Collection;

/**
 * SQL statement.
 */
public interface SQLStatement extends ASTNode {
    
    /**
     * Get count of parameters.
     *
     * @return count of parameters
     */
    int getParameterCount();
    
    /**
     * Get parameter marker segments.
     *
     * @return parameter marker segments
     */
    Collection<ParameterMarkerSegment> getParameterMarkers();
    
    /**
     * Add parameter marker segments.
     *
     * @param segments parameter marker segments
     */
    void addParameterMarkers(Collection<ParameterMarkerSegment> segments);
    
    /**
     * Get variable names.
     *
     * @return variable names
     */
    Collection<String> getVariableNames();
    
    /**
     * Get comment segments.
     *
     * @return comment segments
     */
    Collection<CommentSegment> getComments();
}
