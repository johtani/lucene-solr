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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import java.awt.BorderLayout;
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

public class StepByStepAnalyzeResultPanelProvider implements StepByStepAnalyzeResultPanelOperator {


  private final ComponentOperatorRegistry operatorRegistry;

  private final TokenAttributeDialogFactory tokenAttrDialogFactory;

  private final JTable namedTokensTable = new JTable();

  private final ListenerFunctions listeners = new ListenerFunctions();

  private Analysis analysisModel;

  private List<Analysis.NamedTokens> namedTokens;

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

    TableUtils.setupTable(namedTokensTable, ListSelectionModel.SINGLE_SELECTION, new StepByStepAnalyzeResultPanelOperator.NamedTokensTableModel(),
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            listeners.showAttributeValues(e);
          }
        });
    panel.add(new JScrollPane(namedTokensTable), BorderLayout.CENTER);

    return panel;
  }


  @Override
  public void setAnalysisModel(Analysis analysisModel) {
    this.analysisModel = analysisModel;
  }

  @Override
  public void executeAnalysisStepByStep(String text) {
    namedTokens = analysisModel.analyzeStepByStep(text);
    NamedTokensTableModel tableModel = new StepByStepAnalyzeResultPanelOperator.NamedTokensTableModel(namedTokens);
    namedTokensTable.setModel(tableModel);
    namedTokensTable.setShowGrid(true);
    for (int i = 0; i < namedTokens.size(); i++) {
      namedTokensTable.getColumnModel().getColumn(i).setPreferredWidth(tableModel.getColumnWidth(i));
    }
  }

  @Override
  public void clearTable() {
    TableUtils.setupTable(namedTokensTable, ListSelectionModel.SINGLE_SELECTION, new NamedTokensTableModel(),
        null);
  }

  private void showAttributeValues(int rowIndex, int columnIndex) {
    Analysis.NamedTokens namedTokens = this.namedTokens.get(columnIndex);
    List<Analysis.Token> tokens = namedTokens.getTokens();

    if (rowIndex <= tokens.size()) {
      String term = "\"" + tokens.get(rowIndex).getTerm() + "\" BY " + namedTokens.getName();
      List<Analysis.TokenAttribute> attributes = tokens.get(rowIndex).getAttributes();
      new DialogOpener<>(tokenAttrDialogFactory).open("Token Attributes", 650, 400,
          factory -> {
            factory.setTerm(term);
            factory.setAttributes(attributes);
          });
    }
    // TODO show error dialog? if click cell that has no data
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
