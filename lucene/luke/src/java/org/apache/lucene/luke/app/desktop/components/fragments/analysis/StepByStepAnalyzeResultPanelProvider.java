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

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import org.apache.lucene.luke.app.desktop.components.ComponentOperatorRegistry;
import org.apache.lucene.luke.app.desktop.components.dialog.analysis.TokenAttributeDialogFactory;
import org.apache.lucene.luke.app.desktop.util.DialogOpener;
import org.apache.lucene.luke.app.desktop.util.MessageUtils;
import org.apache.lucene.luke.app.desktop.util.TableUtils;
import org.apache.lucene.luke.models.analysis.Analysis;

/** Provider of the Step by step analyze result panel */
public class StepByStepAnalyzeResultPanelProvider implements StepByStepAnalyzeResultPanelOperator {

  private final ComponentOperatorRegistry operatorRegistry;

  private final TokenAttributeDialogFactory tokenAttrDialogFactory;

  private final JTable charfilterTextsTable = new JTable();

  private final JTable charfilterTextsRowHeader = new JTable();

  private final JTable namedTokensTable = new JTable();

  private final JTable namedTokensRowHeader = new JTable();

  private final ListenerFunctions listeners = new ListenerFunctions();

  private Analysis analysisModel;

  private Analysis.StepByStepResult result;

  public StepByStepAnalyzeResultPanelProvider(TokenAttributeDialogFactory tokenAttrDialogFactory) {
    this.operatorRegistry = ComponentOperatorRegistry.getInstance();
    operatorRegistry.register(StepByStepAnalyzeResultPanelOperator.class, this);
    this.tokenAttrDialogFactory = tokenAttrDialogFactory;
  }

  public JPanel get() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setOpaque(false);

    JPanel hint = new JPanel(new FlowLayout(FlowLayout.LEADING));
    hint.setOpaque(false);
    hint.add(new JLabel(MessageUtils.getLocalizedMessage("analysis.hint.show_attributes_step_by_step")));
    panel.add(hint, BorderLayout.PAGE_START);

    JPanel inner = new JPanel();
    inner.setLayout(new BoxLayout(inner, BoxLayout.PAGE_AXIS));

    TableUtils.setupTable(charfilterTextsRowHeader, ListSelectionModel.SINGLE_SELECTION, new RowHeaderTableModel(),
        null);
    TableUtils.setupTable(charfilterTextsTable, ListSelectionModel.SINGLE_SELECTION, new CharfilterTextTableModel(),
        null);
    inner.add(initResultScroll(panel, charfilterTextsTable, charfilterTextsRowHeader));

    TableUtils.setupTable(namedTokensRowHeader, ListSelectionModel.SINGLE_SELECTION, new RowHeaderTableModel(),
        null);
    TableUtils.setupTable(namedTokensTable, ListSelectionModel.SINGLE_SELECTION, new NamedTokensTableModel(),
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            listeners.showAttributeValues(e);
          }
        });
    namedTokensTable.setColumnSelectionAllowed(true);
    inner.add(initResultScroll(panel, namedTokensTable, namedTokensRowHeader));
    panel.add(inner, BorderLayout.CENTER);
    return panel;
  }

  private JScrollPane initResultScroll(JPanel panel, JTable table, JTable header) {
    JScrollPane scroll = new JScrollPane(table);
    scroll.setRowHeaderView(header);
    scroll.setCorner(JScrollPane.UPPER_LEFT_CORNER, header.getTableHeader());
    Dimension tsz = new Dimension(200, header.getPreferredSize().height);
    scroll.getRowHeader().setPreferredSize(tsz);
    return scroll;
  }


  @Override
  public void setAnalysisModel(Analysis analysisModel) {
    this.analysisModel = analysisModel;
  }

  @Override
  public void executeAnalysisStepByStep(String text) {
    result = analysisModel.analyzeStepByStep(text);
    RowHeaderTableModel charfilterTextsHeaderModel = new RowHeaderTableModel(result.getCharfilteredTexts());
    charfilterTextsRowHeader.setModel(charfilterTextsHeaderModel);
    charfilterTextsRowHeader.setShowGrid(true);

    CharfilterTextTableModel charfilterTextTableModel = new CharfilterTextTableModel(result.getCharfilteredTexts());
    charfilterTextsTable.setModel(charfilterTextTableModel);
    charfilterTextsTable.setShowGrid(true);

    RowHeaderTableModel namedTokensHeaderModel = new RowHeaderTableModel(result.getNamedTokens());
    namedTokensRowHeader.setModel(namedTokensHeaderModel);
    namedTokensRowHeader.setShowGrid(true);

    NamedTokensTableModel tableModel = new NamedTokensTableModel(result.getNamedTokens());
    namedTokensTable.setModel(tableModel);
    namedTokensTable.setShowGrid(true);
    for (int i = 0; i < tableModel.getColumnCount(); i++) {
      namedTokensTable.getColumnModel().getColumn(i).setPreferredWidth(tableModel.getColumnWidth(i));
    }
  }

  @Override
  public void clearTable() {
    TableUtils.setupTable(charfilterTextsRowHeader, ListSelectionModel.SINGLE_SELECTION, new RowHeaderTableModel(),
        null);
    TableUtils.setupTable(charfilterTextsTable, ListSelectionModel.SINGLE_SELECTION, new CharfilterTextTableModel(),
        null);

    TableUtils.setupTable(namedTokensRowHeader, ListSelectionModel.SINGLE_SELECTION, new RowHeaderTableModel(),
        null);
    TableUtils.setupTable(namedTokensTable, ListSelectionModel.SINGLE_SELECTION, new NamedTokensTableModel(),
        null);
  }

  private void showAttributeValues(int rowIndex, int columnIndex) {
    Analysis.NamedTokens namedTokens =
        this.result.getNamedTokens().get(rowIndex - this.result.getCharfilteredTexts().size());
    List<Analysis.Token> tokens = namedTokens.getTokens();

    if (rowIndex <= tokens.size()) {
      String term = "\"" + tokens.get(columnIndex).getTerm() + "\" BY " + namedTokens.getName();
      List<Analysis.TokenAttribute> attributes = tokens.get(columnIndex).getAttributes();
      new DialogOpener<>(tokenAttrDialogFactory).open("Token Attributes", 650, 400,
          factory -> {
            factory.setTerm(term);
            factory.setAttributes(attributes);
          });
    }
  }

  private class ListenerFunctions {
    void showAttributeValues(MouseEvent e) {
      if (e.getClickCount() != 2 || e.isConsumed()) {
        return;
      }
      int rowIndex = namedTokensTable.rowAtPoint(e.getPoint());
      int columnIndex = namedTokensTable.columnAtPoint(e.getPoint());
      if (rowIndex < 0 || rowIndex >= namedTokensTable.getRowCount()) {
        return;
      } else if (columnIndex < 0 || columnIndex >= namedTokensTable.getColumnCount()) {
        return;
      }
      StepByStepAnalyzeResultPanelProvider.this.showAttributeValues(rowIndex, columnIndex);
    }
  }
}
