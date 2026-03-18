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

package org.apache.shardingsphere.data.pipeline.core.ingest.record;

/**
 * Column.
 */
public interface Column {
    
    /**
     * Get name.
     *
     * @return name
     */
    String getName();
    
    /**
     * Get old value.
     *
     * @return old value
     */
    Object getOldValue();
    
    /**
     * Get value.
     *
     * @return value
     */
    Object getValue();
    
    /**
     * Judge whether the column is updated.
     *
     * @return true if the column is updated, otherwise false
     */
    boolean isUpdated();
    
    /**
     * Judge whether the column is unique key.
     *
     * @return true if the column is unique key, otherwise false
     */
    boolean isUniqueKey();
}
