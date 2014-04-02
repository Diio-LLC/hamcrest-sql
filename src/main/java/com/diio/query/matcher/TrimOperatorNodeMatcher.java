package com.diio.query.matcher;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.CharConstantNode;
import com.akiban.sql.parser.QueryTreeNode;
import com.akiban.sql.parser.TrimOperatorNode;

/**
 * Matches TRIM ([ [ LEADING | TRAILING | BOTH ] [ <trim character> ] FROM ] <char value expr> )
 *
 * @author omaksymchuk
 */
public class TrimOperatorNodeMatcher extends QueryTreeNodeMatcher {
    public enum TrimQualifier {
        LEADING("LTRIM"),
        TRAILING("RTRIM"),
        BOTH("TRIM");

        private final String operator;

        TrimQualifier(String operator) {
            this.operator = operator;
        }

        public String getOperator() {
            return operator;
        }
    }

    private final Matcher<QueryTreeNode> trimSourceMatcher;

    private final TrimQualifier trimQualifier;

    private final char trimChar;

    public TrimOperatorNodeMatcher(Matcher<QueryTreeNode> trimSourceMatcher, TrimQualifier trimQualifier, char trimChar) {
        this.trimSourceMatcher = trimSourceMatcher;
        this.trimQualifier = trimQualifier;
        this.trimChar = trimChar;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("TRIM ");
        description.appendText(trimQualifier + " " + trimChar + " ");
        description.appendDescriptionOf(trimSourceMatcher);
    }

    @Override
    protected boolean matchesSafely(QueryTreeNode item) {
        if (item instanceof TrimOperatorNode) {
            TrimOperatorNode node = (TrimOperatorNode) item;

            String nodeTrimChar = null;

            try {
                nodeTrimChar = ((CharConstantNode) node.getRightOperand()).getString();
            } catch (StandardException e) {
                e.printStackTrace();
            }

            return trimQualifier.getOperator().equals(node.getOperator())
                    && String.valueOf(trimChar).equals(nodeTrimChar)
                    && trimSourceMatcher.matches(node.getLeftOperand());
        }

        return false;

    }

    @Factory
    public static Matcher<QueryTreeNode> trim(Matcher<QueryTreeNode> trimSourceMatcher, TrimQualifier trimQualifier, char trimChar) {
        return new TrimOperatorNodeMatcher(trimSourceMatcher, trimQualifier, trimChar);
    }

    @Factory
    public static Matcher<QueryTreeNode> trim(Matcher<QueryTreeNode> trimSourceMatcher, char trimChar) {
        return new TrimOperatorNodeMatcher(trimSourceMatcher, TrimQualifier.BOTH, trimChar);
    }

    @Factory
    public static Matcher<QueryTreeNode> trim(Matcher<QueryTreeNode> trimSourceMatcher) {
        return new TrimOperatorNodeMatcher(trimSourceMatcher, TrimQualifier.BOTH, ' ');
    }

    @Factory
    public static Matcher<QueryTreeNode> trimLeft(Matcher<QueryTreeNode> trimSourceMatcher, char trimChar) {
        return new TrimOperatorNodeMatcher(trimSourceMatcher, TrimQualifier.LEADING, trimChar);
    }

    @Factory
    public static Matcher<QueryTreeNode> trimRight(Matcher<QueryTreeNode> trimSourceMatcher, char trimChar) {
        return new TrimOperatorNodeMatcher(trimSourceMatcher, TrimQualifier.TRAILING, trimChar);
    }
}
