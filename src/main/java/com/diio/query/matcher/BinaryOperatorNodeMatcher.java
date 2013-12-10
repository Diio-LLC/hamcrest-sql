/*
   Copyright (c) 2013 Diio, LLC

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

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

import com.akiban.sql.parser.BinaryOperatorNode;
import com.akiban.sql.parser.QueryTreeNode;

/**
 * Matches three-part binary operation subtrees. Useful for matching expressions, especially
 * those found in WHERE clauses, such as
 * 
 * mytable.id = 33
 * 
 * The three parts are the left side, the operation, and the right side.
 * 
 * Nested matchers can be used to match the left and right sides, so it is possible to match complicated expressions 
 * on those sides. 
 * 
 * @author kkoster
 *
 */
public class BinaryOperatorNodeMatcher extends QueryTreeNodeMatcher {

    private final String operation;
    private final Matcher<QueryTreeNode> left;
    private final Matcher<QueryTreeNode> right;

    public BinaryOperatorNodeMatcher(Matcher<QueryTreeNode> leftMatcher, 
            String operation, 
            Matcher<QueryTreeNode> rightMatcher) {
        this.operation = operation;
        this.left = leftMatcher;
        this.right = rightMatcher;
    }
    
    @Override
    public void describeTo(Description description) {
        description.appendText("a relational expression of ").
            appendDescriptionOf(left).
            appendText(operation + " ").
            appendDescriptionOf(right);
    }
    
    @Override
    protected boolean matchesSafely(QueryTreeNode item) {
        if (item instanceof BinaryOperatorNode) {
            BinaryOperatorNode node = (BinaryOperatorNode) item;
            if (operation.equalsIgnoreCase(node.getOperator())) {
                return left.matches(node.getLeftOperand()) &&
                        right.matches(node.getRightOperand());
            }
        }
        return false;
    }

    /**
     * Syntactic sugar!
     *
     * For example, assertThat(query, hasInQuery(relation(column("my_id"), "=", "22")));
     */
    @Factory
    public static Matcher<QueryTreeNode> relation(Matcher<QueryTreeNode> leftMatcher, String operation, Matcher<QueryTreeNode> rightMatcher) {
        return new BinaryOperatorNodeMatcher(leftMatcher, operation, rightMatcher);
    }
    
    /**
     * Syntactic sugar!
     *
     * For example, assertThat(query, hasInQuery(and(column("my_id").equals(22), column("other_id").equals("23"))));
     */
    @Factory
    public static Matcher<QueryTreeNode> andRelation(Matcher<QueryTreeNode> leftMatcher, 
            Matcher<QueryTreeNode> rightMatcher) {
        return relation(leftMatcher, "and", rightMatcher);
    }
    
    
    /**
     * Syntactic sugar!
     *
     * For example, assertThat(query, hasInQuery(relation(column("my_id"), "=", "22")));
     */
    @Factory
    public static Matcher<QueryTreeNode> orRelation(Matcher<QueryTreeNode> leftMatcher, Matcher<QueryTreeNode> rightMatcher) {
        return relation(leftMatcher, "or", rightMatcher);
    }

    /**
     * Syntactic sugar!
     *
     * For example, assertThat(query, hasInQuery(concat(column("my_id"), "22")));
     */
    @Factory
    public static Matcher<QueryTreeNode> concat(Matcher<QueryTreeNode> leftMatcher, Matcher<QueryTreeNode> rightMatcher) {
        return new BinaryOperatorNodeMatcher(leftMatcher, "||", rightMatcher);
    }

    /**
     * Syntactic sugar!
     *
     * For example, assertThat(query, hasInQuery(plus(column("my_id"), "22")));
     */
    @Factory
    public static Matcher<QueryTreeNode> plus(Matcher<QueryTreeNode> leftMatcher, Matcher<QueryTreeNode> rightMatcher) {
        return new BinaryOperatorNodeMatcher(leftMatcher, "+", rightMatcher);
    }
    
    /**
     * Syntactic sugar!
     *
     * For example, assertThat(query, hasInQuery(minus(column("my_id"), "22")));
     */
    @Factory
    public static Matcher<QueryTreeNode> minus(Matcher<QueryTreeNode> leftMatcher, 
            Matcher<QueryTreeNode> rightMatcher) {
        return new BinaryOperatorNodeMatcher(leftMatcher, "-", rightMatcher);
    }
    
    /**
     * Syntactic sugar!
     *
     * For example, assertThat(query, hasInQuery(dividedBy(column("my_id"), "22")));
     */
    @Factory
    public static Matcher<QueryTreeNode> dividedBy(Matcher<QueryTreeNode> leftMatcher, 
            Matcher<QueryTreeNode> rightMatcher) {
        return new BinaryOperatorNodeMatcher(leftMatcher, "/", rightMatcher);
    }

    /**
     * Syntactic sugar!
     *
     * For example, assertThat(query, hasInQuery(times(column("my_id"), "22")));
     */
    @Factory
    public static Matcher<QueryTreeNode> times(Matcher<QueryTreeNode> leftMatcher, Matcher<QueryTreeNode> rightMatcher) {
        return new BinaryOperatorNodeMatcher(leftMatcher, "*", rightMatcher);
    }

    /**
     * Syntactic sugar!
     *
     * For example, assertThat(query, hasInQuery(lessThanOrEqualsTo(column("my_id"), "22")));
     */
    @Factory
    public static Matcher<QueryTreeNode> lessThan(Matcher<QueryTreeNode> leftMatcher, Matcher<QueryTreeNode> rightMatcher) {
        return relation(leftMatcher, "<", rightMatcher);
    }

    /**
     * Syntactic sugar!
     *
     * For example, assertThat(query, hasInQuery(greaterThan(column("my_id"), "22")));
     */
    @Factory
    public static Matcher<QueryTreeNode> greaterThan(Matcher<QueryTreeNode> leftMatcher, Matcher<QueryTreeNode> rightMatcher) {
        return relation(leftMatcher, ">", rightMatcher);
    }

    /**
     * Syntactic sugar!
     *
     * For example, assertThat(query, hasInQuery(lessThanOrEqualsTo(column("my_id"), "22")));
     */
    @Factory
    public static Matcher<QueryTreeNode> lessThanOrEqualsTo(Matcher<QueryTreeNode> leftMatcher,
            Matcher<QueryTreeNode> rightMatcher) {
        return new BinaryOperatorNodeMatcher(leftMatcher, "<=", rightMatcher);
    }

    /**
     * Syntactic sugar!
     *
     * For example, assertThat(query, hasInQuery(greaterThanOrEqualsTo(column("my_id"), "22")));
     */
    @Factory
    public static Matcher<QueryTreeNode> greaterThanOrEqualsTo(Matcher<QueryTreeNode> leftMatcher, Matcher<QueryTreeNode> rightMatcher) {
        return new BinaryOperatorNodeMatcher(leftMatcher, ">=", rightMatcher);
    }

    /**
     * Syntactic sugar!
     *
     * For example, assertThat(query, hasInQuery(equalTo(column("my_id"), "22")));
     */
    @Factory
    public static Matcher<QueryTreeNode> equalTo(Matcher<QueryTreeNode> leftMatcher,
            Matcher<QueryTreeNode> rightMatcher) {
        return new BinaryOperatorNodeMatcher(leftMatcher, "=", rightMatcher);
    }

    /**
     * Syntactic sugar!
     *
     * For example, assertThat(query, hasInQuery(is(column("my_id"), true)));
     */
    @Factory
    public static Matcher<QueryTreeNode> is(Matcher<QueryTreeNode> leftMatcher, Matcher<QueryTreeNode> rightMatcher) {
        return new BinaryOperatorNodeMatcher(leftMatcher, "IS", rightMatcher);
    }
}
