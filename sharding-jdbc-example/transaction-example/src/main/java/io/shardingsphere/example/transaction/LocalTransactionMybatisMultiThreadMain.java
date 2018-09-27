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

package io.shardingsphere.example.transaction;

import io.shardingsphere.example.transaction.fixture.service.DemoService;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocalTransactionMybatisMultiThreadMain {
    
    public static void main(final String[] args) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(10);
        try (ConfigurableApplicationContext applicationContext = new ClassPathXmlApplicationContext("META-INF/shardingDatabasesTables.xml")) {
            final DemoService demoService = applicationContext.getBean(DemoService.class);
            ExecutorService executorService = Executors.newFixedThreadPool(5);
            for (int i = 0; i < 10; i++) {
                executorService.execute(new Runnable() {
                    
                    @Override
                    public void run() {
                        demoService.demo();
                        latch.countDown();
                    }
                });
            }
            latch.await();
            executorService.shutdown();
        }
    }
}
