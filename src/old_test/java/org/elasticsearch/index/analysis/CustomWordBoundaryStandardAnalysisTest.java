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

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.TestHelper;
import org.elasticsearch.common.inject.Injector;
import org.elasticsearch.common.inject.ModulesBuilder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsModule;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.EnvironmentModule;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.IndexNameModule;
import org.elasticsearch.index.settings.IndexSettingsModule;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;
import org.elasticsearch.plugin.analysis.CustomWordBoundaryStandardTokenizerPlugin;
import org.junit.Test;

public class CustomWordBoundaryStandardAnalysisTest {

  @Test
  public void standardAnalysis() throws IOException {
    String source = "@foobar Google+ is leading the way in user-generated content #social";
    String[] expected = { "foobar", "google", "leading", "way", "user", "generated", "content", "social" };
    Analyzer analyzer = initAnalysisService().analyzer("test_standard").analyzer();
    TestHelper.assertTokens(analyzer.tokenStream("_tmp", new StringReader(source)), expected);
  }

  @Test
  public void extStandardAnalysis() throws IOException {
    String source = "@foobar Google+ is leading the way in user-generated content #social";
    String[] expected = { "@foobar", "google+", "leading", "way", "user-generated", "content", "#social" };
    Analyzer analyzer = initAnalysisService().analyzer("test_standard_ext").analyzer();
    TestHelper.assertTokens(analyzer.tokenStream("_tmp", new StringReader(source)), expected);
  }

  public AnalysisService initAnalysisService() {
    Settings settings = Settings.settingsBuilder()
                                         .loadFromStream("org/elasticsearch/index/analysis/standard_ext_analysis.json", getClass().getResourceAsStream("org/elasticsearch/index/analysis/standard_ext_analysis.json"))
                                         .build();

    Index index = new Index("test_index");
    Injector parentInjector = new ModulesBuilder().add(new SettingsModule(settings),
                                                       new EnvironmentModule(new Environment(settings))).createInjector();

    AnalysisModule analysisModule = new AnalysisModule(settings,
                                                       parentInjector.getInstance(IndicesAnalysisService.class));
    new CustomWordBoundaryStandardTokenizerPlugin().onModule(analysisModule);

    Injector injector = new ModulesBuilder().add(new IndexSettingsModule(index, settings),
                                                 new IndexNameModule(index),
                                                 analysisModule).createChildInjector(parentInjector);

    return injector.getInstance(AnalysisService.class);
  }
}
