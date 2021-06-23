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

package org.apache.shardingsphere.encrypt.rewrite.condition;

import java.util.List;
import java.util.Map;

/**
 * Encrypt condition.
 */
public interface EncryptCondition {
    
    /**
     * Get column name.
     * 
     * @return column name
     */
    String getColumnName();
    
    /**
     * Get table name.
     * 
     * @return table name
     */
    String getTableName();
    
    /**
     * Get start index.
     * 
     * @return start index
     */
    int getStartIndex();
    
    /**
     * Get stop index.
     * 
     * @return stop index
     */
    int getStopIndex();
    
    /**
     * Get position index map.
     * 
     * @return position index map
     */
    Map<Integer, Integer> getPositionIndexMap();
    
    /**
     * Get position value map.
     * 
     * @return position value map
     */
    Map<Integer, Object> getPositionValueMap();
    
    /**
     * Get values.
     *
     * @param parameters SQL parameters
     * @return values
     */
    List<Object> getValues(List<Object> parameters);
}
