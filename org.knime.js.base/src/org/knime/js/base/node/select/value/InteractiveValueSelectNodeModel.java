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
 * ---------------------------------------------------------------------
 *
 * Created on 30.04.2013 by Christian Albrecht, KNIME.com AG, Zurich, Switzerland
 */
package org.knime.js.base.node.select.value;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.DataContainer;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.port.PortType;
import org.knime.core.node.web.ValidationError;
import org.knime.core.node.wizard.WizardNode;
import org.knime.js.core.JavaScriptViewCreator;

/**
 *
 * @author Christian Albrecht, KNIME.com AG, Zurich, Switzerland
 */
public class InteractiveValueSelectNodeModel extends NodeModel implements WizardNode<InteractiveValueSelectViewContent, InteractiveValueSelectViewContent> {

    static SettingsModelStringArray createSelectedValuesArray() {
        return new SettingsModelStringArray("selectedValuesKey", new String[] {});
    }

    static SettingsModelStringArray createPossibleValuesArray() {
        return new SettingsModelStringArray("possibleValuesKey", new String[] {});
    }

    static SettingsModelString createColumnSelection() {
        return new SettingsModelString("column", "");
    }

    private SettingsModelStringArray selectedValues = createSelectedValuesArray();
    private SettingsModelStringArray possibleValues = createPossibleValuesArray();
    private SettingsModelString column = createColumnSelection();

    private DataColumnSpec colSpec = new DataColumnSpecCreator("Selected Values", StringCell.TYPE).createSpec();
    private String m_viewPath;

    /**
     *
     */
    public InteractiveValueSelectNodeModel() {
        super(new PortType[]{BufferedDataTable.TYPE},
              new PortType[]{BufferedDataTable.TYPE});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        DataTableSpec spec = new DataTableSpec(colSpec);
        return new DataTableSpec[]{spec};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InteractiveValueSelectViewContent getViewRepresentation() {
        return new InteractiveValueSelectViewContent(possibleValues.getStringArrayValue(), selectedValues.getStringArrayValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
                throws Exception {
        DataTableSpec spec = inData[0].getSpec();
        Set<DataCell> possibleValueCells = spec.getColumnSpec(spec.findColumnIndex(column.getStringValue())).getDomain().getValues();
        String[] pValues = new String[possibleValueCells.size()];
        int i = 0;
        for (DataCell cell : possibleValueCells) {
            pValues[i++] = cell.toString();
        }
        possibleValues.setStringArrayValue(pValues);
        return new BufferedDataTable[] {createResult(new String[] {}, exec)};
    }

    private BufferedDataTable createResult(final String[] values, final ExecutionContext ec) throws CanceledExecutionException {
        DataContainer dc = new DataContainer(new DataTableSpec(colSpec));
        int i = 0;
        for (String value : values) {
            dc.addRowToTable(new DefaultRow("Selected Value " + (i++), value));
        }
        dc.close();
        return ec.createBufferedDataTable(dc.getTable(), ec);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        selectedValues.saveSettingsTo(settings);
        possibleValues.saveSettingsTo(settings);
        column.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        selectedValues.validateSettings(settings);
        possibleValues.validateSettings(settings);
        column.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        selectedValues.loadSettingsFrom(settings);
        possibleValues.loadSettingsFrom(settings);
        column.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        selectedValues = createSelectedValuesArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InteractiveValueSelectViewContent createEmptyViewRepresentation() {
        try {
            return InteractiveValueSelectViewContent.class.newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadViewValue(final InteractiveValueSelectViewContent viewContent, final boolean useAsDefault) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValidationError validateViewValue(final InteractiveValueSelectViewContent viewContent) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getJavascriptObjectID() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InteractiveValueSelectViewContent getViewValue() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InteractiveValueSelectViewContent createEmptyViewValue() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isHideInWizard() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveCurrentValue(final NodeSettingsWO content) {
        // TODO Auto-generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getViewHTMLPath() {
        if (m_viewPath == null || m_viewPath.isEmpty()) {
            // view is not created
            m_viewPath = createViewPath();
        } else {
            // check if file still exists, create otherwise
            File viewFile = new File(m_viewPath);
            if (!viewFile.exists()) {
                m_viewPath = createViewPath();
            }
        }
        return m_viewPath;
    }

    private String createViewPath() {
        JavaScriptViewCreator<InteractiveValueSelectViewContent, InteractiveValueSelectViewContent> viewCreator =
            new JavaScriptViewCreator<InteractiveValueSelectViewContent, InteractiveValueSelectViewContent>(
                getJavascriptObjectID());
        try {
            return viewCreator.createWebResources("View", getViewRepresentation(), getViewValue());
        } catch (IOException e) {
            return null;
        }
    }

}
