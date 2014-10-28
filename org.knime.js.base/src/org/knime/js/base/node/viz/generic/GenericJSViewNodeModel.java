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
 *  propagated with or for interoperation with KNIME. The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------------
 *
 * History
 *   30.04.2014 (Christian Albrecht, KNIME.com AG, Zurich, Switzerland): created
 */
package org.knime.js.base.node.viz.generic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.knime.base.util.flowvariable.FlowVariableProvider;
import org.knime.base.util.flowvariable.FlowVariableResolver;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortType;
import org.knime.core.node.web.ValidationError;
import org.knime.core.node.wizard.WizardNode;
import org.knime.js.core.JSONDataTable;
import org.knime.js.core.JavaScriptViewCreator;

/**
 *
 * @author Christian Albrecht, KNIME.com AG, Zurich, Switzerland, University of Konstanz
 */
final class GenericJSViewNodeModel extends NodeModel implements
    WizardNode<GenericJSViewRepresentation, GenericJSViewValue>, FlowVariableProvider {

    private final Object m_lock = new Object();
    private final GenericJSViewConfig m_config;
    private GenericJSViewRepresentation m_representation;
    private String m_viewPath;

    /**
     */
    GenericJSViewNodeModel() {
        super(new PortType[]{BufferedDataTable.TYPE_OPTIONAL}, null);
        m_config = new GenericJSViewConfig();
        m_representation = createEmptyViewRepresentation();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        if (StringUtils.isEmpty(m_config.getJsCode())) {
            throw new InvalidSettingsException("No script defined");
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
        throws Exception {

        synchronized (m_lock) {
            //create JSON table if data available
            if (inData[0] != null) {
                JSONDataTable table = new JSONDataTable(inData[0], 1, inData[0].getRowCount(), exec);
                m_representation.setTable(table);
            }

            m_representation.setJsCode(parseTextAndReplaceVariables());
            m_representation.setCssCode(m_config.getCssCode());
            setPathsFromLibNames(m_config.getDependencies());
        }
        return null;
    }

    private String parseTextAndReplaceVariables() throws InvalidSettingsException {
        String flowVarCorrectedText = null;
        if (m_config.getJsCode() != null) {
            try {
                flowVarCorrectedText = FlowVariableResolver.parse(m_config.getJsCode(), this);
            } catch (NoSuchElementException nse) {
                throw new InvalidSettingsException(nse.getMessage(), nse);
            }
        }
        return flowVarCorrectedText;
    }

    private static final String ID_WEB_RES = "org.knime.js.core.webResources";

    private static final String ATTR_RES_BUNDLE_ID = "webResourceBundleID";

    private static final String ID_IMPORT_RES = "importResource";

    private static final String ATTR_PATH = "relativePath";

    private static final String ATTR_TYPE = "type";

    private void setPathsFromLibNames(final String[] libNames) {
        ArrayList<String> jsPaths = new ArrayList<String>();
        ArrayList<String> cssPaths = new ArrayList<String>();
        for (String lib : libNames) {
            IConfigurationElement confElement = getConfigurationFromWebResID(lib);
            if (confElement != null) {
                for (IConfigurationElement resElement : confElement.getChildren(ID_IMPORT_RES)) {
                    String path = resElement.getAttribute(ATTR_PATH);
                    String type = resElement.getAttribute(ATTR_TYPE);
                    if (path != null && type != null) {
                        if (type.equalsIgnoreCase("javascript")) {
                            jsPaths.add(path);
                        } else if (type.equalsIgnoreCase("css")) {
                            cssPaths.add(path);
                        }
                    } else {
                        setWarningMessage("Required library " + lib + " is not correctly configured");
                    }
                }
            } else {
                setWarningMessage("Required library is not registered: " + lib);
            }
        }
        m_representation.setJsDependencies(jsPaths.toArray(new String[0]));
        m_representation.setCssDependencies(cssPaths.toArray(new String[0]));
    }

    private IConfigurationElement getConfigurationFromWebResID(final String id) {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IConfigurationElement[] configurationElements = registry.getConfigurationElementsFor(ID_WEB_RES);
        for (IConfigurationElement element : configurationElements) {
            if (id.equals(element.getAttribute(ATTR_RES_BUNDLE_ID))) {
                return element;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValidationError validateViewValue(final GenericJSViewValue viewContent) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadViewValue(final GenericJSViewValue viewContent, final boolean useAsDefault) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GenericJSViewRepresentation getViewRepresentation() {
        synchronized (m_lock) {
            return m_representation;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GenericJSViewValue getViewValue() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GenericJSViewRepresentation createEmptyViewRepresentation() {
        return new GenericJSViewRepresentation();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GenericJSViewValue createEmptyViewValue() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getJavascriptObjectID() {
        return "knime_generic_view";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec) throws IOException,
        CanceledExecutionException {
        File f = new File(nodeInternDir, "representation.xml");
        NodeSettingsRO settings = NodeSettings.loadFromXML(new FileInputStream(f));
        m_representation = createEmptyViewRepresentation();
        try {
            m_representation.loadFromNodeSettings(settings);
        } catch (InvalidSettingsException e) { /* do nothing */
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec) throws IOException,
        CanceledExecutionException {
        NodeSettings settings = new NodeSettings("genericViewRepresentation");
        if (m_representation != null) {
            m_representation.saveToNodeSettings(settings);
        }
        File f = new File(nodeInternDir, "representation.xml");
        settings.saveToXML(new FileOutputStream(f));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_config.saveSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_config.loadSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        synchronized (m_lock) {
            m_representation = createEmptyViewRepresentation();
        }
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
        JavaScriptViewCreator<GenericJSViewNodeModel, GenericJSViewRepresentation, GenericJSViewValue> viewCreator =
            new JavaScriptViewCreator<GenericJSViewNodeModel, GenericJSViewRepresentation, GenericJSViewValue>(
                getJavascriptObjectID());
        try {
            return viewCreator.createWebResources("View", m_representation, null);
        } catch (IOException e) {
            return null;
        }
    }

}
