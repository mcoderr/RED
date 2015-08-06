package org.robotframework.ide.core.testData.model.table.variables.mapping;

import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.VariableTable;
import org.robotframework.ide.core.testData.model.table.mapping.ElementsUtility;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.variables.DictionaryVariable;
import org.robotframework.ide.core.testData.model.table.variables.IVariableHolder;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;


public class DictionaryVariableValueMapper implements IParsingMapper {

    private final ElementsUtility utility;


    public DictionaryVariableValueMapper() {
        this.utility = new ElementsUtility();
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        List<IRobotTokenType> types = rt.getTypes();
        types.remove(RobotTokenType.UNKNOWN);
        types.add(0, RobotTokenType.VARIABLES_VARIABLE_VALUE);

        VariableTable variableTable = robotFileOutput.getFileModel()
                .getVariableTable();
        List<IVariableHolder> variables = variableTable.getVariables();
        if (!variables.isEmpty()) {
            IVariableHolder var = variables.get(variables.size() - 1);
            KeyValuePair keyValPair = splitKeyNameFromValue(rt);
            ((DictionaryVariable) var).put(rt, keyValPair.getKey(),
                    keyValPair.getValue());
        } else {
            // FIXME: some error
        }
        processingState.push(ParsingState.DICTIONARY_VARIABLE_VALUE);

        return rt;
    }


    @VisibleForTesting
    protected KeyValuePair splitKeyNameFromValue(final RobotToken raw) {
        StringBuilder keyText = new StringBuilder();
        StringBuilder valueText = new StringBuilder();

        boolean wasEquals = false;
        StringBuilder rawText = raw.getText();
        for (int i = 0; i < rawText.length(); i++) {
            char c = rawText.charAt(i);

            if (c == '=' && !wasEquals) {
                wasEquals = true;
                continue;
            }

            if (wasEquals) {
                valueText.append(c);
            } else {
                keyText.append(c);
            }
        }

        RobotToken key = new RobotToken();
        key.setLineNumber(raw.getLineNumber());
        key.setStartColumn(raw.getStartColumn());
        key.setText(keyText);
        key.setType(RobotTokenType.VARIABLES_DICTIONARY_KEY);

        RobotToken value = new RobotToken();
        value.setLineNumber(raw.getLineNumber());
        if (valueText.length() > 0) {
            // plus one for equals sign
            value.setStartColumn(key.getEndColumn() + 1);
        } else {
            value.setStartColumn(value.getEndColumn());
        }
        value.setText(valueText);
        value.setType(RobotTokenType.VARIABLES_DICTIONARY_VALUE);

        return new KeyValuePair(key, value);
    }

    protected class KeyValuePair {

        private final RobotToken key;
        private final RobotToken value;


        public KeyValuePair(final RobotToken key, final RobotToken value) {
            this.key = key;
            this.value = value;
        }


        public RobotToken getKey() {
            return key;
        }


        public RobotToken getValue() {
            return value;
        }
    }


    @Override
    public boolean checkIfCanBeMapped(RobotFileOutput robotFileOutput,
            RobotLine currentLine, RobotToken rt, String text,
            Stack<ParsingState> processingState) {
        ParsingState state = utility.getCurrentStatus(processingState);
        return (state == ParsingState.DICTIONARY_VARIABLE_DECLARATION || state == ParsingState.DICTIONARY_VARIABLE_VALUE);
    }
}
