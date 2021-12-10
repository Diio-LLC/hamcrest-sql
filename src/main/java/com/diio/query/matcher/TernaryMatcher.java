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
import org.hamcrest.core.IsAnything;

import com.akiban.sql.parser.QueryTreeNode;
import com.akiban.sql.parser.TernaryOperatorNode;

/**
 * Matches a ternary operation subtree. Useful for matching expressions, especially
 * those found in WHERE clauses, such as
 * 
 * mytable.full_name LIKE '%Doe%'
 * 
 * Matchers are used to match the "receiver" (in the LIKE case, it is the column), left, and right operands, so it is possible to 
 * match complicated expressions on all three of those elements. 
 * 
 * @author kkoster
 *
 */
public class TernaryMatcher extends QueryTreeNodeMatcher {

    private final String operation;
    private final Matcher<QueryTreeNode> receiver;
    private final Matcher<QueryTreeNode> left;
    private final Matcher<QueryTreeNode> right;

    public TernaryMatcher(Matcher<QueryTreeNode> receiverMatcher,
            String operation, 
            Matcher<QueryTreeNode> leftMatcher,
            Matcher<QueryTreeNode> rightMatcher) {
        this.operation = operation;
        this.receiver = receiverMatcher;
        this.left = leftMatcher;
        this.right = rightMatcher;
    }
    
    @Override
    public void describeTo(Description description) {
        description.appendText("a ternary expression of ").
            appendDescriptionOf(receiver).
            appendText(operation + " ").
            appendDescriptionOf(left).
            appendDescriptionOf(right);
    }
    
    @Override
    protected boolean matchesSafely(QueryTreeNode item) {
        if (item instanceof TernaryOperatorNode) {
            TernaryOperatorNode node = (TernaryOperatorNode) item;

            if (operation.equalsIgnoreCase(node.getOperator())) {
                return receiver.matches(node.getReceiver()) &&
                        left.matches(node.getLeftOperand()) &&
                        right.matches(node.getRightOperand());
            }
        }

        return false;
    }

    
    /**
     * Syntactic sugar!
     *
     * For example, assertThat(query, hasInQuery(like(column("full_name"), "%Doe%")));
     */
    @Factory
    public static Matcher<QueryTreeNode> like(Matcher<QueryTreeNode> receiverMatcher, Matcher<QueryTreeNode> leftMatcher) {
        return new TernaryMatcher(receiverMatcher, "LIKE", leftMatcher, new IsAnything<QueryTreeNode>());
    }
}
