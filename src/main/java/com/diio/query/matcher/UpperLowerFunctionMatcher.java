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
import com.akiban.sql.parser.SimpleStringOperatorNode;

/**
 * Specialized matcher for UPPER and LOWER functions. For more general function matching, see FunctionMatcher.
 *  
 * @author kkoster
 *
 */
public class UpperLowerFunctionMatcher extends QueryTreeNodeMatcher {

    private final String functionName;
    private final Matcher<QueryTreeNode> argumentMatcher;

    public UpperLowerFunctionMatcher(String upperOrLower, Matcher<QueryTreeNode> argumentMatcher) {
        functionName = upperOrLower;
        this.argumentMatcher = argumentMatcher;
    }
    
    @Override
    public void describeTo(Description description) {
        description.appendText("function ").
            appendText(functionName);
        if (argumentMatcher != null) {
            description.appendText(" called with arguments ");
            description.appendDescriptionOf(argumentMatcher);
        }
    }
    
    @Override
    protected boolean matchesSafely(QueryTreeNode item) {
        if (item instanceof SimpleStringOperatorNode) {
            SimpleStringOperatorNode node = (SimpleStringOperatorNode) item;
            if (functionName.equalsIgnoreCase(node.getMethodName())) {
                return argumentMatcher.matches(node.getOperand());
            }
        }
        return false;
    }

    /**
     * Syntactic sugar!
     *
     * For example, assertThat(query, hasInQuery(lower(column("my_str"))));
     */
    @Factory
    public static Matcher<QueryTreeNode> lower(Matcher<QueryTreeNode> argumentMatcher) {
        return new UpperLowerFunctionMatcher("LOWER", argumentMatcher);
    }
    
    /**
     * Syntactic sugar!
     *
     * For example, assertThat(query, hasInQuery(lower(column("my_str"))));
     */
    @Factory
    public static Matcher<QueryTreeNode> upper(Matcher<QueryTreeNode> argumentMatcher) {
        return new UpperLowerFunctionMatcher("UPPER", argumentMatcher);
    }    
}
