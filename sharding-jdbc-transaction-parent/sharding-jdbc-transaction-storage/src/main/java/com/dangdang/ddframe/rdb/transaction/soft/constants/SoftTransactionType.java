/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.transaction.soft.constants;

/**
 * 柔性事务类型.
 * 
 * @author zhangliang
 */
public enum SoftTransactionType {
    
    /**
     * 最大努力送达型事务.
     * 
     * <p>
     * 使用要求: 
     * INSERT语句要求必须包含主键(不能是自增主键).
     * UPDATE语句要求幂等.
     * DELETE语句无要求.
     * </p>
     */
    BestEffortsDelivery, 
    
    /**
     * TCC型事务.
     * 
     * <p>
     * 使用要求: 
     * 业务方提供cancel方法.
     * </p>
     */
    TryConfirmCancel
}
