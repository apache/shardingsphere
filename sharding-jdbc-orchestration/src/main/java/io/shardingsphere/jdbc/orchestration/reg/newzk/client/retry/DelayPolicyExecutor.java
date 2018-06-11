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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/*
 * delay policy executor
 *
 * @author lidongbo
 */
public class DelayPolicyExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DelayPolicyExecutor.class);
    
    private final DelayRetryPolicy delayRetryPolicy;
    
    private final Random random;
    
    private int executeCount;
    
    private long executeTick;
    
    public DelayPolicyExecutor() {
        this(DelayRetryPolicy.newNoInitDelayPolicy());
    }
    
    public DelayPolicyExecutor(final DelayRetryPolicy delayRetryPolicy) {
        this.delayRetryPolicy = delayRetryPolicy;
        this.executeTick = System.currentTimeMillis();
        this.random = new Random();
//        next();
    }
    
    /**
     * has next.
     *
     * @return has next
     */
    public boolean hasNext() {
        return executeCount < delayRetryPolicy.getRetryCount();
    }
    
    /**
     * next exec tick.
     *
     * @return next exec tick
     */
    public long getNextTick() {
        return executeTick;
    }
    
    /**
     * next.
     */
    public void next() {
        executeCount++;
        long sleep = delayRetryPolicy.getBaseDelay() * Math.max(1, this.random.nextInt(1 << delayRetryPolicy.getRetryCount() + 1));
        if (sleep < delayRetryPolicy.getDelayUpperBound()) {
            executeTick += sleep;
        } else {
            executeTick += delayRetryPolicy.getDelayUpperBound();
        }
        LOGGER.debug("next executeCount:{}, executeTick:{}", executeCount, executeTick);
    }
}
