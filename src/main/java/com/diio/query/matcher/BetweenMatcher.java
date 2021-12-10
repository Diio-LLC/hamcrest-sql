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

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

import com.akiban.sql.parser.BetweenOperatorNode;
import com.akiban.sql.parser.QueryTreeNode;

/**
 * Matches SQL BETWEEN expressions.
 * 
 * @author kkoster
 *
 */
public class BetweenMatcher extends QueryTreeNodeMatcher {

    private final Matcher<QueryTreeNode> left;
    private final Matcher<QueryTreeNode> lower;
    private final Matcher<QueryTreeNode> upper;

    public BetweenMatcher(Matcher<QueryTreeNode> leftMatcher, Matcher<QueryTreeNode> lowerBoundMatcher, Matcher<QueryTreeNode> upperBoundMatcher) {
        this.left = leftMatcher;
        this.lower = lowerBoundMatcher;
        this.upper = upperBoundMatcher;
    }

    @Override
    public void describeTo(Description description) {
        description.appendDescriptionOf(left).
            appendText("BETWEEN ").
            appendDescriptionOf(lower).
            appendText(" and ").
            appendDescriptionOf(upper);
    }
    
    @Override
    protected boolean matchesSafely(QueryTreeNode item) {
        if (item instanceof BetweenOperatorNode) {
            BetweenOperatorNode node = (BetweenOperatorNode) item;
            return left.matches(node.getLeftOperand()) &&
                    lower.matches(node.getRightOperandList().get(0)) &&
                    upper.matches(node.getRightOperandList().get(1));
        }
        return false;
    }

    /**
     * Syntactic sugar!
     *
     * For example, assertThat(query, hasInQuery(between(column("my_date", literal(55), literal(65)))));
     */
    @Factory
    public static Matcher<QueryTreeNode> between(Matcher<QueryTreeNode> leftMatcher, Matcher<QueryTreeNode> lowerBoundMatcher,
            Matcher<QueryTreeNode> upperBoundMatcher) {
        return new BetweenMatcher(leftMatcher, lowerBoundMatcher, upperBoundMatcher);
    }
}
