/**
 * Copyright (c) 2009-2012 National University of Ireland, Galway. All Rights Reserved.
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
 * @project siren-core
 * @author Renaud Delbru [ 14 Mar 2012 ]
 * @link http://renaud.delbru.fr/
 */
package org.sindice.siren.analysis;

import org.apache.lucene.util.IntsRef;
import org.sindice.siren.util.XSDDatatype;

public class MockSirenToken {

  char[] term;
  int startOffset, endOffset;
  int posInc;
  int tokenType;
  char[] datatype;
  IntsRef nodePath;

  private MockSirenToken(final char[] term, final int startOffset,
                         final int endOffset, final int posInc,
                         final int tokenType, final char[] datatype,
                         final IntsRef nodePath) {
    this.term = term;
    this.startOffset = startOffset;
    this.endOffset = endOffset;
    this.posInc = posInc;
    this.tokenType = tokenType;
    this.datatype = datatype;
    this.nodePath = nodePath;
  }

  public static MockSirenToken token(final String term, final IntsRef nodePath) {
    return token(term, 0, 0, 1, TupleTokenizer.LITERAL,
      XSDDatatype.XSD_STRING.toCharArray(), nodePath);
  }

  public static MockSirenToken token(final String term, final int startOffset,
                                     final int endOffset, final int posInc,
                                     final int tokenType, final char[] datatype,
                                     final IntsRef nodePath) {
    return new MockSirenToken(term.toCharArray(), startOffset, endOffset,
      posInc, tokenType, datatype, nodePath);
  }

  public static IntsRef node(final int ... id) {
    return new IntsRef(id, 0, id.length);
  }

}