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

package io.shardingsphere.transaction.saga;

import com.alibaba.fastjson.JSON;
import io.shardingsphere.transaction.saga.request.Compensation;
import io.shardingsphere.transaction.saga.request.SagaApi;
import io.shardingsphere.transaction.saga.request.SagaRequest;
import io.shardingsphere.transaction.saga.request.SagaRequestNoParents;
import io.shardingsphere.transaction.saga.request.Transaction;
import io.shardingsphere.transaction.saga.util.PostSagaRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Saga soft transaction.
 * 
 * @author zhangyonglun
 */
public class SagaSoftTransaction {
    
    private String transactionId;
    
    private String serviceName = "saga-transaction-example";
    
    private List<SQLPair> sqlPairs = new ArrayList<>(128);
    
    /**
     * Begin transaction.
     *
     */
    public final void begin() {
        transactionId = UUID.randomUUID().toString();
    }
    
    /**
     * End transaction.
     * 
     */
    public final void end() {
        List<SagaRequest> sagaRequests = new ArrayList<>(128);
        for (int i = 0; i < sqlPairs.size(); i++) {
            Map<String, String> transactionForm = new HashMap<>();
            transactionForm.put("sql", sqlPairs.get(i).getSqlPair().get(0));
            Map<String, Map<String, String>> transactionParams = new HashMap<>();
            transactionParams.put("form", transactionForm);
            Transaction transaction = new Transaction("post", "/execute", transactionParams);
            Map<String, String> compensationForm = new HashMap<>();
            compensationForm.put("sql", sqlPairs.get(i).getSqlPair().get(1));
            Map<String, Map<String, String>> compensationParams = new HashMap<>();
            compensationParams.put("form", compensationForm);
            Compensation compensation = new Compensation("post", "/execute", compensationParams);
            SagaRequest sagaRequest;
            if (0 == i) {
                sagaRequest = new SagaRequestNoParents(transactionId + i, "rest", serviceName, transaction, compensation);
            } else {
                // sagaRequest = new SagaRequestWithParents(transactionId + i, "rest", serviceName, Collections.singletonList(transactionId + (i - 1)), transaction, compensation);
                sagaRequest = new SagaRequestNoParents(transactionId + i, "rest", serviceName, transaction, compensation);
            }
            sagaRequests.add(sagaRequest);
        }
        SagaApi sagaApi = new SagaApi("BackwardRecovery", sagaRequests);
        PostSagaRequest.getInstance().request(JSON.toJSONString(sagaApi));
    }
    
    /**
     * Set sql and compensation.
     *
     * @param sql sql
     * @param compensation compensation
     */
    public void setSQLAndCompensation(final String sql, final String compensation) {
        SQLPair sqlPair = new SQLPair(sql, compensation);
        sqlPairs.add(sqlPair);
    }
}
