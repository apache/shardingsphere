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

package org.apache.shardingsphere.mcp.bootstrap.transport.http;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

final class DefaultAcceptHeaderRequestWrapper extends HttpServletRequestWrapper {
    
    private static final String ACCEPT_HEADER = "Accept";
    
    private static final String DEFAULT_ACCEPT = "application/json, text/event-stream";
    
    private DefaultAcceptHeaderRequestWrapper(final HttpServletRequest request) {
        super(request);
    }
    
    static HttpServletRequest wrapIfNecessary(final HttpServletRequest request) {
        return Objects.toString(request.getHeader(ACCEPT_HEADER), "").trim().isEmpty() ? new DefaultAcceptHeaderRequestWrapper(request) : request;
    }
    
    @Override
    public String getHeader(final String name) {
        return ACCEPT_HEADER.equalsIgnoreCase(name) ? DEFAULT_ACCEPT : super.getHeader(name);
    }
    
    @Override
    public Enumeration<String> getHeaders(final String name) {
        return ACCEPT_HEADER.equalsIgnoreCase(name) ? Collections.enumeration(List.of(DEFAULT_ACCEPT)) : super.getHeaders(name);
    }
    
    @Override
    public Enumeration<String> getHeaderNames() {
        Set<String> result = new LinkedHashSet<>(Collections.list(super.getHeaderNames()));
        result.add(ACCEPT_HEADER);
        return Collections.enumeration(result);
    }
}
