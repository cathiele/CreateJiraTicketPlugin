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

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.util.xmlb.XmlSerializerUtil;

import java.io.Serializable;

/**
 * (C) Carsten Thiele / ct 13.10.12 15:54
 */
public class CommentRule implements Serializable, PersistentStateComponent<CommentRule> {

    private String name;
    private String commentExpression;
    private String commentTemplate;
    private String jiraSummaryTemplate;
    private String jiraIssueType;
    private String jiraPriority;
    private String jiraLabels;

    public CommentRule() {

    }

    public String getJiraIssueType() {
        return jiraIssueType;
    }

    public void setJiraIssueType(String jiraIssueType) {
        this.jiraIssueType = jiraIssueType;
    }

    public String getJiraPriority() {
        return jiraPriority;
    }

    public void setJiraPriority(String jiraPriority) {
        this.jiraPriority = jiraPriority;
    }

    public String getJiraLabels() {
        return jiraLabels;
    }

    public void setJiraLabels(String jiraLabels) {
        this.jiraLabels = jiraLabels;
    }

    public String getName() {
        return name;
    }

    public String getCommentExpression() {
        return commentExpression;
    }

    public String getJiraSummaryTemplate() {
        return jiraSummaryTemplate;
    }

    public String getCommentTemplate() {
        return commentTemplate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCommentExpression(String commentExpression) {
        this.commentExpression = commentExpression;
    }

    public void setJiraSummaryTemplate(String jiraSummaryTemplate) {
        this.jiraSummaryTemplate = jiraSummaryTemplate;
    }

    public void setCommentTemplate(String commentTemplate) {
        this.commentTemplate = commentTemplate;
    }

    @Override
    public CommentRule getState() {
        return this;
    }

    @Override
    public void loadState(CommentRule commentRule) {
        XmlSerializerUtil.copyBean(commentRule, this);
    }
}
