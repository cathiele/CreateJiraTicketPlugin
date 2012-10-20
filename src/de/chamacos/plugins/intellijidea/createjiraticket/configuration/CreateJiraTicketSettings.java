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

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.BasicProject;
import com.atlassian.jira.rest.client.domain.IssueType;
import com.atlassian.jira.rest.client.domain.Priority;
import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * (C) Carsten Thiele / ct 13.10.12 13:40
 */
public class CreateJiraTicketSettings implements Configurable, DocumentListener, ItemListener {
    private JPasswordField jiraPasswordPasswordField;
    private JPanel mainSettingsPanel;
    private JTextField jiraUsernameTextField;
    private JTextField summaryTextField;
    private JComboBox priorityComboBox;
    private JList rulesList;
    private JButton addRuleButton;
    private JButton removeRuleButton;
    private JButton duplicateRuleButton;
    private JTextField jiraUrlTextField;
    private JComboBox jiraProjectsComboBox;
    private JButton reloadProjectListButton;
    private JTextField ruleNameTextField;
    private JTextField commentPatternTextField;
    private JTextField commentReplaceTextField;
    private JComboBox issueTypeComboBox;
    private JTextField labelsTextField;

    private boolean modified = false;
    private Project project;
    private ArrayList<BasicProject> projects;
    private CommentRule currentSelectedRule = null;
    private RuleChangeListener ruleChangeListener;

    public CreateJiraTicketSettings(Project project) {
        this.project = project;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Create Jira Ticket";
    }

    @Override
    public Icon getIcon() {
        return null;  // TODO (Maybe) Create Icon for the Plugin?
    }

    @Override
    public String getHelpTopic() {
        return null;  // TODO implement Help Text
    }

    public void setSelectedRule(int index) {
        if (index == -1) {
            currentSelectedRule = null;
            updateViewWithSelectedRule();
        } else {
            CreateJiraTicketPluginConfiguration config = CreateJiraTicketPluginConfiguration.getInstance(project);
            currentSelectedRule = config.getCommentRules().get(index);
            updateViewWithSelectedRule();
        }
    }

    private void changeStateofRuleFields(boolean state) {
        ruleNameTextField.setEnabled(state);
        commentPatternTextField.setEnabled(state);
        commentReplaceTextField.setEnabled(state);
        issueTypeComboBox.setEnabled(state);
        summaryTextField.setEnabled(state);
        priorityComboBox.setEnabled(state);
        labelsTextField.setEnabled(state);
    }

    public void updateViewWithSelectedRule() {
        ruleChangeListener.setActive(false);
        if (currentSelectedRule == null) {
            changeStateofRuleFields(false);
            ruleNameTextField.setText("");
            commentPatternTextField.setText("");
            commentReplaceTextField.setText("");
            issueTypeComboBox.setSelectedItem(null);
            summaryTextField.setText("");
            priorityComboBox.setSelectedItem(null);
            labelsTextField.setText("");
        } else {
            changeStateofRuleFields(true);
            ruleNameTextField.setText(currentSelectedRule.getName());
            commentPatternTextField.setText(currentSelectedRule.getCommentExpression());
            commentReplaceTextField.setText(currentSelectedRule.getCommentTemplate());
            issueTypeComboBox.setSelectedItem(getIssueTypeForName(issueTypeComboBox.getModel(), currentSelectedRule.getJiraIssueType()));
            summaryTextField.setText(currentSelectedRule.getJiraSummaryTemplate());
            priorityComboBox.setSelectedItem(getPriorityForName(priorityComboBox.getModel(), currentSelectedRule.getJiraPriority()));
            labelsTextField.setText(currentSelectedRule.getJiraLabels());
            ruleChangeListener.setActive(true);
        }

    }

    private Priority getPriorityForName(ComboBoxModel model, String priorityName) {
        for (int i = 0; i < model.getSize(); i++) {
            Priority priority = (Priority) model.getElementAt(i);
            if (priority.getName().equals(priorityName)) {
                return priority;
            }
        }
        return null;
    }

    private IssueType getIssueTypeForName(ComboBoxModel model, String issueTypeName) {

        for (int i = 0; i < model.getSize(); i++) {
            IssueType issueType = (IssueType) model.getElementAt(i);
            if (issueType.getName().equals(issueTypeName)) {
                return issueType;
            }
        }
        return null;
    }


    public void addNewRule() {
        CommentRule rule = new CommentRule();
        rule.setName("New Rule");
        rule.setCommentExpression("^// TODO (.*)$");
        rule.setCommentTemplate("// TODO {Key} {Summary}");
        rule.setJiraSummaryTemplate("$1");
        CreateJiraTicketPluginConfiguration config = CreateJiraTicketPluginConfiguration.getInstance(project);
        config.getCommentRules().add(rule);
        rulesList.setModel(new RuleListModel(config.getCommentRules()));
        rulesList.setSelectedIndex(config.getCommentRules().size()); // TODO not working, how can i select a rule?
    }

    public void removeSelectedRule() {
        if (rulesList.getSelectedIndex() >= 0) {
            CreateJiraTicketPluginConfiguration config = CreateJiraTicketPluginConfiguration.getInstance(project);
            config.getCommentRules().remove(rulesList.getSelectedIndex());
            rulesList.setModel(new RuleListModel(config.getCommentRules()));
        }
    }

    public void duplicateSelectedRule() {
        if (rulesList.getSelectedIndex() >= 0) {
            CommentRule duplicateRule = new CommentRule();

            duplicateRule.setName(currentSelectedRule.getName());
            duplicateRule.setCommentExpression((currentSelectedRule.getCommentExpression()));
            duplicateRule.setCommentTemplate(currentSelectedRule.getCommentTemplate());
            duplicateRule.setJiraIssueType(currentSelectedRule.getJiraIssueType());
            duplicateRule.setJiraSummaryTemplate(currentSelectedRule.getJiraSummaryTemplate());
            duplicateRule.setJiraPriority(currentSelectedRule.getJiraPriority());
            duplicateRule.setJiraLabels(currentSelectedRule.getJiraLabels());

            CreateJiraTicketPluginConfiguration config = CreateJiraTicketPluginConfiguration.getInstance(project);
            config.getCommentRules().add(duplicateRule);
            rulesList.setModel(new RuleListModel(config.getCommentRules()));
            rulesList.setSelectedIndex(config.getCommentRules().size()); // TODO not working, how can i select a rule?
        }
    }

    @Override
    public JComponent createComponent() {
        jiraUrlTextField.getDocument().addDocumentListener(this);
        jiraUsernameTextField.getDocument().addDocumentListener(this);
        jiraPasswordPasswordField.getDocument().addDocumentListener(this);
        jiraProjectsComboBox.addItemListener(this);
        jiraProjectsComboBox.setRenderer(new JiraProjectComboBoxCellRenderer());

        priorityComboBox.setRenderer(new PriorityComboBoxCellRenderer());
        issueTypeComboBox.setRenderer(new IssueTypeComboBoxCellRenderer());
        rulesList.setCellRenderer(new RuleListCellRenderer());
        rulesList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                setSelectedRule(rulesList.getSelectedIndex());
            }
        });
        reloadProjectListButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                fetchProjectsFromServer();
            }
        });

        addRuleButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                addNewRule();
            }
        });

        removeRuleButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                removeSelectedRule();
            }
        });

        duplicateRuleButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                duplicateSelectedRule();
            }
        });

        ruleChangeListener = new RuleChangeListener(this);
        ruleNameTextField.getDocument().addDocumentListener(ruleChangeListener);
        commentPatternTextField.getDocument().addDocumentListener(ruleChangeListener);
        commentReplaceTextField.getDocument().addDocumentListener(ruleChangeListener);
        issueTypeComboBox.addItemListener(ruleChangeListener);
        summaryTextField.getDocument().addDocumentListener(ruleChangeListener);
        priorityComboBox.addItemListener(ruleChangeListener);
        labelsTextField.getDocument().addDocumentListener(ruleChangeListener);

        CreateJiraTicketPluginConfiguration config = CreateJiraTicketPluginConfiguration.getInstance(this.project);
        rulesList.setModel(new RuleListModel(config.getCommentRules()));
        rulesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fetchProjectsFromServer(config);
        mapSettingsToForm();
        updateViewWithSelectedRule();

        return this.mainSettingsPanel;
    }

    private void mapSettingsToForm() {

        CreateJiraTicketPluginConfiguration config = CreateJiraTicketPluginConfiguration.getInstance(this.project);
        jiraUrlTextField.setText(config.getJiraUrl());
        jiraUsernameTextField.setText(config.getUsername());
        jiraPasswordPasswordField.setText(config.getPassword());
        jiraProjectsComboBox.setSelectedItem(findProjectByKey(config.getProject()));

        // Hint: add new Attributes here
    }

    private BasicProject findProjectByKey(String projectKey) {
        if (projects == null) {
            return null;
        }
        for (BasicProject currentProject : projects) {
            if (projectKey.equals(currentProject.getKey())) {
                return currentProject;
            }
        }
        return null;
    }

    private boolean jiraServerSettingsComplete() {
        return !"".equals(jiraUrlTextField.getText()) && !"".equals(jiraUsernameTextField.getText()) && !"".equals(new String(jiraPasswordPasswordField.getPassword()));
    }

    private void fetchProjectsFromServer(CreateJiraTicketPluginConfiguration config) {

        if (!config.jiraServerSettingsComplete()) {
            projects = new ArrayList<BasicProject>();
            jiraProjectsComboBox.setModel(new ProjectComboBoxModel(projects));
            jiraProjectsComboBox.setEnabled(false);
        }
        URI jiraServerUri;
        try {
            jiraServerUri = new URI(config.getJiraUrl());
        } catch (Exception e) {
            // TODO Exception Handling
            return;
        }
        String username = config.getUsername();
        String password = config.getPassword();

        fetchInformationsFromServer(jiraServerUri, username, password);
    }

    private void fetchProjectsFromServer() {
        if (!jiraServerSettingsComplete()) {
            projects = new ArrayList<BasicProject>();
            jiraProjectsComboBox.setModel(new ProjectComboBoxModel(projects));
            jiraProjectsComboBox.setEnabled(false);
        }
        String username = jiraUsernameTextField.getText();
        String password = new String(jiraPasswordPasswordField.getPassword());
        URI jiraServerUri;
        try {
            jiraServerUri = new URI(jiraUrlTextField.getText());
        } catch (URISyntaxException e) {
            e.printStackTrace();  // TODO Exception Handling
            return;
        }
        fetchInformationsFromServer(jiraServerUri, username, password);
    }

    private void fetchInformationsFromServer(URI jiraServerUri, String username, String password) {
        JerseyJiraRestClientFactory factory = new JerseyJiraRestClientFactory();
        JiraRestClient restClient = factory.createWithBasicHttpAuthentication(jiraServerUri, username, password);
        com.atlassian.jira.rest.client.ProgressMonitor monitor = new NullProgressMonitor();

        Iterable<BasicProject> allProjects;
        try {
            allProjects = restClient.getProjectClient().getAllProjects(monitor);
            projects = new ArrayList<BasicProject>();
        } catch (Exception e) {
            ProjectComboBoxModel projectComboBoxModel = new ProjectComboBoxModel(projects);
            jiraProjectsComboBox.setModel(projectComboBoxModel);
            jiraProjectsComboBox.setEnabled(false);
            return; // => Zugangsdaten oder URL nicht korrekt
        }
        for (BasicProject project : allProjects) {
            projects.add(project);
        }
        Collections.sort(projects, new Comparator<BasicProject>() {
            @Override
            public int compare(BasicProject basicProject1, BasicProject basicProject2) {
                if (basicProject1 == null || basicProject2 == null) {
                    return 0;
                }
                if (basicProject1.getName() == null || basicProject2.getName() == null) {
                    return 0;
                }
                return basicProject1.getName().compareToIgnoreCase(basicProject2.getName());
            }
        });
        ProjectComboBoxModel projectComboBoxModel = new ProjectComboBoxModel(projects);
        jiraProjectsComboBox.setModel(projectComboBoxModel);
        jiraProjectsComboBox.setEnabled(true);

        Iterable<Priority> prioritiesIteratable = restClient.getMetadataClient().getPriorities(monitor);
        ArrayList<Priority> priorities = new ArrayList<Priority>();
        for (Priority priority : prioritiesIteratable) {
            priorities.add(priority);
        }
        priorityComboBox.setModel(new PriorityComboBoxModel(priorities));
        if (currentSelectedRule != null && currentSelectedRule.getJiraPriority() != null) {
            priorityComboBox.setSelectedItem(getPriorityForName(priorityComboBox.getModel(), currentSelectedRule.getJiraPriority()));
        }


        Iterable<IssueType> issueTypesIteratable = restClient.getMetadataClient().getIssueTypes(monitor);
        ArrayList<IssueType> issueTypes = new ArrayList<IssueType>();
        for (IssueType issueType : issueTypesIteratable) {
            issueTypes.add(issueType);
        }
        Collections.sort(issueTypes, new Comparator<IssueType>() {
            @Override
            public int compare(IssueType issueType1, IssueType issueType2) {
                return issueType1.getName().compareToIgnoreCase(issueType2.getName());
            }
        });
        issueTypeComboBox.setModel(new IssueTypeComboBoxModel(issueTypes));
        if (currentSelectedRule != null && currentSelectedRule.getJiraIssueType() != null) {
            issueTypeComboBox.setSelectedItem(getIssueTypeForName(issueTypeComboBox.getModel(), currentSelectedRule.getJiraIssueType()));
        }
    }

    @Override
    public boolean isModified() {
        return this.modified;
    }

    @Override
    public void apply() throws ConfigurationException {
        mapFormToSettings();

        // Reload Data from Commited Config
        CreateJiraTicketPluginConfiguration config = CreateJiraTicketPluginConfiguration.getInstance(this.project);
        fetchProjectsFromServer(config);
        mapSettingsToForm();

        this.modified = false;
    }

    private void mapFormToSettings() {
        CreateJiraTicketPluginConfiguration config = CreateJiraTicketPluginConfiguration.getInstance(this.project);
        config.setJiraUrl(jiraUrlTextField.getText());
        config.setUsername(jiraUsernameTextField.getText());
        config.setPassword(new String(jiraPasswordPasswordField.getPassword()));

        if (jiraProjectsComboBox.getSelectedItem() != null) {
            config.setProject(((BasicProject) jiraProjectsComboBox.getSelectedItem()).getKey());
        } else {
            config.setProject("");
        }

        modified = false;
        // TODO Implement
    }

    @Override
    public void reset() {
        mapSettingsToForm();
        this.modified = false;
    }

    @Override
    public void disposeUIResources() {
    }

    private void createUIComponents() {
        // place custom component creation code here
    }

    @Override
    public void insertUpdate(DocumentEvent documentEvent) {
        modified = true;
    }

    @Override
    public void removeUpdate(DocumentEvent documentEvent) {
        modified = true;
    }

    @Override
    public void changedUpdate(DocumentEvent documentEvent) {
        modified = true;
    }

    @Override
    public void itemStateChanged(ItemEvent itemEvent) {
        modified = true;

    }

    public JTextField getSummaryTextField() {
        return summaryTextField;
    }

    public JComboBox getPriorityComboBox() {
        return priorityComboBox;
    }

    public JTextField getRuleNameTextField() {
        return ruleNameTextField;
    }

    public JTextField getCommentPatternTextField() {
        return commentPatternTextField;
    }

    public JTextField getCommentReplaceTextField() {
        return commentReplaceTextField;
    }

    public JComboBox getIssueTypeComboBox() {
        return issueTypeComboBox;
    }

    public JTextField getLabelsTextField() {
        return labelsTextField;
    }

    public CommentRule getCurrentSelectedRule() {
        return currentSelectedRule;
    }

    public JList getRulesList() {
        return rulesList;
    }
}
