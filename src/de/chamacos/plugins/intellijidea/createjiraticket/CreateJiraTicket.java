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
package de.chamacos.plugins.intellijidea.createjiraticket;

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.ProgressMonitor;
import com.atlassian.jira.rest.client.RestClientException;
import com.atlassian.jira.rest.client.domain.*;
import com.atlassian.jira.rest.client.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.LightColors;
import com.intellij.ui.popup.NotificationPopup;
import com.intellij.ui.popup.list.ListPopupImpl;
import com.intellij.util.IncorrectOperationException;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import de.chamacos.plugins.intellijidea.createjiraticket.configuration.CommentRule;
import de.chamacos.plugins.intellijidea.createjiraticket.configuration.CreateJiraTicketPluginConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.Component;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * (C) Carsten Thiele / ct 10.10.12 19:51
 */
public class CreateJiraTicket extends AnAction implements IntentionAction {

    private static DefaultActionGroup getGroup() {
        return (DefaultActionGroup) ActionManager.getInstance().getAction(IdeActions.GROUP_GENERATE);
    }


    public void actionPerformed(final AnActionEvent event) {
        final Project project = event.getData(PlatformDataKeys.PROJECT);
        final Editor editor = event.getData(PlatformDataKeys.EDITOR);
        PsiFile psiFile = event.getData(LangDataKeys.PSI_FILE);

        doAction(project, editor, psiFile);
    }

    private void doAction(final Project project, final Editor editor, PsiFile psiFile) {
        final CreateJiraTicketPluginConfiguration config = CreateJiraTicketPluginConfiguration.getInstance(project);

        PsiElement commentElement = findPsiCommentNearCursor(psiFile, editor);
        if (commentElement == null) {
            return;
        }

        final String comment = commentElement.getText();

        ArrayList<CommentRule> matchingRules = new ArrayList<CommentRule>();

        for (CommentRule rule : config.getCommentRules()) {
            String pattern = rule.getCommentExpression();

            try {
                if (comment.matches(pattern)) {
                    matchingRules.add(rule);
                }
            } catch (Exception e) {
                e.printStackTrace();
                // TODO Error Handling
            }
        }

        if (matchingRules.size() > 1) {
            // More than one matching rule => Present selection dialog to the user (popup)

            final PsiElement finalCommentElement = commentElement;
            final BaseListPopupStep<CommentRule> step = new BaseListPopupStep<CommentRule>("Select Rule", matchingRules) {
                @Override
                public PopupStep onChosen(CommentRule commentRule, boolean finalChoice) {
                    doCreateTicketAndChangeComment(project, editor, finalCommentElement, comment, commentRule);
                    return null;
                }
            };

            final ListPopup popup = new ListPopupImpl(step) {
                @Override
                protected ListCellRenderer getListElementRenderer() {
                    return new DefaultListCellRenderer() {
                        @Override
                        public Component getListCellRendererComponent(JList list, Object item, int index, boolean b, boolean b1) {
                            Component component = super.getListCellRendererComponent(list, item, index, b, b1);
                            setText(((CommentRule) item).getName());
                            return component;
                        }
                    };
                }
            };

            popup.showInBestPositionFor(editor);

        } else if (matchingRules.size() == 1) {
            // Exact match, immediate execution of Action
            CommentRule rule = matchingRules.get(0);
            doCreateTicketAndChangeComment(project, editor, commentElement, comment, rule);

        } else {
            // No Match => Present Message and give hint to configuration

            showErrorPopup(project, editor, "<html><b>No matching rule.</b><br/>Please configure Rule or change Comment.");
        }
    }

    private PsiElement findPsiCommentNearCursor(PsiFile psiFile, Editor editor) {
        if (editor != null) {
            int offset = editor.getCaretModel().getOffset();
            PsiElement elementAtCursor = psiFile.findElementAt(offset);
            if (elementAtCursor != null) {
                PsiElement commentElement = PsiTreeUtil.getParentOfType(elementAtCursor, PsiComment.class, false);

                // if the cursor is positioned after the last character of the comment (eg. after last Character of //-Comments in java-Source ) the Position is not part of the PsiComment
                // In this case the comment will be searched "to the left" of the current character.
                if (commentElement == null) {
                    if (elementAtCursor.getPrevSibling() != null) {
                        commentElement = PsiTreeUtil.getParentOfType(elementAtCursor.getPrevSibling(), PsiComment.class, false);
                    }
                }
                return commentElement;
            }
        }
        return null;
    }

    private void doCreateTicketAndChangeComment(final Project project, final Editor editor, PsiElement commentElement, String comment, CommentRule rule) {
        CreateJiraTicketPluginConfiguration config = CreateJiraTicketPluginConfiguration.getInstance(project);
        final String ticketSummary = comment.replaceAll(rule.getCommentExpression(), rule.getJiraSummaryTemplate());

        Issue issue = null;
        try {
            issue = createJiraTicket(ticketSummary, config, rule);
        } catch (Exception e) {
            e.printStackTrace(); // TODO remove me
            String errorMessage = "Unknow Error occured";
            if (e instanceof RestClientException) {
                if (e.getCause() instanceof UniformInterfaceException) {
                    UniformInterfaceException uie = (UniformInterfaceException) e.getCause();
                    if (uie.getResponse().getStatus() == 401) {
                        errorMessage = "Wrong Username or Password!";
                    } else if (uie.getResponse().getStatus() == 404) {
                        errorMessage = "Wrong URL for Jira-Server specified!";
                    }
                } else if (e.getCause() instanceof ClientHandlerException) {
                    errorMessage = "Invalid Jira-Hostname specified!";
                }
            } else if (e instanceof IllegalArgumentException) {
                errorMessage = e.getMessage();
            }
            showErrorPopup(project, editor, errorMessage);

            return;
        }
        comment = comment.replaceAll(rule.getCommentExpression(), rule.getCommentTemplate());

        comment = replaceKeywordsInComment(comment, issue);

        final String finalComment = comment;
        final TextRange textRageOfComment = commentElement.getTextRange();
        JButton component = new JButton();
        final String url = config.getJiraUrl() + "/browse/" + issue.getKey();
        component.setText("<html>Ticket Created: <a href=\"" + url + "\" >" + issue.getKey() + "</a></html>");
        component.setBorderPainted(false);
        component.setOpaque(false);
        component.setBackground(LightColors.GREEN);
        component.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(new URI(url));
                    } catch (Exception e) {
                        // IGNORED
                    }
                }
            }
        });

        NotificationPopup popup = new NotificationPopup(editor.getComponent(), component, LightColors.GREEN);
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {

                CommandProcessor.getInstance().executeCommand(project, new Runnable() {
                    @Override
                    public void run() {
                        editor.getDocument().replaceString(textRageOfComment.getStartOffset(), textRageOfComment.getEndOffset(), finalComment);
                    }
                }, "Create Jira Ticket", null);
            }
        });

    }

    private void showErrorPopup(final Project project, Editor editor, String errorMessage) {
        JButton button = new JButton();
        button.setText(errorMessage);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setBackground(LightColors.RED);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ShowSettingsUtil.getInstance().showSettingsDialog(project, "Create Jira Ticket");
            }
        });
        NotificationPopup popup = new NotificationPopup(editor.getComponent(), button, LightColors.RED);
    }

    private String replaceKeywordsInComment(String commentWithPlaceholder, Issue issue) {
        List<String> keywords = new ArrayList<String>();

        boolean found = true;
        int startIndex = 0;
        while (found) {
            int indexOfOpen = commentWithPlaceholder.indexOf("{", startIndex);
            int indexOfClose = commentWithPlaceholder.indexOf("}", indexOfOpen);
            if (indexOfOpen >= 0 && indexOfClose >= 0) {
                String match = commentWithPlaceholder.substring(indexOfOpen + 1, indexOfClose);
                keywords.add(match);
                startIndex = indexOfClose;
            } else {
                found = false;
            }
        }

        for (String keyword : keywords) {
            commentWithPlaceholder = commentWithPlaceholder.replace("{" + keyword + "}", getFieldValueAsString(issue, keyword));
        }
        return commentWithPlaceholder;
    }

    private String getFieldValueAsString(Issue issue, String field) {
        String fieldValue = null;
        try {
            Method m = issue.getClass().getMethod("get" + field);
            Object attributeValue = m.invoke(issue);
            if (attributeValue instanceof String) {
                fieldValue = (String) attributeValue;
            } else if (attributeValue instanceof BasicPriority) {
                fieldValue = ((BasicPriority) attributeValue).getName();
            }
            // TODO Feature: Support more Attributetypes from Jira API

        } catch (Exception e) {
            // ignored => cause field can be custom field and therefore a getter will not be found
        }

        if (fieldValue == null) {
            Field fieldInstance = issue.getFieldByName(field);
            if (fieldInstance != null) {
                fieldValue = fieldInstance.toString();
            }
        }
        return fieldValue;
    }

    private Issue createJiraTicket(String comment, CreateJiraTicketPluginConfiguration config, CommentRule rule) throws URISyntaxException {
        JerseyJiraRestClientFactory factory = new JerseyJiraRestClientFactory();
        URI jiraServerUri = new URI(config.getJiraUrl());
        JiraRestClient restClient = factory.createWithBasicHttpAuthentication(jiraServerUri, config.getUsername(), config.getPassword());
        ProgressMonitor monitor = new NullProgressMonitor();

        String projectKey = config.getProject();
        if (projectKey == null || "".equals(projectKey)) {
            throw new IllegalArgumentException("Project must be set!");
        }
        String issueTypeName = rule.getJiraIssueType();
        if (issueTypeName == null || "".equals(issueTypeName)) {
            throw new IllegalArgumentException("Issuetype must be set!");
        }
        com.atlassian.jira.rest.client.domain.Project project = restClient.getProjectClient().getProject(projectKey, monitor);

        if (project == null) {
            throw new IllegalArgumentException("Project not found on Server!");
        }
        IssueType issueType = null;
        for (IssueType currentIssueType : restClient.getMetadataClient().getIssueTypes(monitor)) {
            if (currentIssueType.getName().equals(issueTypeName)) {
                issueType = currentIssueType;
                break;
            }
        }
        if (issueType == null) {
            throw new IllegalArgumentException("Invalid IssueType selected");
        }

        IssueInputBuilder builder = new IssueInputBuilder(project, issueType);
        builder.setSummary(comment);

        ArrayList<String> labels = new ArrayList<String>();
        for (String label : rule.getJiraLabels().split(",")) {
            labels.add(label.trim());
        }

        // only set Labels-Field if labels are specified. If the labels field is not permitted to be set on IssueCreation setting the field
        // in the Issue will cause an Error while creating the ticket
        if (labels.size() > 0) {
            builder.setFieldValue("labels", labels);
        }


        BasicPriority priority = null;
        for (BasicPriority currentPriority : restClient.getMetadataClient().getPriorities(monitor)) {
            if (currentPriority.getName().equals(rule.getJiraPriority())) {
                priority = currentPriority;
                break;
            }
        }

        if (priority == null) {
            throw new IllegalArgumentException("Invalid Priorty selected");
        }

        builder.setPriority(priority);

        BasicIssue basicIssue = restClient.getIssueClient().createIssue(builder.build(), monitor);

        return restClient.getIssueClient().getIssue(basicIssue.getKey(), monitor);
    }

    @NotNull
    @Override
    public String getText() {
        return "Create Jira Ticket";
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "Create Ticket";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        // Available when cursor is on or "near" PSIComment (same rule as in "Normal" AnAction)
        return findPsiCommentNearCursor(psiFile, editor) != null;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        doAction(project, editor, psiFile);
    }

    @Override
    public boolean startInWriteAction() {
        return false;  // WriteAction is handled by Action Method
    }
}
