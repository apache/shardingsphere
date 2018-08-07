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

package io.shardingsphere.transaction.innersaga.mock;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Saga child transaction for each sql.
 * need service comb implement
 *
 * @author yangyi
 */
@AllArgsConstructor
@Getter
public class SagaSubTransaction {
    /**
     * sub transaction id.
     */
    private String subId;
    
    /**
     * datasource name for this part saga transaction.
     */
    private String datasource;
    
    /**
     * the confirm sql of this part saga transaction.
     */
    private String confirm;
    
    /**
     * the cancel sql of this part saga transaction.
     */
    private String cancel;
}
