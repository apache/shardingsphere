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

package io.shardingsphere.jdbc.orchestration.reg.newzk.client.retry;

import lombok.extern.slf4j.Slf4j;

import java.util.Random;

/*
 * Delay policy executor.
 *
 * @author lidongbo
 */
@Slf4j
public class DelayPolicyExecutor {
    
    private final DelayRetryPolicy delayRetryPolicy;
    
    private final Random random;
    
    private int executeCount;
    
    private long executeTick;
    
    public DelayPolicyExecutor() {
        this(DelayRetryPolicy.defaultDelayPolicy());
    }
    
    public DelayPolicyExecutor(final DelayRetryPolicy delayRetryPolicy) {
        this.delayRetryPolicy = delayRetryPolicy;
        this.executeTick = System.currentTimeMillis();
        this.random = new Random();
    }
    
    /**
     * Has next.
     *
     * @return has next
     */
    public boolean hasNext() {
        return executeCount < delayRetryPolicy.getRetryCount();
    }
    
    /**
     * Next exec tick.
     *
     * @return next exec tick
     */
    public long getNextTick() {
        return executeTick;
    }
    
    /**
     * Next.
     */
    public void next() {
        executeCount++;
        long sleep = delayRetryPolicy.getBaseDelay() * Math.max(1, this.random.nextInt(1 << delayRetryPolicy.getRetryCount() + 1));
        if (sleep < delayRetryPolicy.getDelayUpperBound()) {
            executeTick += sleep;
        } else {
            executeTick += delayRetryPolicy.getDelayUpperBound();
        }
        log.debug("next executeCount:{}, executeTick:{}", executeCount, executeTick);
    }
}
