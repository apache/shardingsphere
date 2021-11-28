package org.apache.shardingsphere.encrypt.rewrite.parameter;

import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewriter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class EncryptParameterRewriterBuilderTest {

    @Test
    public void getParameterReWritersTest() {
        ShardingSphereSchema shardingSphereSchema = mock(ShardingSphereSchema.class);
        final EncryptRule encryptRule = mock(EncryptRule.class);

        EncryptParameterRewriterBuilder encryptParameterRewriterBuilder = new EncryptParameterRewriterBuilder(encryptRule, true);

        final Collection<ParameterRewriter> parameterReWriters = encryptParameterRewriterBuilder.getParameterRewriters(shardingSphereSchema);
        assertEquals(4, parameterReWriters.size());
    }
}