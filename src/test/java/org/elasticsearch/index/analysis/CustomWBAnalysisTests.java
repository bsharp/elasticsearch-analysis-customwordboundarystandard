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

import org.elasticsearch.test.ESTestCase;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import org.elasticsearch.Version;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.inject.Injector;
import org.elasticsearch.common.inject.ModulesBuilder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsModule;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.EnvironmentModule;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.IndexNameModule;
import org.elasticsearch.index.settings.IndexSettingsModule;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.nio.file.Path;
import java.nio.file.Files;
import java.io.StringReader;
import java.io.IOException;

import static org.elasticsearch.common.settings.Settings.Builder.EMPTY_SETTINGS;
import static org.elasticsearch.common.settings.Settings.settingsBuilder;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;

import org.elasticsearch.index.analysis.CustomWordBoundaryStandardTokenizerFactory;
import org.apache.lucene.analysis.standard.CustomWordBoundaryStandardTokenizer;
import org.elasticsearch.plugin.analysis.CustomWordBoundaryStandardTokenizerPlugin;


/**
 */
public class CustomWBAnalysisTests extends ESTestCase {

    @Test
    public void testDefaultsCustomWBAnalysis() throws IOException {
        AnalysisService analysisService = createAnalysisService();

        NamedAnalyzer analyzer = analysisService.analyzer("my_analyzer");
        assertThat(analyzer.analyzer(), instanceOf(CustomAnalyzer.class));
        assertThat(analyzer.analyzer().tokenStream(null, new StringReader("")), instanceOf(CustomWordBoundaryStandardTokenizer.class));

        String source = "@ericschmidt google+ rocks #social";
        String[] expected = new String[]{"@ericschmidt", "google+", "rocks", "#social"};
        assertSimpleTSOutput(analyzer.analyzer().tokenStream(null, new StringReader(source)), expected);
    }

    public AnalysisService createAnalysisService() throws IOException {
        Path home = createTempDir();
        Path config = home.resolve("config");
        Files.createDirectory(config);

        String json = "/org/elasticsearch/index/analysis/standardcustomwb_analysis.json";
        Settings settings = Settings.settingsBuilder()
                .put("path.home", home)
                .loadFromStream(json, getClass().getResourceAsStream(json))
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .build();

        Index index = new Index("test");

        Injector parentInjector = new ModulesBuilder().add(new SettingsModule(settings),
                new EnvironmentModule(new Environment(settings)))
                .createInjector();

        AnalysisModule analysisModule = new AnalysisModule(settings, parentInjector.getInstance(IndicesAnalysisService.class));
        new CustomWordBoundaryStandardTokenizerPlugin().onModule(analysisModule);

        Injector injector = new ModulesBuilder().add(
                new IndexSettingsModule(index, settings),
                new IndexNameModule(index),
                analysisModule)
                .createChildInjector(parentInjector);

        return injector.getInstance(AnalysisService.class);
    }

    public static void assertSimpleTSOutput(TokenStream stream,
                                            String[] expected) throws IOException {
        stream.reset();
        CharTermAttribute termAttr = stream.getAttribute(CharTermAttribute.class);
        assertThat(termAttr, notNullValue());
        int i = 0;
        while (stream.incrementToken()) {
            assertThat(expected.length, greaterThan(i));
            assertThat( "expected different term at index " + i, expected[i++], equalTo(termAttr.toString()));
        }
        assertThat("not all tokens produced", i, equalTo(expected.length));
    }
}
