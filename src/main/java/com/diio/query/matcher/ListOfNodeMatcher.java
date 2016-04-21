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

import java.util.Iterator;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

import com.akiban.sql.parser.QueryTreeNode;
import com.akiban.sql.parser.QueryTreeNodeList;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;

/**
 * Used to match whether subtrees under a QueryTreeNodeList match a set of matchers in order. There are three possible semantics in order
 * of specificity: allowing gaps, subsequence not allowing gaps, and one match per node in the list.
 *
 * Note that QueryHasMatcher is used to match up submatchers to each node in the list. So submatchers don't need to exactly specify the contents of
 * each subtree in the node list, only some part of each subtree.
 *
 * @author kkoster
 *
 */
public class ListOfNodeMatcher extends QueryTreeNodeMatcher {
    public enum MatchType {
        ALLOWING_GAPS("in order"),
        SUBSEQUENCE_NO_GAPS("in a subsequence without gaps"),
        EXACT_SEQUENCE("in exact sequence");

        private final String descr;

        private MatchType(String descriptionAppendage) {
            this.descr = descriptionAppendage;
        }
    }

    private final Matcher<QueryTreeNode>[] submatchers;

    private final MatchType matchType;

    public ListOfNodeMatcher(Matcher<QueryTreeNode>[] submatchers, MatchType howToMatch) {
        this.submatchers = submatchers;
        matchType = howToMatch;
    }

    public Matcher<QueryTreeNode>[] getSubMatchers() {
        return submatchers;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("[");
        for (int i = 0; i < submatchers.length; i++) {
            Matcher<QueryTreeNode> matcher = submatchers[i];
            if (i > 0) {
                description.appendText(", ");
            }
            description.appendDescriptionOf(matcher);
        }
        description.appendText("] ");
        description.appendText(matchType.descr);
    }

    @Override
    protected boolean matchesSafely(QueryTreeNode item) {
        if (item instanceof QueryTreeNodeList) {
            @SuppressWarnings("unchecked")
            UnmodifiableIterator<Matcher<QueryTreeNode>> matcherIterator = Iterators.forArray(submatchers);

            if (!matcherIterator.hasNext()) {
                //degenerate case -- no specified submatcher matches everything
                return true;
            }
            Matcher<QueryTreeNode> nextMatcher = matcherIterator.next();

            @SuppressWarnings("unchecked")
            final Iterator<QueryTreeNode> nodeListIterator = ((QueryTreeNodeList<QueryTreeNode>)item).iterator();

            QueryTreeNode nextNode;

            while (nodeListIterator.hasNext()) {
                nextNode = nodeListIterator.next();
                if (QueryHasMatcher.hasInQuery(nextMatcher).matches(nextNode)) {
                    if (matcherIterator.hasNext()) {
                        nextMatcher = matcherIterator.next();
                    } else {
                        return (matchType != MatchType.EXACT_SEQUENCE) || !nodeListIterator.hasNext();
                    }
                } else if (matchType == MatchType.EXACT_SEQUENCE) {
                    return false;
                } else if (matchType == MatchType.SUBSEQUENCE_NO_GAPS) {
                    @SuppressWarnings("unchecked")
                    UnmodifiableIterator<Matcher<QueryTreeNode>> newMatcherIterator = Iterators.forArray(submatchers);

                    //on the next iteration, pretend we haven't matched anything yet, even if we have
                    matcherIterator = newMatcherIterator;

                    nextMatcher = matcherIterator.next();
                }
            }
        }
        return false;
    }

    @Factory
    @SafeVarargs
    public static ListOfNodeMatcher ordered(@SuppressWarnings("unchecked") Matcher<QueryTreeNode>... nodeMatchers) {
        return new ListOfNodeMatcher(nodeMatchers, MatchType.ALLOWING_GAPS);
    }

    @Factory
    @SafeVarargs
    public static ListOfNodeMatcher subsequence(@SuppressWarnings("unchecked") Matcher<QueryTreeNode>... nodeMatchers) {
        return new ListOfNodeMatcher(nodeMatchers, MatchType.SUBSEQUENCE_NO_GAPS);
    }

    @Factory
    @SafeVarargs
    public static ListOfNodeMatcher exactSequence(@SuppressWarnings("unchecked") Matcher<QueryTreeNode>... nodeMatchers) {
        return new ListOfNodeMatcher(nodeMatchers, MatchType.EXACT_SEQUENCE);
    }
}
