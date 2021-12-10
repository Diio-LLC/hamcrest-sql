/*
   Copyright (c) 2022 Cirium

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.diio.query.matcher;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

import com.akiban.sql.parser.ConstantNode;
import com.akiban.sql.parser.QueryTreeNode;
import com.akiban.sql.parser.SQLToJavaValueNode;
import com.akiban.sql.types.TypeId;

/**
 * Matches SQL literals of varying types. For matching numeric literals, consider using NumberMatcher instead.
 * 
 * @author kkoster
 *
 * @param <T> the type of literal to match.
 * @see NumberMatcher
 */
public class LiteralMatcher<T> extends QueryTreeNodeMatcher {

    private final T literal;
    private final Matcher<T> matcher;
    private final boolean checkSingleCharLiteral;

    public LiteralMatcher(T literal) {
        this(literal, false);
    }

    public LiteralMatcher(T literal, boolean checkSingleCharLiteral) {
        this.literal = literal;
        this.matcher = null;
        this.checkSingleCharLiteral = checkSingleCharLiteral;
    }

    public LiteralMatcher(Matcher<T> numericMatcher) {
        this(numericMatcher, false);
    }

    public LiteralMatcher(Matcher<T> numericMatcher, boolean checkSingleCharLiteral) {
        this.literal = null;
        this.matcher = numericMatcher;
        this.checkSingleCharLiteral = checkSingleCharLiteral;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("a literal with value ");
        if (literal != null) {
            description.appendText(literal.toString());
        } else {
            description.appendDescriptionOf(matcher);
        }
    }

    @Override
    protected boolean matchesSafely(QueryTreeNode item) {
        final QueryTreeNode checkThisItem;
        if (checkSingleCharLiteral && item instanceof SQLToJavaValueNode) {
            checkThisItem = ((SQLToJavaValueNode) item).getSQLValueNode();
        } else {
            checkThisItem = item;
        }

        if (checkThisItem instanceof ConstantNode) {
            Object value = ((ConstantNode) checkThisItem).getValue();

            if (matcher == null) {
                if (value instanceof Number && literal instanceof Number) {
                    return ((Number) value).doubleValue() == ((Number) literal).doubleValue();
                } else {
                    return literal.equals(value);
                }
            } else {
                return matcher.matches(value);
            }
        }
        return false;
    }


    /**
     * Syntactic sugar!
     *
     * For example, assertThat(query, literal("my_id")));
     */
    @Factory
    public static <T> LiteralMatcher<T> singlecharliteral(T literalValue) {
        return new LiteralMatcher<T>(literalValue, true);
    }

    /**
     * Syntactic sugar!
     *
     * For example, assertThat(query, literal("my_id")));
     */
    @Factory
    public static <T> LiteralMatcher<T> literal(T literalValue) {
        return new LiteralMatcher<T>(literalValue);
    }

    /**
     * Syntactic sugar!
     *
     * For example, assertThat(query, literal(22)));
     */
    @Factory
    public static <T extends Number> LiteralMatcher<Number> integralLiteral(T literal) {
        return new LiteralMatcher<Number>(NumberMatcher.strictIntegral(literal));
    }

    /**
     * Syntactic sugar!
     *
     * For example, assertThat(query, literal(22.45)));
     */
    @Factory
    public static <T extends Number> LiteralMatcher<Number> floatingPointLiteral(T literal, double error) {
        return new LiteralMatcher<Number>(NumberMatcher.strictAndClose(literal, error));
    }

    /**
     * Syntactic sugar!
     *
     * For example, assertThat(query, timeLiteral("11:34:12")));
     */
    @Factory
    public static Matcher<QueryTreeNode> timeLiteral(String timeAsString) {
        return new CastMatcher(CoreMatchers.equalTo(TypeId.TIME_ID), literal(timeAsString));
    }

    /**
     * Syntactic sugar!
     *
     * For example, assertThat(query, dateLiteral("11:34:12")));
     */
    @Factory
    public static Matcher<QueryTreeNode> dateLiteral(String dateAsString) {
        return new CastMatcher(CoreMatchers.equalTo(TypeId.DATE_ID), literal(dateAsString));
    }
}
