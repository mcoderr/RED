package org.robotframework.ide.core.testData.text.lexer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.robotframework.ide.core.testData.text.lexer.matcher.AsteriskMatcher;


/**
 * Concatenation of the same special tokens like asterisk '*', which appears
 * together.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see AsteriskMatcher
 */
public enum GroupedSameTokenType implements RobotType {
    /**
     * just for not return null in {@link #getToken(RobotToken)} method
     */
    UNKNOWN(RobotTokenType.UNKNOWN),
    /**
     * 
     */
    MANY_ASTERISKS(RobotTokenType.SINGLE_ASTERISK);

    private final RobotTokenType wrappedType;
    private static final Map<RobotTokenType, GroupedSameTokenType> reservedWordTypes;

    static {
        Map<RobotTokenType, GroupedSameTokenType> temp = new HashMap<>();
        GroupedSameTokenType[] values = GroupedSameTokenType.values();
        for (GroupedSameTokenType type : values) {
            temp.put(type.wrappedType, type);
        }

        reservedWordTypes = Collections.unmodifiableMap(temp);
    }


    public static GroupedSameTokenType getToken(final RobotType previousType) {
        GroupedSameTokenType groupedSameTokenType = reservedWordTypes
                .get(previousType);

        if (groupedSameTokenType == null) {
            groupedSameTokenType = GroupedSameTokenType.UNKNOWN;
        }

        return groupedSameTokenType;
    }


    public boolean isFromThisGroup(RobotToken token) {
        RobotType tokenType = token.getType();
        return (tokenType == this) || (tokenType == this.wrappedType);
    }


    private GroupedSameTokenType(final RobotTokenType wrappedType) {
        this.wrappedType = wrappedType;
    }


    @Override
    public boolean isWriteable() {
        return false;
    }


    @Override
    public String toWrite() {
        throw new UnsupportedOperationException(
                "Write should be performed from " + RobotToken.class
                        + "#getText(); method. Type: " + this);
    }


    public RobotTokenType getWrappedType() {
        return this.wrappedType;
    }
}
