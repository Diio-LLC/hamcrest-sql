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

import com.akiban.sql.parser.InListOperatorNode;
import com.akiban.sql.parser.QueryTreeNode;
import com.akiban.sql.parser.ValueNodeList;

/**
 * Used to match IN list expressions. Relies on ListOfNodeMatcher to match sequences or subequences of lists. The left operand matcher
 * may be any kind of ValueNode matcher, including a ListOfNodeMatcher, but the matcher for the right operand must be a ListOfNodeMatcher.
 *  
 * @author kkoster
 * 
 * @see ListOfNodeMatcher
 *
 */
public class InMatcher extends QueryTreeNodeMatcher {

    private final Matcher<QueryTreeNode> leftMatcher;
    private final ListOfNodeMatcher rightMatcher;

    public InMatcher(Matcher<QueryTreeNode> leftMatcher, ListOfNodeMatcher rightMatcher) {
        this.leftMatcher = leftMatcher;
        this.rightMatcher = rightMatcher;
    }

    @Override
    public void describeTo(Description description) {
        description.appendDescriptionOf(leftMatcher);
        description.appendText(" IN (");
        description.appendDescriptionOf(rightMatcher);
        description.appendText(")");
    }

    @Override
    protected boolean matchesSafely(QueryTreeNode item) {
        if (item instanceof InListOperatorNode) {
            InListOperatorNode node = (InListOperatorNode) item;
            ValueNodeList leftList = node.getLeftOperand().getNodeList();
            if (leftList.size() == 1 && !(leftMatcher instanceof ListOfNodeMatcher)) {
                //unwrap the (non-list) single valued left operand from the left list
                if (!leftMatcher.matches(leftList.get(0))) {
                    return false;
                }
            } else {
                if (!leftMatcher.matches(leftList)) {
                    return false;
                }
            }
            return rightMatcher.matches(node.getRightOperandList().getNodeList());
        }
        return false;
    }

    /**
     * Syntactic sugar.
     */
    @Factory
    public static Matcher<QueryTreeNode> in(Matcher<QueryTreeNode> left, ListOfNodeMatcher right) {
        return new InMatcher(left, right);
    }
}
