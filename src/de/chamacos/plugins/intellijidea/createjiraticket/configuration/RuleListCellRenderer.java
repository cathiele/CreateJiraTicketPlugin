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

import javax.swing.*;
import java.awt.*;

/**
 * (C) Carsten Thiele / ct 14.10.12 10:09
 */
public class RuleListCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(javax.swing.JList listComponent, java.lang.Object value, int index, boolean b, boolean b1) {
        Component component = super.getListCellRendererComponent(listComponent, value, index, b, b1);
        if (value != null) {
            CommentRule commentRule = (CommentRule) value;
            setText(commentRule.getName());
        }
        return component;
    }
}
