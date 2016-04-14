/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.lucene.analysis.standard;

import java.io.IOException;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Assert;

public class TestHelper {

  public static void assertTokens(TokenStream stream, String[] expected) throws IOException {
    stream.reset();
    CharTermAttribute termAttr = stream.getAttribute(CharTermAttribute.class);
    Assert.assertNotNull(termAttr);
    int i = 0;
    while (stream.incrementToken()) {
      Assert.assertTrue(i < expected.length);
      Assert.assertEquals("expected different term at index " + i, expected[i++], termAttr.toString());
    }
    Assert.assertEquals("not all tokens produced", Integer.valueOf(i), Integer.valueOf(expected.length));
  }
}
