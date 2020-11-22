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

package org.apache.shardingsphere.proxy.backend.response.query;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.executor.sql.result.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.raw.result.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.BackendResponse;

import java.util.LinkedList;
import java.util.List;

/**
 * Query response.
 */
@RequiredArgsConstructor
@Getter
public final class QueryResponse implements BackendResponse {
    
    private final List<QueryHeader> queryHeaders;
    
    private final List<QueryResult> queryResults = new LinkedList<>();
}
