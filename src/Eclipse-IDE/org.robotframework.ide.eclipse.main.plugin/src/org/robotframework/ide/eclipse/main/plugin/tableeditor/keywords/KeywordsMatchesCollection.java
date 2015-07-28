package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords;

import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment.MatchesCollection;

import com.google.common.collect.Range;

public class KeywordsMatchesCollection extends MatchesCollection {

    @Override
    public void collect(final RobotElement element, final String filter) {
        if (element instanceof RobotKeywordsSection) {
            collectMatches((RobotKeywordsSection) element, filter);
        }
    }

    public void collectMatches(final RobotKeywordsSection section, final String filter) {
        for (final RobotKeywordDefinition keywordDefinition : section.getChildren()) {
            collectMatches(keywordDefinition, filter);
        }
    }

    private void collectMatches(final RobotKeywordDefinition keywordDefinition, final String filter) {
        boolean isMatching = false;
        isMatching |= collectMatches(filter, keywordDefinition.getName());
        final RobotDefinitionSetting argumentsSetting = keywordDefinition.getArgumentsSetting();
        if (argumentsSetting != null && argumentsSetting.getArguments() != null) {
            for (final String argument : argumentsSetting.getArguments()) {
                isMatching |= collectMatches(filter, argument);
            }
        }
        isMatching |= collectMatches(filter, keywordDefinition.getComment());
        if (isMatching) {
            rowsMatching++;
        }

        for (final RobotKeywordCall call : keywordDefinition.getChildren()) {
            collectMatches(call, filter);
        }
    }

    private void collectMatches(final RobotKeywordCall call, final String filter) {
        boolean isMatching = false;
        isMatching |= collectMatches(filter, call.getName());
        for (final String arg : call.getArguments()) {
            isMatching |= collectMatches(filter, arg);
        }
        isMatching |= collectMatches(filter, call.getComment());
        if (isMatching) {
            rowsMatching++;
        }
    }

    private boolean collectMatches(final String filter, final String label) {
        int index = label.indexOf(filter);
        final boolean result = index >= 0;
        while (index >= 0) {
            put(label, Range.closed(index, index + filter.length()));
            allMatches++;
            index = label.indexOf(filter, index + 1);
        }
        return result;
    }
}
