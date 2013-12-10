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
import org.hamcrest.core.IsAnything;

import com.akiban.sql.parser.ConditionalNode;
import com.akiban.sql.parser.QueryTreeNode;

/**
 * Matches CASE statements, such as CASE WHEN my_col='a value' THEN 33 ELSE 34 END
 * 
 * TODO: support matching multiple WHEN... THEN... ELSE conditions for a single CASE statement
 * 
 * @author kkoster
 *
 */
public class CaseStatementMatcher extends QueryTreeNodeMatcher {

    private final Matcher<QueryTreeNode> whenMatcher;
    private final Matcher<QueryTreeNode> thenMatcher;
    private final Matcher<QueryTreeNode> elseMatcher;

    public CaseStatementMatcher(Matcher<QueryTreeNode> whenMatcher, Matcher<QueryTreeNode> thenMatcher, Matcher<QueryTreeNode> elseMatcher) {
        this.whenMatcher = whenMatcher;
        this.thenMatcher = thenMatcher;
        this.elseMatcher = elseMatcher;
    }
    
    @Override
    public void describeTo(Description description) {
        description.appendText("a CASE statement clause of ").
            appendDescriptionOf(whenMatcher).
            appendText(" THEN ").
            appendDescriptionOf(thenMatcher).
            appendText(" ELSE ").
            appendDescriptionOf(elseMatcher);
    }
    
    @Override
    protected boolean matchesSafely(QueryTreeNode item) {
        if (item instanceof ConditionalNode) {
            ConditionalNode caseNode = (ConditionalNode) item;
            return whenMatcher.matches(caseNode.getTestCondition()) 
                    && thenMatcher.matches(caseNode.getThenNode()) 
                    && elseMatcher.matches(caseNode.getElseNode());
        }
        return false;
    }

    /**
     * Syntactic sugar! This version only checks the content of a conditional in a WHEN clause, such as
     * CASE WHEN (my_id=35)
     *
     * For example, assertThat(query, cased(column("my_id").equals(35)));
     */
    @Factory
    public static Matcher<QueryTreeNode> cased(Matcher<QueryTreeNode> whenMatcher) {
        return new CaseStatementMatcher(whenMatcher, new IsAnything<QueryTreeNode>(), new IsAnything<QueryTreeNode>());
    }
    
    /**
     * Syntactic sugar! This version checks the content of a conditional in a WHEN clause, a THEN
     * clause, and an ELSE clause such as
     * CASE WHEN (my_id=35) THEN 5 ELSE 4 END
     *
     * For example, assertThat(query, cased(column("my_id").equals(35)));
     */
    @Factory
    public static Matcher<QueryTreeNode> cased(Matcher<QueryTreeNode> whenMatcher, Matcher<QueryTreeNode> thenMatcher, 
            Matcher<QueryTreeNode> elseMatcher) {
        return new CaseStatementMatcher(whenMatcher, thenMatcher, elseMatcher);
    }    
    
}
