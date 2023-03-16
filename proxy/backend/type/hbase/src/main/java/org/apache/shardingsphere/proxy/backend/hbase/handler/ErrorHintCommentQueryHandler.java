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

package org.apache.shardingsphere.proxy.backend.hbase.handler;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.proxy.backend.hbase.result.HBaseBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Backend handler for error hint.
 */
@Getter
public final class ErrorHintCommentQueryHandler implements HBaseBackendHandler {
    
    private final String hintComment;
    
    private int currentIndex;
    
    private final String[][] result;
    
    public ErrorHintCommentQueryHandler(final String hintComment) {
        this.hintComment = hintComment;
        this.currentIndex = 0;
        int index = 1;
        this.result = new String[][]{{String.valueOf(index++), "HBase", "supported", "", ""}, {String.valueOf(index), StringUtils.strip(hintComment, "* "), "unsupported", "/", "/"}};
    }
    
    @Override
    public ResponseHeader execute() {
        List<QueryHeader> queryHeaders = getColumnNames().stream().map(each -> new QueryHeader("", "", each, each, Types.CHAR, "CHAR", 255, 0, false, false, false, false)).collect(
                Collectors.toList());
        return new QueryResponseHeader(queryHeaders);
    }
    
    private Collection<String> getColumnNames() {
        return Arrays.asList("ID", "Hint", "Status", "Author", "Email");
    }
    
    @Override
    public boolean next() {
        currentIndex += 1;
        return currentIndex <= result.length;
    }
    
    @Override
    public Collection<Object> getRowDataObjects() {
        return Arrays.asList(result[currentIndex - 1]);
    }
}
