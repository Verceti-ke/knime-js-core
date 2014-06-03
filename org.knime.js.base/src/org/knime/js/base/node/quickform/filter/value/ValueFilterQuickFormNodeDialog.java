/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------------
 */
package org.knime.js.base.node.quickform.filter.value;

import java.awt.GridBagConstraints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.ColumnSelectionPanel;
import org.knime.core.node.util.filter.StringFilterPanel;
import org.knime.js.base.dialog.selection.multiple.MultipleSelectionsComponentFactory;
import org.knime.js.base.node.quickform.QuickFormNodeDialog;

/**
 * 
 * @author Christian Albrecht, KNIME.com AG, Zurich, Switzerland, University of
 *         Konstanz
 */
@SuppressWarnings("unchecked")
public class ValueFilterQuickFormNodeDialog extends QuickFormNodeDialog {

    private final JCheckBox m_lockColumn;

    private final ColumnSelectionPanel m_defaultColumnField;
    
    private final StringFilterPanel m_defaultField;

    private final ColumnSelectionPanel m_columnField;

    private final StringFilterPanel m_valueField;
    
    private String[] m_possibleValues;
    
    private DataTableSpec m_spec;
    
    private final JComboBox<String> m_type;

    /** Constructors, inits fields calls layout routines. */
    ValueFilterQuickFormNodeDialog() {
        m_type = new JComboBox<String>(MultipleSelectionsComponentFactory.listMultipleSelectionsComponents());
        m_lockColumn = new JCheckBox();
        m_defaultColumnField = new ColumnSelectionPanel((Border) null, new Class[]{DataValue.class});
        m_columnField = new ColumnSelectionPanel((Border) null, new Class[]{DataValue.class});
        m_defaultField = new StringFilterPanel(true);
        m_valueField = new StringFilterPanel(true);
        m_defaultColumnField.addItemListener(new ItemListener() {
            /** {@inheritDoc} */
            @Override
            public void itemStateChanged(final ItemEvent ie) {
                Object o = ie.getItem();
                if (o != null) {
                    final String column = m_defaultColumnField.getSelectedColumn();
                    if (column != null) {
                        updateValues(column, m_defaultField);
                    }
                }
            }
        });
        m_columnField.addItemListener(new ItemListener() {
            /** {@inheritDoc} */
            @Override
            public void itemStateChanged(final ItemEvent ie) {
                Object o = ie.getItem();
                if (o != null) {
                    final String column = m_columnField.getSelectedColumn();
                    if (column != null) {
                        updateValues(column, m_valueField);
                    }
                }
                if (m_lockColumn.isSelected()) {
                    m_defaultColumnField.setSelectedColumn(m_columnField.getSelectedColumn());
                }
            }
        });
        m_lockColumn.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                m_defaultColumnField.setEnabled(!m_lockColumn.isSelected());
                if (m_lockColumn.isSelected()) {
                    m_defaultColumnField.setSelectedColumn(m_columnField.getSelectedColumn());
                }
            }
        });
        createAndAddTab();
    }

    private void updateValues(final String column, final StringFilterPanel panel) {
        DataColumnSpec dcs = m_spec.getColumnSpec(column);
        if (dcs == null) {
            m_possibleValues = new String[0];
        } else {
            final Set<DataCell> vals = dcs.getDomain().getValues();
            m_possibleValues = new String[vals.size()];
            int i = 0;
            for (final DataCell cell : vals) {
                m_possibleValues[i++] = cell.toString();
            }
        }
        List<String> excludes = Arrays.asList(m_possibleValues);
        panel.update(new ArrayList<String>(0), excludes,
                m_possibleValues);
        panel.update(new ArrayList<String>(0), excludes,
                m_possibleValues);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void fillPanel(final JPanel panelWithGBLayout, final GridBagConstraints gbc) {
        addPairToPanel("Selection type: ", m_type, panelWithGBLayout, gbc);
        addPairToPanel("Lock column: ", m_lockColumn, panelWithGBLayout, gbc);
        addPairToPanel("Default column selection: ", m_defaultColumnField, panelWithGBLayout, gbc);
        addPairToPanel("Default Values: ", m_defaultField, panelWithGBLayout, gbc);
        addPairToPanel("Column selection: ", m_columnField, panelWithGBLayout, gbc);
        addPairToPanel("Variable Values: ", m_valueField, panelWithGBLayout, gbc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs)
            throws NotConfigurableException {
        final DataTableSpec spec = (DataTableSpec) specs[0];
        final List<DataColumnSpec> filteredSpecs = new ArrayList<DataColumnSpec>();
        for (DataColumnSpec cspec : spec) {
            if (cspec.getDomain().hasValues()) {
                filteredSpecs.add(cspec);
            }
        }
        if (filteredSpecs.size() == 0) {
            throw new NotConfigurableException("Data does not contain any column with domain values.");
        }
        m_spec = new DataTableSpec(filteredSpecs.toArray(new DataColumnSpec[0]));
        m_defaultColumnField.update(m_spec, null);
        m_columnField.update(m_spec, null);
        ValueFilterQuickFormRepresentation representation = new ValueFilterQuickFormRepresentation();
        representation.loadFromNodeSettingsInDialog(settings);
        loadSettingsFrom(representation);
        String selectedDefaultColumn = representation.getDefaultColumn();
        if (selectedDefaultColumn.isEmpty()) {
            List<DataColumnSpec> cspecs = m_columnField.getAvailableColumns();
            if (cspecs.size() > 0) {
                selectedDefaultColumn = cspecs.get(0).getName();
            }
        }
        m_defaultColumnField.setSelectedColumn(selectedDefaultColumn);
        List<String> defaultIncludes = Arrays.asList(representation.getDefaultValues());
        List<String> defaultExcludes =
                new ArrayList<String>(Math.max(0, m_possibleValues.length
                        - defaultIncludes.size()));
        for (String string : m_possibleValues) {
            if (!defaultIncludes.contains(string)) {
                defaultExcludes.add(string);
            }
        }
        m_defaultField.update(defaultIncludes, defaultExcludes, m_possibleValues);
        ValueFilterQuickFormValue value = new ValueFilterQuickFormValue();
        String selectedColumn = value.getColumn();
        if (selectedColumn.isEmpty()) {
            List<DataColumnSpec> cspecs = m_columnField.getAvailableColumns();
            if (cspecs.size() > 0) {
                selectedColumn = cspecs.get(0).getName();
            }
        }
        m_columnField.setSelectedColumn(selectedColumn);
        value.loadFromNodeSettingsInDialog(settings);
        List<String> valueIncludes = Arrays.asList(value.getValues());
        List<String> valueExcludes = new ArrayList<String>(Math.max(0, m_possibleValues.length - valueIncludes.size()));
        for (String string : m_possibleValues) {
            if (!valueIncludes.contains(string)) {
                valueExcludes.add(string);
            }
        }
        m_valueField.update(valueIncludes, valueExcludes, m_possibleValues);
        m_lockColumn.setSelected(representation.getLockColumn());
        m_type.setSelectedItem(representation.getType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
        ValueFilterQuickFormRepresentation representation = new ValueFilterQuickFormRepresentation();
        saveSettingsTo(representation);
        representation.setLockColumn(m_lockColumn.isSelected());
        representation.setDefaultColumn(m_defaultColumnField.getSelectedColumn());
        Set<String> defaultIncludes = m_defaultField.getIncludeList();
        representation.setDefaultValues(defaultIncludes.toArray(new String[defaultIncludes.size()]));
        representation.setFromSpec(m_spec);
        representation.setType((String)m_type.getSelectedItem());
        representation.saveToNodeSettings(settings);
        ValueFilterQuickFormValue value = new ValueFilterQuickFormValue();
        Set<String> valueIncludes = m_valueField.getIncludeList();
        value.setValues(valueIncludes.toArray(new String[valueIncludes.size()]));
        value.setColumn(m_columnField.getSelectedColumn());
        value.saveToNodeSettings(settings);
    }

}