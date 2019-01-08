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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * saga recovery policy type.
 *
 * @author yangyi
 */

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum SagaRecoveryPolicy {
    FORWARD("ForwardRecovery"), BACKWARD("BackwardRecovery");
    
    @Getter
    private final String name;
    
    /**
     * Find SagaRecoveryPolicy by policy name.
     *
     * @param policyName policy name
     * @return SagaRecoveryPolicy
     */
    public static SagaRecoveryPolicy find(final String policyName) {
        for (SagaRecoveryPolicy each : SagaRecoveryPolicy.values()) {
            if (policyName.equals(each.getName())) {
                return each;
            }
        }
        throw new UnsupportedOperationException(String.format("Cannot find saga recovery policy of [%s]", policyName));
    }
}
