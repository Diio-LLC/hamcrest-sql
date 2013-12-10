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

import java.io.StringWriter;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.QueryTreeNode;
import com.akiban.sql.parser.Visitable;
import com.akiban.sql.parser.Visitor;

/**
 * Attempts to match the given nested Matcher&lt;QueryTreeNode&rt; against all query subtrees. Useful
 * in cases where you are attempting to determine whether a subclause is present but you do not care
 * about the rest of the SQL statement.
 * 
 * @author kkoster
 *
 */
public class QueryHasMatcher extends TypeSafeMatcher<QueryTreeNode> {

    private final Matcher<QueryTreeNode> subMatcher;

    public QueryHasMatcher(Matcher<QueryTreeNode> m) {
        subMatcher = m;
    }
    
    @Override
    public void describeTo(Description description) {
        description.appendText("Query that has ").appendDescriptionOf(subMatcher);
    }
    
    @Override
    protected void describeMismatchSafely(QueryTreeNode item, Description mismatchDescription) {
        if (item instanceof QueryTreeNode) {
            final StringWriter writer = new StringWriter();
            ((QueryTreeNode)item).treePrint(writer);
            mismatchDescription.appendText("was ").appendText(writer.toString());
        } else {
            super.describeMismatchSafely(item, mismatchDescription);
        }        
    }

    @Override
    protected boolean matchesSafely(QueryTreeNode item) {
        final MatchingVisitor matchingVisitor = new MatchingVisitor(subMatcher);
        try {
            item.accept(matchingVisitor);
        } catch (StandardException e) {
            throw new RuntimeException(e);
        }
        return matchingVisitor.foundMatch();
    }

    public QueryTreeNode getMatch(QueryTreeNode node) {
        final MatchingVisitor matchingVisitor = new MatchingVisitor(subMatcher);
        try {
            node.accept(matchingVisitor);
        } catch (StandardException e) {
            throw new RuntimeException(e);
        }
        return matchingVisitor.match;
    }    
    
    private static class MatchingVisitor implements Visitor {
        private final Matcher<QueryTreeNode> matcher;
        private QueryTreeNode match = null;
        private boolean found = false; //boolean needed because of possibility of a matcher that matches nulls
        
        public MatchingVisitor(Matcher<QueryTreeNode> subMatcher) {
            matcher = subMatcher;            
            if (subMatcher == null) {
                throw new IllegalArgumentException("Submatcher must be defined for " + getClass().getName());
            }
        }

        public boolean foundMatch() {
            return found;
        }

        @Override
        public Visitable visit(Visitable node) throws StandardException {
            if (matcher.matches(node)) {
                match = (QueryTreeNode) node;
                found = true;
            }
            return node;
        }

        @Override
        public boolean visitChildrenFirst(Visitable node) {
            return false;
        }

        @Override
        public boolean stopTraversal() {
            return found;
        }

        @Override
        public boolean skipChildren(Visitable node) throws StandardException {
            //could eventually make this stop before going into parts of a statement we aren't targeting
            //e.g. skip subqueries or skip ORDER BYs if we only care about columns within SELECTS 
            return false;
        }
        
    }

    
    /**
     * Syntactic sugar!
     *
     * For example, assertThat(query, hasInQuery(column("my_id"))));
     */
    @Factory
    public static QueryHasMatcher hasInQuery(Matcher<QueryTreeNode> subMatcher) {
        return new QueryHasMatcher(subMatcher);
    }

}
