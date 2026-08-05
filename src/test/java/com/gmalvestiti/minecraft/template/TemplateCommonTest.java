package com.gmalvestiti.minecraft.template;

import com.gmalvestiti.minecraft.template.config.Config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
public class TemplateCommonTest {

    @Mock
    private Config config;

    private static final String TEST_MESSAGE = "Test message";

    @BeforeEach
    void beforeEach() {
        TemplateCommon.CONFIG = null;
    }

    @Test
    void testConstructor() {
        assertDoesNotThrow(TemplateCommon::new);
    }

    @Test
    void testInitConfigNull() {
        assertDoesNotThrow(TemplateCommon::init);
    }

    @Test
    void testInitConfigEnabledTrue() {
        config.ENABLED = true;
        TemplateCommon.CONFIG = config;

        assertDoesNotThrow(TemplateCommon::init);
    }

    @Test
    void testInitConfigEnabledFalse() {
        config.ENABLED = false;
        TemplateCommon.CONFIG = config;

        assertDoesNotThrow(TemplateCommon::init);
    }

    @Test
    void testLogInfo() {
        assertDoesNotThrow(() -> TemplateCommon.info(TEST_MESSAGE));
    }

    @Test
    void testLogError() {
        assertDoesNotThrow(() -> TemplateCommon.error(TEST_MESSAGE, new RuntimeException()));
    }
}
