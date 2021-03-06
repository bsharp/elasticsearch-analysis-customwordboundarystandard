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

package org.elasticsearch.index.analysis;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.CustomWordBoundaryStandardTokenizer;
import org.apache.lucene.analysis.standard.CustomWordBoundaryStandardTokenizerImpl;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.settings.IndexSettingsService;
import org.elasticsearch.common.settings.Settings;
import org.apache.lucene.util.Version;


@AnalysisSettingsRequired
public class CustomWordBoundaryStandardTokenizerFactory extends AbstractTokenizerFactory {

  private final Map<Character,Character> characterMappings;

  @Inject
  public CustomWordBoundaryStandardTokenizerFactory(Index index, IndexSettingsService indexSettingsService, Environment environment, @Assisted String name, @Assisted Settings settings) {
    super(index, indexSettingsService.getSettings(), name, settings);

    List<String> rules = Analysis.getWordList(environment, settings, "mappings");
    if (rules == null) {
      throw new IllegalArgumentException("mapping requires either `mappings` or `mappings_path` to be configured");
    }

    Map<Character, Character> map = new HashMap<>();
    parseRules(rules, map);
    characterMappings = Collections.unmodifiableMap(map);
  }

  @Override
  public Tokenizer create() {
    if (version.onOrAfter(Version.LUCENE_5_5_0)) {
      return new CustomWordBoundaryStandardTokenizer(characterMappings);
    } else {
      return new StandardTokenizer();
    }
  }

  // source => target
  private static Pattern rulePattern = Pattern.compile("(.*)\\s*=>\\s*(.*)\\s*$");

  /**
   * parses a list of MappingCharFilter style rules into a normalize char map
   */
  private void parseRules(List<String> rules, Map<Character, Character> map) {
    for (String rule : rules) {
      Matcher m = rulePattern.matcher(rule);
      if (!m.find())
        throw new RuntimeException("Invalid Mapping Rule : [" + rule + "]");
      String lhs = parseString(m.group(1).trim());
      String rhs = parseString(m.group(2).trim());
      if (lhs == null || rhs == null || lhs.length() > 1)
        throw new RuntimeException("Invalid Mapping Rule : [" + rule + "]. Illegal mapping.");

      try {
        map.put(lhs.charAt(0), translateWordBoundary(rhs));
      }
      catch (IllegalArgumentException iae) {
        throw new RuntimeException("Invalid Mapping Rule : [" + rule + "]. Unrecognized WordBoundary property value");
      }
    }
  }

  public static enum WBProperty {
    L, N, EXNL, MNL, MN, ML, SQ, DQ, BRK
  }

  private Character translateWordBoundary(String mapping) throws IllegalArgumentException {
    WBProperty property = WBProperty.valueOf(mapping);
    switch (property) {
      case L:
        return CustomWordBoundaryStandardTokenizerImpl.WB_CLASS_LETTER;
      case N:
        return CustomWordBoundaryStandardTokenizerImpl.WB_CLASS_NUMERIC;
      case EXNL:
        return CustomWordBoundaryStandardTokenizerImpl.WB_CLASS_EXTENDED_NUM_LETTER;
      case MNL:
        return CustomWordBoundaryStandardTokenizerImpl.WB_CLASS_MID_NUMBER_LETTER;
      case MN:
        return CustomWordBoundaryStandardTokenizerImpl.WB_CLASS_MID_NUMBER;
      case ML:
        return CustomWordBoundaryStandardTokenizerImpl.WB_CLASS_MID_LETTER;
      case SQ:
        return CustomWordBoundaryStandardTokenizerImpl.WB_CLASS_SINGLE_QUOTE;
      case DQ:
        return CustomWordBoundaryStandardTokenizerImpl.WB_CLASS_DOUBLE_QUOTE;
      case BRK:
        return CustomWordBoundaryStandardTokenizerImpl.WB_CLASS_BREAK;
      default:
        throw new IllegalArgumentException();
    }
  }

  /**
   * Copied from {@link org.elasticsearch.index.analysis.MappingCharFilterFactory}
   */
  char[] out = new char[256];
  private String parseString(String s) {
    int readPos = 0;
    int len = s.length();
    int writePos = 0;
    while (readPos < len) {
      char c = s.charAt(readPos++);
      if (c == '\\') {
        if (readPos >= len)
          throw new RuntimeException("Invalid escaped char in [" + s + "]");
        c = s.charAt(readPos++);
        switch (c) {
          case '\\':
            c = '\\';
            break;
          case 'n':
            c = '\n';
            break;
          case 't':
            c = '\t';
            break;
          case 'r':
            c = '\r';
            break;
          case 'b':
            c = '\b';
            break;
          case 'f':
            c = '\f';
            break;
          case 'u':
            if (readPos + 3 >= len)
              throw new RuntimeException("Invalid escaped char in [" + s + "]");
            c = (char) Integer.parseInt(s.substring(readPos, readPos + 4), 16);
            readPos += 4;
            break;
        }
      }
      out[writePos++] = c;
    }
    return new String(out, 0, writePos);
  }
}
