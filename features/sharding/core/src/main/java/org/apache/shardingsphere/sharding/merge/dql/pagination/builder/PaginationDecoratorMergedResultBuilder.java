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

package org.apache.shardingsphere.sharding.merge.dql.pagination.builder;

import org.apache.shardingsphere.infra.binder.context.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPI;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.decorator.DecoratorMergedResult;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;

import java.sql.SQLException;

/**
 * Pagination decorator merged result builder.
 */
@SingletonSPI
public interface PaginationDecoratorMergedResultBuilder extends DatabaseTypedSPI {
    
    /**
     * Build decorator merged result.
     *
     * @param mergedResult merged result to be decorated
     * @param paginationContext pagination context
     * @return decorated decorator merged result
     * @throws SQLException SQL exception
     */
    DecoratorMergedResult build(MergedResult mergedResult, PaginationContext paginationContext) throws SQLException;
}
