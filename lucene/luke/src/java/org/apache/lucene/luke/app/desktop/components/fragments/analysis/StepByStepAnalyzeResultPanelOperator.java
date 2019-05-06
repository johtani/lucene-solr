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
import org.apache.lucene.luke.app.desktop.components.TableModelBase;
import org.apache.lucene.luke.models.analysis.Analysis;

/** Operator of the Step by step analyze result panel */
public interface StepByStepAnalyzeResultPanelOperator extends ComponentOperatorRegistry.ComponentOperator {

  private static String shortenName(String name) {
    return name.substring(name.lastIndexOf('.') + 1);
  }

  /** Table model for row header (display charfilter/tokenizer/filter name)  */
  final class RowHeaderTableModel extends TableModelBase<RowHeaderTableModel.Column> {

    enum Column implements TableColumnInfo {
      NAME("Name", 0, String.class, 200);

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

    RowHeaderTableModel() {
      super();
    }

    RowHeaderTableModel(List<? extends Analysis.NamedObject> namedObjects) {
      super(namedObjects.size());
      for (int i = 0; i < namedObjects.size(); i++) {
        data[i][0] = shortenName(namedObjects.get(i).getName());
      }
    }

    @Override
    protected Column[] columnInfos() {
      return Column.values();
    }
  }

  /** Table model for charfilter result */
  final class CharfilterTextTableModel extends TableModelBase<CharfilterTextTableModel.Column> {

    enum Column implements TableColumnInfo {
      TEXT("Text", 0, String.class, 1000);

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

    CharfilterTextTableModel() {
      super();
    }

    CharfilterTextTableModel(List<Analysis.CharfilteredText> charfilteredTexts) {
      super(charfilteredTexts.size());
      for (int i = 0; i < charfilteredTexts.size(); i++) {
        data[i][Column.TEXT.getIndex()] = charfilteredTexts.get(i).getText();
      }
    }

    @Override
    protected Column[] columnInfos() {
      return Column.values();
    }
  }

  /** Table model for tokenizer/filter result */
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

    //Currently this only show each tokenizer/filters result independently,
    // so the result doesn't show deletion/separation by next filter,
    // e.g. "library" by WordDelimiterFilter is different position between other output.
    NamedTokensTableModel(List<Analysis.NamedTokens> namedTokens) {
      int maxColumnSize = 0;
      Analysis.NamedTokens namedToken;
      for (Analysis.NamedTokens tokens : namedTokens) {
        namedToken = tokens;
        if (maxColumnSize < namedToken.getTokens().size()) {
          maxColumnSize = namedToken.getTokens().size();
        }
      }
      int rowSize = namedTokens.size();
      this.data = new Object[rowSize][maxColumnSize];

      for (int i = 0; i < namedTokens.size(); i++) {
        namedToken = namedTokens.get(i);
        data[i][0] = shortenName(namedToken.getName());
        for (int j = 0; j < namedToken.getTokens().size(); j++) {
          Analysis.Token token = namedToken.getTokens().get(j);
          data[i][j] = token.getTerm();
          if (maxColumnSize == namedToken.getTokens().size()) {
            columnMap.put(j, new Column(String.valueOf(j), j, String.class, 200));
          }
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
