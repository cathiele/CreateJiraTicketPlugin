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
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;

/**
 * (C) Carsten Thiele / ct 13.10.12 22:24
 */
public class JiraProjectComboBoxCellRenderer extends BasicComboBoxRenderer {
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean selected, boolean focused) {
        JLabel component = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focused);
        if (value != null) {
            BasicProject project = (BasicProject) value;
            component.setText(project.getName());
        }
        return component;
    }
}
