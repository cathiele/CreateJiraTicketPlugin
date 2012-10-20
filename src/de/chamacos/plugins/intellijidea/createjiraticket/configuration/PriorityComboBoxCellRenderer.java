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
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;

/**
 * (C) Carsten Thiele / ct 14.10.12 12:28
 */
public class PriorityComboBoxCellRenderer extends BasicComboBoxRenderer {
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean selected, boolean focused) {
        JLabel component = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focused);
        if (value != null) {
            Priority priority = (Priority) value;
            component.setText(priority.getName());
        }
        return component;
    }
}
