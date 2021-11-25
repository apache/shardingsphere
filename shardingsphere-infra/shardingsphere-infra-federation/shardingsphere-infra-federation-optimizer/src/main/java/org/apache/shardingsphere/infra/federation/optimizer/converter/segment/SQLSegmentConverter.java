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

package org.apache.shardingsphere.infra.federation.optimizer.converter.segment;

import org.apache.calcite.sql.SqlNode;
import org.apache.shardingsphere.sql.parser.sql.common.segment.SQLSegment;

import java.util.Optional;

/**
 * SQL segment converter.
 * 
 * @param <S> type of SQL segment
 * @param <T> type of SQL node
 */
public interface SQLSegmentConverter<S extends SQLSegment, T extends SqlNode> {
    
    /**
     * Convert SQL segment to SQL node.
     * 
     * @param segment SQL segment be to converted
     * @return converted SQL node
     */
    Optional<T> convertToSQLNode(S segment);
    
    /**
     * Convert SQL node to SQL segment.
     *
     * @param sqlNode SQL node be to converted
     * @return converted SQL segment
     */
    Optional<S> convertToSQLSegment(T sqlNode);
    
    /**
     * Get start index.
     * 
     * @param sqlNode SQL node
     * @return start index
     */
    default int getStartIndex(SqlNode sqlNode) {
        return sqlNode.getParserPosition().getColumnNum() - 1;
    }
    
    /**
     * Get stop index.
     *
     * @param sqlNode SQL node
     * @return stop index
     */
    default int getStopIndex(SqlNode sqlNode) {
        return sqlNode.getParserPosition().getEndColumnNum() - 1;
    }
}
