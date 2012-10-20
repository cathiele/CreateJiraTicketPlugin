/*
   Copyright 2012 Carsten Thiele

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License./**
*/
package de.chamacos.plugins.intellijidea.createjiraticket.configuration;

import com.atlassian.jira.rest.client.domain.IssueType;
import com.atlassian.jira.rest.client.domain.Priority;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * (C) Carsten Thiele / ct 14.10.12 13:15
 */
public class RuleChangeListener implements DocumentListener, ItemListener {
    private CreateJiraTicketSettings delegate;
    private boolean active = false;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public RuleChangeListener(CreateJiraTicketSettings createJiraTicketSettings) {
        this.delegate = createJiraTicketSettings;
    }

    @Override
    public void insertUpdate(DocumentEvent documentEvent) {
        updateModel();
    }

    @Override
    public void removeUpdate(DocumentEvent documentEvent) {
        updateModel();
    }

    @Override
    public void changedUpdate(DocumentEvent documentEvent) {
        updateModel();
    }

    @Override
    public void itemStateChanged(ItemEvent itemEvent) {
        updateModel();
    }

    private void updateModel() {
        if (active) {

            if (delegate.getCurrentSelectedRule() != null && delegate.getCurrentSelectedRule().getName() != null && !delegate.getCurrentSelectedRule().getName().equals(delegate.getRuleNameTextField().getText())) {
                delegate.getRulesList().updateUI();
            }
            delegate.getCurrentSelectedRule().setName(delegate.getRuleNameTextField().getText());
            delegate.getCurrentSelectedRule().setCommentExpression(delegate.getCommentPatternTextField().getText());
            delegate.getCurrentSelectedRule().setCommentTemplate(delegate.getCommentReplaceTextField().getText());
            if (delegate.getIssueTypeComboBox().getSelectedItem() != null) {
                delegate.getCurrentSelectedRule().setJiraIssueType(((IssueType) delegate.getIssueTypeComboBox().getSelectedItem()).getName());
            }
            delegate.getCurrentSelectedRule().setJiraSummaryTemplate(delegate.getSummaryTextField().getText());
            if (delegate.getPriorityComboBox().getSelectedItem() != null) {
                delegate.getCurrentSelectedRule().setJiraPriority(((Priority) delegate.getPriorityComboBox().getSelectedItem()).getName());
            }
            delegate.getCurrentSelectedRule().setJiraLabels(delegate.getLabelsTextField().getText());
        }
    }

}
