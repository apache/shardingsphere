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

package org.apache.shardingsphere.core.parse.antlr.extractor.impl;

import com.google.common.base.Optional;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.parse.antlr.extractor.CollectionSQLSegmentExtractor;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.ExtractorUtils;
import org.apache.shardingsphere.core.parse.antlr.extractor.util.RuleName;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.table.TableNameOrAliasSegment;

import java.util.Collection;
import java.util.LinkedList;

/**
 *  Table names or aliases extractor.
 *
 * @author zhangliang
 */
public final class TableNamesOrAliasesExtractor implements CollectionSQLSegmentExtractor {
    
    private final TableNameOrAliasExtractor tableNameOrAliasExtractor = new TableNameOrAliasExtractor();
    
    @Override
    public Collection<TableNameOrAliasSegment> extract(final ParserRuleContext ancestorNode) {
        Collection<TableNameOrAliasSegment> result = new LinkedList<>();
        for (ParserRuleContext each : ExtractorUtils.getAllDescendantNodes(ancestorNode, RuleName.TABLE_NAME_OR_ALIAS)) {
            Optional<TableNameOrAliasSegment> tableNameOrAliasSegment = tableNameOrAliasExtractor.extract(each);
            if (tableNameOrAliasSegment.isPresent()) {
                result.add(tableNameOrAliasSegment.get());
            }
        }
        return result;
    }
}
