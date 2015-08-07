package org.robotframework.ide.core.testData.model.table.testCases;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class TestCaseTemplate extends AModelElement {

    private final RobotToken declaration;
    private RobotToken keywordName;
    private final List<RobotToken> unexpectedTrashArguments = new LinkedList<>();

    private final List<RobotToken> comment = new LinkedList<>();


    public TestCaseTemplate(final RobotToken declaration) {
        this.declaration = declaration;
    }


    @Override
    public boolean isPresent() {
        return (declaration != null);
    }


    public RobotToken getDeclaration() {
        return declaration;
    }


    public RobotToken getKeywordName() {
        return keywordName;
    }


    public void setKeywordName(RobotToken keywordName) {
        this.keywordName = keywordName;
    }


    public List<RobotToken> getUnexpectedTrashArguments() {
        return unexpectedTrashArguments;
    }


    public void addUnexpectedTrashArgument(final RobotToken trashArgument) {
        this.unexpectedTrashArguments.add(trashArgument);
    }


    public List<RobotToken> getComment() {
        return comment;
    }


    public void addCommentPart(final RobotToken rt) {
        this.comment.add(rt);
    }
}
