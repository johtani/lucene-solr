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

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.lucene.luke.app.desktop.components.ComponentOperatorRegistry;
import org.apache.lucene.luke.app.desktop.components.TableColumnInfo;
import org.apache.lucene.luke.models.analysis.Analysis;

/** Operator of the simple analyze result panel */
public interface StepByStepAnalyzeResultPanelOperator extends ComponentOperatorRegistry.ComponentOperator {

  final class NamedTokensTableModel extends AbstractTableModel {

    class Column implements TableColumnInfo {

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

    private final Map<Integer, Column> columnMap = new TreeMap<>();

    private final Object[][] data;

    NamedTokensTableModel() {
      this.data = new Object[0][0];
    }

    NamedTokensTableModel(List<Analysis.NamedTokens> namedTokens) {
      int maxLength = 0;
      Analysis.NamedTokens namedToken;
      for (int i = 0; i < namedTokens.size(); i++) {
        namedToken = namedTokens.get(i);
        columnMap.put(i, new Column(namedToken.getName(), i, String.class, 200));
        if (maxLength < namedToken.getTokens().size()) {
          maxLength = namedToken.getTokens().size();
        }
      }
      this.data = new Object[maxLength][namedTokens.size()];

      for (int i = 0; i < namedTokens.size(); i++) {
        namedToken = namedTokens.get(i);
        for (int j = 0; j < namedToken.getTokens().size(); j++) {
          Analysis.Token token = namedToken.getTokens().get(j);
          data[j][i] = token.getTerm();
        }
      }
    }

    @Override
    public int getRowCount() {
      return data.length;
    }

    @Override
    public int getColumnCount() {
      return columnMap.size();
    }


    @Override
    public String getColumnName(int colIndex) {
      if (columnMap.containsKey(colIndex)) {
        return columnMap.get(colIndex).getColName();
      }
      return "";
    }

    @Override
    public Class<?> getColumnClass(int colIndex) {
      if (columnMap.containsKey(colIndex)) {
        return columnMap.get(colIndex).getType();
      }
      return Object.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      return data[rowIndex][columnIndex];
    }

    public int getColumnWidth(int columnIndex) {
      return columnMap.get(columnIndex).getColumnWidth();
    }
  }

  void setAnalysisModel(Analysis analysisModel);

  void executeAnalysisStepByStep(String text);

  void clearTable();
}
