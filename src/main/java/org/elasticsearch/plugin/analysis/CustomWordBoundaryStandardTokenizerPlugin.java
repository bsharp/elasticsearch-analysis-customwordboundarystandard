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

package org.elasticsearch.plugin.analysis;

import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.index.analysis.CustomWordBoundaryStandardTokenizerFactory;
import org.elasticsearch.plugins.Plugin;

public class CustomWordBoundaryStandardTokenizerPlugin extends Plugin {

  @Override
  public String name() {
    return "analysis-standard-customwb";
  }

  @Override
  public String description() {
    return "An extension to Lucene's standard tokenizer that supports custom character mappings to override word boundary property values - ES 2.x";
  }

  public void onModule(AnalysisModule module) {
    module.addTokenizer("standard_customwb", CustomWordBoundaryStandardTokenizerFactory.class);
  }
}
