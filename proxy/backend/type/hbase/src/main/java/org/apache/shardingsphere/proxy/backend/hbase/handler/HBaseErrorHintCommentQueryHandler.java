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
 * Backend handler for error hint for HBase.
 */
public final class HBaseErrorHintCommentQueryHandler implements HBaseBackendHandler {
    
    private static final Collection<String> COLUMN_NAMES = Arrays.asList("ID", "Hint", "Status");
    
    private final List<HBaseErrorHintCommentQueryRowData> rowDataList;
    
    private int currentIndex;
    
    public HBaseErrorHintCommentQueryHandler(final String hintComment) {
        rowDataList = Arrays.asList(new HBaseErrorHintCommentQueryRowData(1, "HBase", true), new HBaseErrorHintCommentQueryRowData(2, StringUtils.strip(hintComment, "* "), false));
        currentIndex = 0;
    }
    
    @Override
    public ResponseHeader execute() {
        List<QueryHeader> queryHeaders = COLUMN_NAMES.stream().map(each -> new QueryHeader("", "", each, each, Types.CHAR, "CHAR", 255, 0, false, false, false, false)).collect(Collectors.toList());
        return new QueryResponseHeader(queryHeaders);
    }
    
    @Override
    public boolean next() {
        currentIndex++;
        return currentIndex <= rowDataList.size();
    }
    
    @Override
    public Collection<Object> getRowDataObjects() {
        return rowDataList.get(currentIndex - 1).toList();
    }
}
