package org.apache.shardingsphere.encrypt.rewrite.token;

import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.SQLTokenGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class EncryptTokenGenerateBuilderTest {

    private EncryptTokenGenerateBuilder builder;

    /**
     * test SQL token generator builder for encrypt.
     */
    @Test
    public void getSQLTokenGeneratorsTest() {
        final EncryptRule encryptRule = mock(EncryptRule.class);
        builder = new EncryptTokenGenerateBuilder(encryptRule, true);
        final Collection<SQLTokenGenerator> sqlTokenGenerators = builder.getSQLTokenGenerators();

        assertEquals(11, sqlTokenGenerators.size());
    }
}
