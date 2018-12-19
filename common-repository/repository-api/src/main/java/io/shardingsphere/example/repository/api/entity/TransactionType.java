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

package io.shardingsphere.example.repository.api.entity;

import java.io.Serializable;

public class TransactionType implements Serializable {
    
    private static final long serialVersionUID = -5333195312041231003L;
    
    private String transactionType;
    
    public String getTransactionType() {
        return transactionType;
    }
    
    public void setTransactionType(final String transactionType) {
        this.transactionType = transactionType;
    }
    
    @Override
    public String toString() {
        return String.format("transactionType:%s", transactionType);
    }
}
