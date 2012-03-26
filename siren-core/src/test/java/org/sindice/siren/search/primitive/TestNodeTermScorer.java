/**
 * Copyright (c) 2009-2011 Sindice Limited. All Rights Reserved.
 *
 * Project and contact information: http://www.siren.sindice.com/
 *
 * This file is part of the SIREn project.
 *
 * SIREn is a free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * SIREn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with SIREn. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * @project siren
 * @author Renaud Delbru [ 10 Dec 2009 ]
 * @link http://renaud.delbru.fr/
 * @copyright Copyright (C) 2009 by Renaud Delbru, All rights reserved.
 */
package org.sindice.siren.search.primitive;

import static org.sindice.siren.analysis.MockSirenToken.node;
import static org.sindice.siren.search.AbstractTestSirenScorer.NodeTermQueryBuilder.ntq;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;
import org.sindice.siren.index.DocsAndNodesIterator;
import org.sindice.siren.search.AbstractTestSirenScorer;
import org.sindice.siren.search.base.NodeScorer;

public class TestNodeTermScorer extends AbstractTestSirenScorer {

  public void testNextPositionFail() throws Exception {
    this.addDocument(writer, "<http://renaud.delbru.fr/> . ");
    final NodeTermScorer scorer = (NodeTermScorer) this.getScorer(ntq("renaud"));
    assertFalse(scorer.nextPosition());
  }

  public void testNextNodeFail() throws Exception {
    this.addDocument(writer, "<http://renaud.delbru.fr/> . ");
    final NodeScorer scorer = this.getScorer(ntq("renaud"));
    assertFalse(scorer.nextNode());
  }

  @Test
  public void testNextPositionWithURI() throws Exception {
    this.addDocument("<http://renaud.delbru.fr/> <http://renaud.delbru.fr/> . ");
    NodeTermScorer scorer = (NodeTermScorer) this.getScorer(ntq("renaud"));
    assertTrue(scorer.nextCandidateDocument());
    assertEquals(0, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertEquals(-1, scorer.pos());

    assertTrue(scorer.nextNode());
    assertEquals(node(0,0), scorer.node());
    assertEquals(-1, scorer.pos());
    assertTrue(scorer.nextPosition());
    assertEquals(0, scorer.pos());

    assertTrue(scorer.nextNode());
    assertEquals(node(0,1), scorer.node());
    assertEquals(-1, scorer.pos());
    assertTrue(scorer.nextPosition());
    assertEquals(2, scorer.pos());

    assertEndOfStream(scorer);

    this.deleteAll();
    this.addDocument("<http://renaud.delbru.fr/> <http://test/name> \"Renaud Delbru\" . ");
    scorer = (NodeTermScorer) this.getScorer(ntq("renaud"));
    assertTrue(scorer.nextCandidateDocument());
    assertEquals(0, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertEquals(-1, scorer.pos());

    assertTrue(scorer.nextNode());
    assertEquals(node(0,0), scorer.node());
    assertEquals(-1, scorer.pos());
    assertTrue(scorer.nextPosition());
    assertEquals(0, scorer.pos());

    assertTrue(scorer.nextNode());
    assertEquals(node(0,2), scorer.node());
    assertEquals(-1, scorer.pos());
    assertTrue(scorer.nextPosition());
    assertEquals(4, scorer.pos());

    assertEndOfStream(scorer);
  }

  @Test
  public void testSkipToEntity() throws Exception {
    final ArrayList<String> docs = new ArrayList<String>();
    for (int i = 0; i < 32; i++) {
      docs.add("<http://renaud.delbru.fr/> . \"renaud delbru\" \"renaud delbru\" . ");
    }
    this.addDocumentsWithIterator(writer, docs);

    final NodeTermScorer scorer = (NodeTermScorer) this.getScorer(ntq("renaud"));
    assertTrue(scorer.skipToCandidate(16));
    assertEquals(16, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertEquals(-1, scorer.pos());
  }

  @Test
  public void testSkipToNonExistingDocument() throws Exception {
    final ArrayList<String> docs = new ArrayList<String>();
    for (int i = 0; i < 32; i++) {
      docs.add("<http://renaud.delbru.fr/> . \"renaud delbru\" \"renaud delbru\" . ");
      docs.add("<aaa> . \"aaa\" \"aaa bbb\" . ");
    }
    this.deleteAll(writer);
    this.addDocumentsWithIterator(writer, docs);

    final NodeTermScorer scorer = (NodeTermScorer) this.getScorer(ntq("renaud"));
    // does not exist, should skip to entity 18
    assertTrue(scorer.skipToCandidate(17));
    assertEquals(18, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertTrue(scorer.nextNode());
    assertEquals(node(0, 0), scorer.node());
    assertEquals(-1, scorer.pos());
    assertTrue(scorer.nextPosition());
    assertEquals(0, scorer.pos());

    assertFalse(scorer.skipToCandidate(76));
    assertEndOfStream(scorer);
  }

  @Test
  public void testSkipToWithConstraint() throws Exception {
    final ArrayList<String> docs = new ArrayList<String>();
    for (int i = 0; i < 32; i++) {
      docs.add("<http://renaud.delbru.fr/> . \"renaud delbru\" \"renaud delbru\" . ");
      docs.add("<aaa> . \"aaa\" \"aaa bbb\" . ");
    }
    this.deleteAll(writer);
    this.addDocumentsWithIterator(writer, docs);

    NodeScorer scorer = this.getScorer(
      ntq("renaud").bound(node(1,0), node(1,1))
    );
    // does not exist, should skip to entity 18
    assertTrue(scorer.skipToCandidate(17));
    assertEquals(18, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertTrue(scorer.nextNode());
    assertEquals(node(1,0), scorer.node());
    assertTrue(scorer.nextNode());
    assertEquals(node(1,1), scorer.node());

    assertFalse(scorer.skipToCandidate(76));
    assertEndOfStream(scorer);

    scorer = this.getScorer(
      ntq("renaud").bound(node(1), node(1))
    );

    // does not exist, should skip to entity 18
    assertTrue(scorer.skipToCandidate(17));
    assertEquals(18, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertTrue(scorer.nextNode());
    assertEquals(node(1,0), scorer.node());
    assertTrue(scorer.nextNode());
    assertEquals(node(1,1), scorer.node());

    scorer = this.getScorer(
      ntq("renaud").bound(node(1), node(1)).level(1)
    );

    // does not exist, should skip to entity 18
    assertTrue(scorer.skipToCandidate(17));
    assertEquals(18, scorer.doc());
    assertEquals(node(-1), scorer.node());
    assertFalse(scorer.nextNode());
    assertEquals(DocsAndNodesIterator.NO_MORE_NOD, scorer.node());
  }

  @Test(expected=Exception.class)
  public void testInvalidScoreCall() throws IOException {
    this.addDocument(writer, "\"Renaud\" . ");
    final NodeScorer scorer = this.getScorer(ntq("renaud"));

    // Invalid call
    scorer.score();
  }

  @Test
  public void testScore() throws IOException {
    this.addDocument(writer, "\"Renaud renaud\" \"renaud\" . ");
    final NodeScorer scorer = this.getScorer(ntq("renaud"));

    assertTrue(scorer.nextCandidateDocument());
    assertEquals(0, scorer.doc());
    assertEquals(3.0, scorer.freq(), 0.01);
    assertFalse(scorer.score() == 0);
  }

}
