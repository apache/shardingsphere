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

package io.shardingsphere.orchestration.reg.newzk.client.action;

import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.transaction.BaseTransaction;
import org.apache.zookeeper.CreateMode;

/**
 * Provider with transaction.
 *
 * @author lidongbo
 */
public interface ITransactionProvider extends IProvider {
    
    /**
     * Only create target node in transaction.
     *
     * @param key key
     * @param value value
     * @param createMode create mode
     * @param transaction zookeeper transaction
     */
    void createInTransaction(String key, String value, CreateMode createMode, BaseTransaction transaction);
}
