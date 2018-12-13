/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.transaction.base.servicecomb.definition;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Saga definition builder.
 *
 * @author yangyi
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class SagaDefinitionBuilder {
    
    private static final String TYPE = "sql";
    
    private final String recoveryPolicy;
    
    private final int transactionRetires;
    
    private final int compensationRetires;
    
    private final int failRetryDelay;
    
    private final ConcurrentLinkedQueue<SagaRequest> requests = new ConcurrentLinkedQueue<>();
    
    private String[] parents = new String[]{};
    
    private ConcurrentLinkedQueue<String> newRequestIds = new ConcurrentLinkedQueue<>();
    
    /**
     * Add child request node to definition graph.
     *
     * @param id request id
     * @param datasource data source name
     * @param sql transaction sql
     * @param params transaction sql parameters
     * @param compensationSql compensation sql
     * @param compensationParams compensation sql parameters
     */
    public void addChildRequest(final String id, final String datasource, final String sql, final List<List<Object>> params,
                                final String compensationSql, final List<Collection<Object>> compensationParams) {
        Transaction transaction = new Transaction(sql, params, transactionRetires);
        Compensation compensation = new Compensation(compensationSql, compensationParams, compensationRetires);
        requests.add(new SagaRequest(id, datasource, TYPE, transaction, compensation, parents, failRetryDelay));
        newRequestIds.add(id);
    }
    
    /**
     * Switch to next logic sql.
     */
    public void switchParents() {
        parents = newRequestIds.toArray(new String[]{});
        newRequestIds = new ConcurrentLinkedQueue<>();
    }
    
    /**
     * Build saga definition json string.
     *
     * @return saga json string
     * @throws JsonProcessingException json process exception
     */
    public String build() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(new SagaDefinition(recoveryPolicy, requests));
    }
}
