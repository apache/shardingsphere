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

package org.apache.shardingsphere.sharding.merge.dql.pagination.builder.dialect;

import org.apache.shardingsphere.infra.binder.context.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.decorator.DecoratorMergedResult;
import org.apache.shardingsphere.sharding.merge.dql.pagination.LimitDecoratorMergedResult;
import org.apache.shardingsphere.sharding.merge.dql.pagination.builder.PaginationDecoratorMergedResultBuilder;

import java.sql.SQLException;

/**
 * Pagination decorator merged result builder for openGauss.
 */
public final class OpenGaussPaginationDecoratorMergedResultBuilder implements PaginationDecoratorMergedResultBuilder {
    
    @Override
    public DecoratorMergedResult build(final MergedResult mergedResult, final PaginationContext paginationContext) throws SQLException {
        return new LimitDecoratorMergedResult(mergedResult, paginationContext);
    }
    
    @Override
    public String getDatabaseType() {
        return "OpenGauss";
    }
}
