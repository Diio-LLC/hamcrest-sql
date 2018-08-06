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

import static com.diio.query.matcher.LiteralMatcher.literal;
import static com.diio.query.matcher.QueryHasMatcher.hasInQuery;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

import com.akiban.sql.parser.JavaToSQLValueNode;
import com.akiban.sql.parser.JavaValueNode;
import com.akiban.sql.parser.QueryTreeNode;
import com.akiban.sql.parser.StaticMethodCallNode;
import com.akiban.sql.parser.TernaryOperatorNode;
import com.akiban.sql.parser.ValueNode;

/**
 * Matches SQL functions. The arguments to the function can be matched via nested Matchers.
 * 
 * @author kkoster
 *
 */
public class FunctionMatcher extends QueryTreeNodeMatcher {
    // For some reason 'SUBSTR' is matched as 'substring' ternary operator.
    public static final String SUBSTR = "substring";

    public static final String NVL2 = "nvl2";

    private final String functionName;

    private final Matcher<QueryTreeNode>[] orderedArgumentMatchers;

    @SafeVarargs
    public FunctionMatcher(String functionName, Matcher<QueryTreeNode>... orderedArgumentMatchers) {
        this.functionName = functionName;
        this.orderedArgumentMatchers = orderedArgumentMatchers;
    }
    
    @Override
    public void describeTo(Description description) {
        description.appendText("function ").
            appendText(functionName);

        if (orderedArgumentMatchers.length > 0) {
            description.appendText(" called with arguments ");

            for (int i = 0; i < orderedArgumentMatchers.length; i++) {
                Matcher<QueryTreeNode> submatcher = orderedArgumentMatchers[i];

                if (i > 0) {
                    description.appendText(", ");
                }

                description.appendDescriptionOf(submatcher);
            }
        }
    }
    
    @Override
    protected boolean matchesSafely(QueryTreeNode item) {
        if (item instanceof JavaToSQLValueNode && ((JavaToSQLValueNode)item).getJavaValueNode() instanceof StaticMethodCallNode) {
            StaticMethodCallNode node = (StaticMethodCallNode) ((JavaToSQLValueNode)item).getJavaValueNode();

            if (functionName.equalsIgnoreCase(node.getMethodName())) {
                JavaValueNode[] parameters = node.getMethodParameters();

                return matchesParameters(parameters);
            }
        } else if (item instanceof TernaryOperatorNode) {
            TernaryOperatorNode node = (TernaryOperatorNode) item;

            if (functionName.equalsIgnoreCase(node.getMethodName())) {
                ValueNode[] parameters = new ValueNode[] {node.getReceiver(), node.getLeftOperand(), node.getRightOperand()};

                return matchesParameters(parameters);

            }
        }

        return false;
    }

    private boolean matchesParameters(QueryTreeNode[] parameters) {
        //no submatchers is the "existential" case for matching the function
        //TODO: consider removing this implicit existential check and require something like an IsAnything submatcher
        if (orderedArgumentMatchers.length == 0) {
            return true;
        } else if (orderedArgumentMatchers.length > 0 && orderedArgumentMatchers.length != parameters.length) {
            return false;
        }

        for (int i = 0; i < parameters.length; i++) {
            QueryTreeNode parameter = parameters[i];

            if (!hasInQuery(orderedArgumentMatchers[i]).matches(parameter)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Syntactic sugar!
     *
     * For example, assertThat(query, function("ROUND", column("price"), literal(0)));
     */
    @Factory
    @SafeVarargs
    public static Matcher<QueryTreeNode> function(String name, Matcher<QueryTreeNode>... matchers) {
        return new FunctionMatcher(name, matchers);
    }

    @Factory
    public static Matcher<QueryTreeNode> substr(Matcher<QueryTreeNode> stringMatcher, int fromIndex, int length) {
        return function(SUBSTR, stringMatcher, literal(fromIndex), literal(length));
    }

    @Factory
    public static Matcher<QueryTreeNode> nvl2(Matcher<QueryTreeNode> ifNonNullMatcher, Matcher<QueryTreeNode>  thenMatcher, Matcher<QueryTreeNode>  elseMatcher) {
        return function(NVL2, ifNonNullMatcher, thenMatcher, elseMatcher);
    }
}
