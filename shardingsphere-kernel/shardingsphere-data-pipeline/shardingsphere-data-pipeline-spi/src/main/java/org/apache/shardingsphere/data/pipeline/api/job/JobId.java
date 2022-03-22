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

package org.apache.shardingsphere.data.pipeline.api.job;

import java.util.List;

/**
 * Job id.
 */
public interface JobId {
    
    /**
     * Get type.
     *
     * @return type
     */
    String getType();
    
    /**
     * Get format version.
     *
     * @return format version
     */
    String getFormatVersion();
    
    /**
     * Get sub-types.
     *
     * @return sub-types
     */
    List<String> getSubTypes();
    
    /**
     * Get schema name.
     *
     * @return schema name
     */
    String getSchemaName();
}
