/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.lucene.luke.app.desktop.components.fragments.analysis;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.lucene.luke.app.desktop.components.ComponentOperatorRegistry;
import org.apache.lucene.luke.app.desktop.components.TableColumnInfo;
import org.apache.lucene.luke.app.desktop.components.TableModelBase;
import org.apache.lucene.luke.models.analysis.Analysis;

/** Operator of the simple analyze result panel */
public interface SimpleAnalyzeResultPanelOperator extends ComponentOperatorRegistry.ComponentOperator  {

  /** Table model for simple result */
  final class TokensTableModel extends TableModelBase<TokensTableModel.Column> {

    enum Column implements TableColumnInfo {
      TERM("Term", 0, String.class, 150),
      ATTR("Attributes", 1, String.class, 1000);

      private final String colName;
      private final int index;
      private final Class<?> type;
      private final int width;

      Column(String colName, int index, Class<?> type, int width) {
        this.colName = colName;
        this.index = index;
        this.type = type;
        this.width = width;
      }

      @Override
      public String getColName() {
        return colName;
      }

      @Override
      public int getIndex() {
        return index;
      }

      @Override
      public Class<?> getType() {
        return type;
      }

      @Override
      public int getColumnWidth() {
        return width;
      }
    }

    TokensTableModel() {
      super();
    }

    TokensTableModel(List<Analysis.Token> tokens) {
      super(tokens.size());
      for (int i = 0; i < tokens.size(); i++) {
        Analysis.Token token = tokens.get(i);
        data[i][Column.TERM.getIndex()] = token.getTerm();
        List<String> attValues = token.getAttributes().stream()
            .flatMap(att -> att.getAttValues().entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue()))
            .collect(Collectors.toList());
        data[i][Column.ATTR.getIndex()] = String.join(",", attValues);
      }
    }

    @Override
    protected Column[] columnInfos() {
      return Column.values();
    }
  }

  void setAnalysisModel(Analysis analysisModel);

  void executeAnalysis(String text);

  void clearTable();
}
