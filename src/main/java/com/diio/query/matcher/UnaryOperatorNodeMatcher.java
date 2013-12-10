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

import com.akiban.sql.parser.QueryTreeNode;
import com.akiban.sql.parser.UnaryOperatorNode;

/**
 * Matches unary operator subtree branching off of NOT or IS NULL. Useful for matching expressions, especially
 * those found in WHERE clauses, such as
 *
 * mytable.id NOT NULL
 *
 * A nested matcher is used to match the operand, so it is possible to match complex/nested expressions.
 *
 * @author kkoster
 *
 */
public class UnaryOperatorNodeMatcher extends QueryTreeNodeMatcher {

    private final String operation;
    private final Matcher<QueryTreeNode> subMatcher;

    public UnaryOperatorNodeMatcher(Matcher<QueryTreeNode> subMatcher, String operation) {
        this.operation = operation;
        this.subMatcher = subMatcher;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(operation + " ").
            appendDescriptionOf(subMatcher);
    }

    @Override
    protected boolean matchesSafely(QueryTreeNode item) {
        if (item instanceof UnaryOperatorNode) {
            UnaryOperatorNode node = (UnaryOperatorNode) item;
            if (operation.equalsIgnoreCase(node.getOperator())) {
                return subMatcher.matches(node.getOperand());
            }
        }
        return false;
    }

    /**
     * Syntactic sugar!
     *
     * For example, assertThat(query, hasInQuery(isNull(column("my_id"))));
     */
    @Factory
    public static Matcher<QueryTreeNode> isNull(Matcher<QueryTreeNode> subMatcher) {
        return new UnaryOperatorNodeMatcher(subMatcher, "IS NULL");
    }

    /**
     * Syntactic sugar!
     *
     * For example, assertThat(query, hasInQuery(not(isNull(column("my_id")))));
     */
    @Factory
    public static Matcher<QueryTreeNode> not(Matcher<QueryTreeNode> subMatcher) {
        return new UnaryOperatorNodeMatcher(subMatcher, "NOT");
    }

    @Factory
    public static Matcher<QueryTreeNode> upper(Matcher<QueryTreeNode> subMatcher) {
        return new UnaryOperatorNodeMatcher(subMatcher, "UPPER");
    }
}
