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
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.util.Version;
import org.junit.Test;
import java.util.Locale;

public class CustomWordBoundaryStandardTokenizerTest {

  @Test
  public void customWordBoundaries_break() throws IOException {
    char[] charz = new char[] { 'z', '_' };
    final Map<Character, Character> characterMappings = new HashMap<>();
    for (char chr : charz)
      characterMappings.put(chr, CustomWordBoundaryStandardTokenizerImpl.WB_CLASS_BREAK);

    final String[] expected = { "foo", "bar" };
    TestHelper.assertTokens(createCustomWordBoundaryStandardTokenizer("foozbar", characterMappings), expected);
    TestHelper.assertTokens(createCustomWordBoundaryStandardTokenizer("foo_bar", characterMappings), expected);
  }

  @Test
  public void customWordBoundaries_extendedNumLetter() throws IOException {
    String f_text = "%sfoo%sbar%s";
    char[] charz = new char[] { '#', '@', '+', '-' };
    final Map<Character, Character> characterMappings = new HashMap<>();
    for (char chr : charz)
      characterMappings.put(chr, CustomWordBoundaryStandardTokenizerImpl.WB_CLASS_EXTENDED_NUM_LETTER);

    for (char chr : charz) {
      final String input = String.format(Locale.US, f_text, args(3, chr));
      final String[] expected = { input }; // e.g. #foo#bar#
      TestHelper.assertTokens(createCustomWordBoundaryStandardTokenizer(input, characterMappings), expected);
    }
  }

  @Test
  public void customWordBoundaries_midLetter() throws IOException {
    String f_text = "%sfoo%sat%sbar%s";
    char[] charz = new char[] { '(', ')', '[', ']' };
    final Map<Character, Character> characterMappings = new HashMap<>();
    for (char chr : charz)
      characterMappings.put(chr, CustomWordBoundaryStandardTokenizerImpl.WB_CLASS_MID_LETTER);

    for (char chr : charz) {
      final String input = String.format(Locale.US, f_text, args(4, chr));
      final String[] expected = { "foo" + chr + "at" + chr + "bar" }; // e.g. # foo[at]bar
      TestHelper.assertTokens(createCustomWordBoundaryStandardTokenizer(input, characterMappings), expected);
    }
  }

  private static Tokenizer createCustomWordBoundaryStandardTokenizer(final String input,
                                                             final Map<Character, Character> characterMappings) {
    return new CustomWordBoundaryStandardTokenizer(new StringReader(input), characterMappings);
  }

  private static Object[] args(int length, char chr) {
    Object[] args = new Object[length];
    Arrays.fill(args, chr);
    return args;
  }
}
