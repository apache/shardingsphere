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

package com.dangdang.ddframe.rdb.transaction.soft.api;

import com.dangdang.ddframe.rdb.transaction.soft.api.config.SoftTransactionConfiguration;
import com.dangdang.ddframe.rdb.transaction.soft.base.AbstractSoftTransactionMockTest;
import com.dangdang.ddframe.rdb.transaction.soft.constants.SoftTransactionType;
import com.google.common.base.Optional;
import org.junit.Test;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class SoftTransactionManagerTest extends AbstractSoftTransactionMockTest {
    
    @Test
    public void assertInitTransactionManager() throws SQLException {
        getSoftTransactionManager().init();
    }
    
    @Test
    public void assertGetBEDCurrentTransaction() throws SQLException {
        getSoftTransactionManager().getTransaction(SoftTransactionType.BestEffortsDelivery);
        assertNotNull(SoftTransactionManager.getCurrentTransaction().get());
        assertNotNull(SoftTransactionManager.getCurrentTransactionConfiguration().get());
        SoftTransactionManager.closeCurrentTransactionManager();
    }
    
    @Test
    public void assertGetTCCCurrentTransaction() throws SQLException {
        getSoftTransactionManager().getTransaction(SoftTransactionType.TryConfirmCancel);
        assertNotNull(SoftTransactionManager.getCurrentTransaction().get());
        assertNotNull(SoftTransactionManager.getCurrentTransactionConfiguration().get());
        SoftTransactionManager.closeCurrentTransactionManager();
    }
    
    @Test
    public void assertCloseCurrentTransactionManager() {
        SoftTransactionManager.closeCurrentTransactionManager();
        assertThat(SoftTransactionManager.getCurrentTransaction(), is(Optional.<AbstractSoftTransaction>absent()));
        assertThat(SoftTransactionManager.getCurrentTransactionConfiguration(), is(Optional.<SoftTransactionConfiguration>absent()));
    }
    
}
