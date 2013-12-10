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

import com.akiban.sql.parser.AggregateNode;
import com.akiban.sql.parser.QueryTreeNode;

/**
 * Matches SQL aggregate functions, such as SUM, MIN, MAX, etc.
 * 
 * @author kkoster
 *
 */
public class AggregateMatcher extends QueryTreeNodeMatcher {

    private final String aggregationType;
    private final Matcher<QueryTreeNode> submatcher;

    public AggregateMatcher(String aggregation, Matcher<QueryTreeNode> expressionMatcher) {
        aggregationType = aggregation;
        submatcher = expressionMatcher;
    }
    
    @Override
    public void describeTo(Description description) {
        description.appendText("an aggregated ").
            appendText(aggregationType).
            appendText(" column with ").
            appendDescriptionOf(submatcher);
    }
    
    @Override
    protected boolean matchesSafely(QueryTreeNode item) {
        if (item instanceof AggregateNode) {
            AggregateNode node = (AggregateNode) item;
            if (aggregationType.equalsIgnoreCase(node.getAggregateName())) {
                return submatcher.matches(node.getOperand());
            }
        }
        return false;
    }

    /**
     * Syntactic sugar!
     *
     * For example, assertThat(query, aggregate("SUM", column("my_id"))));
     */
    @Factory
    public static Matcher<QueryTreeNode> aggregate(String name, Matcher<QueryTreeNode> matcher) {
        return new AggregateMatcher(name, matcher);
    }

    /**
     * Syntactic sugar!
     *
     * For example, assertThat(query, sum(column("my_id"))));
     */
    @Factory
    public static Matcher<QueryTreeNode> sum(Matcher<QueryTreeNode> matcher) {
        return new AggregateMatcher("SUM", matcher);
    }
}
