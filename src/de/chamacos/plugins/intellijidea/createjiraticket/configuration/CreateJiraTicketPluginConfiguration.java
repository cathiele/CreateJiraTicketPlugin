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

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * (C) Carsten Thiele / ct 13.10.12 15:53
 */


@State(name = "CreateJiraTicketConfiguration", storages = {
        @Storage(id = "default", file = "$PROJECT_FILE$"),
        @Storage(id = "dir", file = "$PROJECT_CONFIG_DIR$" + "/createJiraTicket.xml", scheme = StorageScheme.DIRECTORY_BASED)
})
public class CreateJiraTicketPluginConfiguration extends AbstractProjectComponent implements Serializable, PersistentStateComponent<CreateJiraTicketPluginConfiguration> {
    private String jiraUrl;
    private String username;
    private String password;
    private String project;
    private ArrayList<CommentRule> commentRules;

    public static CreateJiraTicketPluginConfiguration getInstance(Project project) {
        return project.getComponent(CreateJiraTicketPluginConfiguration.class);
    }

    public CreateJiraTicketPluginConfiguration() {
        super(null);
        this.commentRules = new ArrayList<CommentRule>();
    }


    public boolean jiraServerSettingsComplete() {
        return !"".equals(jiraUrl) && !"".equals(username) && !"".equals(password);
    }

    @Override
    @NotNull
    public String getComponentName() {
        return "CreateJiraTicketConfiguration";
    }

    protected CreateJiraTicketPluginConfiguration(Project project) {
        super(project);
        this.commentRules = new ArrayList<CommentRule>();
    }

    public String getJiraUrl() {
        return jiraUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getProject() {
        return project;
    }

    public ArrayList<CommentRule> getCommentRules() {
        return commentRules;
    }

    public void setJiraUrl(String jiraUrl) {
        this.jiraUrl = jiraUrl;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public void setCommentRules(ArrayList<CommentRule> commentRules) {
        this.commentRules = commentRules;
    }

    @Override
    public CreateJiraTicketPluginConfiguration getState() {
        return this;
    }

    @Override
    public void loadState(CreateJiraTicketPluginConfiguration createJiraTicketPluginConfiguration) {
        XmlSerializerUtil.copyBean(createJiraTicketPluginConfiguration, this);
    }
}
