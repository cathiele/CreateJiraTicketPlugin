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

import com.atlassian.jira.rest.client.domain.BasicProject;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.util.ArrayList;

/**
 * (C) Carsten Thiele / ct 13.10.12 22:06
 */
public class ProjectComboBoxModel implements ComboBoxModel {
    private ArrayList<BasicProject> projects;
    private BasicProject selectedItem;

    public ProjectComboBoxModel(ArrayList<BasicProject> projects) {
        this.projects = projects;
    }

    @Override
    public void setSelectedItem(Object o) {
        selectedItem = (BasicProject) o;
    }

    @Override
    public Object getSelectedItem() {
        return selectedItem;
    }

    @Override
    public int getSize() {
        return projects.size();
    }

    @Override
    public Object getElementAt(int i) {
        return projects.get(i);
    }

    @Override
    public void addListDataListener(ListDataListener listDataListener) {
        // No datalistener needed
    }

    @Override
    public void removeListDataListener(ListDataListener listDataListener) {
        // No datalistener needed
    }
}
