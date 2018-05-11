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

package io.shardingsphere.transaction.constants;

/**
 * Type of B.A.S.E transaction.
 * 
 * @author zhangliang
 */
public enum SoftTransactionType {
    
    /**
     * Best efforts delivery.
     * 
     * <p>
     * Required:
     * INSERT SQL should include primary key(auto-increment primary key in invalid).
     * UPDATE SQL should idempotent.
     * Every DELETE SQL are ok.
     * </p>
     */
    BestEffortsDelivery, 
    
    /**
     * Try confirm cancel.
     * 
     * <p>
     * Required: 
     * Business app implements cancel method.
     * </p>
     */
    TryConfirmCancel
}
