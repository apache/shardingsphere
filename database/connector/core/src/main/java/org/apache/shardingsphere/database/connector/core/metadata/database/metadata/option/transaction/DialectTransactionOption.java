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

package org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.transaction;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

/**
 * Dialect transaction option.
 */
@RequiredArgsConstructor
@Getter
public final class DialectTransactionOption {
    
    private final boolean isSupportGlobalCSN;
    
    private final boolean isDDLNeedImplicitCommit;
    
    private final boolean isSupportAutoCommitInNestedTransaction;
    
    private final boolean isSupportDDLInXATransaction;
    
    // TODO Investigate the reason of some databases cannot support meta data refreshed in transaction. The method should be removed finally after metadata refresh supported for all database.
    private final boolean isSupportMetaDataRefreshInTransaction;
    
    private final int defaultIsolationLevel;
    
    private final boolean isReturnRollbackStatementWhenCommitFailed;
    
    private final boolean isAllowCommitAndRollbackOnlyWhenTransactionFailed;
    
    private final Collection<String> xaDriverClassNames;
}
