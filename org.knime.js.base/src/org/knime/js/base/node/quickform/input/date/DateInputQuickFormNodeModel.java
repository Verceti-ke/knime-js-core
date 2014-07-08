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
package org.knime.js.base.node.quickform.input.date;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.web.ValidationError;
import org.knime.js.base.node.quickform.QuickFormFlowVariableNodeModel;

/**
 * The model for the date input quick form node.
 *
 * @author Patrick Winter, KNIME.com AG, Zurich, Switzerland
 */
public class DateInputQuickFormNodeModel
        extends QuickFormFlowVariableNodeModel
        <DateInputQuickFormRepresentation,
        DateInputQuickFormValue,
        DateInputQuickFormConfig> {

    /**
     * Format string for the date to string and string to date operations.
     */
    static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * Format string for the date to string and string to date operations.
     */
    static final String DATE_TIME_FORMAT = "yyyy-MM-dd;HH:mm";

    /**
     * {@inheritDoc}
     */
    @Override
    public DateInputQuickFormValue createEmptyViewValue() {
        return new DateInputQuickFormValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getJavascriptObjectID() {
        return "org_knime_js_base_node_quickform_input_date";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createAndPushFlowVariable() throws InvalidSettingsException {
        Date value = getRelevantValue().getDate();
        Date min = getConfig().getMin();
        Date max = getConfig().getMax();
        if (!getConfig().getWithTime()) {
            // Set time to midnight for value, min and max
            value = new Date(value.getYear(), value.getMonth(), value.getDate());
            min = new Date(min.getYear(), min.getMonth(), min.getDate());
            max = new Date(max.getYear(), max.getMonth(), max.getDate());
        }
        if (getConfig().getUseMin() && value.before(min)) {
            throw new InvalidSettingsException("The set date " + value
                    + " is before the earliest allowed date " + min);
        }
        if (getConfig().getUseMax() && value.after(max)) {
            throw new InvalidSettingsException("The set date " + value
                    + " is after the latest allowed date " + max);
        }
        SimpleDateFormat sdf =
                new SimpleDateFormat(getConfig().getWithTime() ? DATE_TIME_FORMAT : DATE_FORMAT);
        pushFlowVariableString(getConfig().getFlowVariableName(),
                sdf.format(value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void copyValueToConfig() {
        getConfig().getDefaultValue().setDate(getViewValue().getDate());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DateInputQuickFormConfig createEmptyConfig() {
        return new DateInputQuickFormConfig();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DateInputQuickFormRepresentation getRepresentation() {
        return new DateInputQuickFormRepresentation(getRelevantValue(), getConfig());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValidationError validateViewValue(final DateInputQuickFormValue viewContent) {
        Date value = getRelevantValue().getDate();
        Date min = getConfig().getMin();
        Date max = getConfig().getMax();
        if (!getConfig().getWithTime()) {
            // Set time to midnight for value, min and max
            value = new Date(value.getYear(), value.getMonth(), value.getDate());
            min = new Date(min.getYear(), min.getMonth(), min.getDate());
            max = new Date(max.getYear(), max.getMonth(), max.getDate());
        }
        if (getConfig().getUseMin() && value.before(min)) {
            return new ValidationError("The set date " + value
                    + " is before the earliest allowed date " + min);
        }
        if (getConfig().getUseMax() && value.after(max)) {
            return new ValidationError("The set date " + value
                    + " is after the latest allowed date " + max);
        }
        return super.validateViewValue(viewContent);
    }

}
