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

import com.atlassian.jira.rest.client.domain.Priority;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.util.ArrayList;

/**
 * (C) Carsten Thiele / ct 14.10.12 12:22
 */
public class PriorityComboBoxModel implements ComboBoxModel {
    private ArrayList<Priority> priorities;
    private Priority selectedItem = null;

    public PriorityComboBoxModel(ArrayList<Priority> priorities) {
        this.priorities = priorities;
    }

    @Override
    public void setSelectedItem(Object item) {
        this.selectedItem = (Priority) item;
    }

    @Override
    public Object getSelectedItem() {
        return selectedItem;
    }

    @Override
    public int getSize() {
        return priorities.size();
    }

    @Override
    public Object getElementAt(int i) {
        return priorities.get(i);
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
