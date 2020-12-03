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

package org.apache.shardingsphere.proxy.backend.text;

import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

/**
 * Text protocol backend handler.
 */
public interface TextProtocolBackendHandler {
    
    /**
     * Execute command.
     *
     * @return backend response
     * @throws SQLException SQL exception
     */
    ResponseHeader execute() throws SQLException;
    
    /**
     * Goto next result value.
     *
     * @return has more result value or not
     * @throws SQLException SQL exception
     */
    default boolean next() throws SQLException {
        return false;
    }
    
    /**
     * Get row data.
     *
     * @return row data
     * @throws SQLException SQL exception
     */
    default Collection<Object> getRowData() throws SQLException {
        return Collections.emptyList();
    }
}
