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

package org.apache.shardingsphere.proxy.backend.text.encoding;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.ClientEncodingResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.admin.executor.DatabaseSetCharsetExecutor;

import java.nio.charset.UnsupportedCharsetException;
import java.sql.SQLException;

/**
 * Set charset backend handler of database.
 */
@RequiredArgsConstructor
public class DatabaseSetCharsetBackendHandler implements TextProtocolBackendHandler {
    
    private final ConnectionSession connectionSession;
    
    private final DatabaseSetCharsetExecutor databaseSetCharsetExecutor;
    
    @Override
    public ResponseHeader execute() throws SQLException {
        try {
            databaseSetCharsetExecutor.execute(connectionSession);
        } catch (final UnsupportedCharsetException ignored) {
            return new ClientEncodingResponseHeader(null, ignored.getCharsetName());
        }
        return new ClientEncodingResponseHeader(databaseSetCharsetExecutor.getCurrentCharset(), null);
    }
}
