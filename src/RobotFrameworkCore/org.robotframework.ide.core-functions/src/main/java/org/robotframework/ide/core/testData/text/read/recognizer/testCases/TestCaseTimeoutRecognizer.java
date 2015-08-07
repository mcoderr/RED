package org.robotframework.ide.core.testData.text.read.recognizer.testCases;

import org.robotframework.ide.core.testData.text.read.recognizer.AExecutableElementSettingsRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class TestCaseTimeoutRecognizer extends
        AExecutableElementSettingsRecognizer {

    public TestCaseTimeoutRecognizer() {
        super(RobotTokenType.TEST_CASE_SETTING_TIMEOUT);
    }
}
