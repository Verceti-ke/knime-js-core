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
 * 
 * History
 *   Oct 14, 2013 (Patrick Winter, KNIME.com AG, Zurich, Switzerland): created
 */
org_knime_js_base_node_quickform_filter_value = function() {
	var valueFilter = {
			version: "1.0.0"
	};
	valueFilter.name = "Value filter";
	var viewValue;
	var viewRepresentation;
	var colselection;
	var selector;

	valueFilter.init = function(representation, value) {
		var body = $('body');
		var qfdiv = $('<div class="quickformcontainer">');
		body.append(qfdiv);
		qfdiv.attr('title', representation.description);
		qfdiv.append("<b>" + representation.label + "</b><br>");
		viewValue = value;
		viewRepresentation = representation;
		if (representation.possibleValues == null) {
			qfdiv.append("Error: No data available");
		} else {
			var columnSelection = value.column;
			if (!representation.lockColumn) {
				colselection = $('<select>');
				qfdiv.append(colselection);
				qfdiv.append($('<br>'));
				for ( var key in representation.possibleValues) {
					var option = $('<option>' + key + '</option>');
					option.appendTo(colselection);
					if (key == columnSelection) {
						option.prop('selected', true);
					}
					option.blur(callUpdate);
				}
				colselection.change(selectionChanged);
			}
			if (viewRepresentation.type == 'Check boxes (vertical)') {
				selector = new checkBoxesMultipleSelections(true);
			} else if (viewRepresentation.type == 'Check boxes (horizontal)') {
				selector = new checkBoxesMultipleSelections(false);
			} else if (viewRepresentation.type == 'List') {
				selector = new listMultipleSelections();
			} else {
				selector = new twinlistMultipleSelections();
			}
			qfdiv.append(selector.getComponent());
			selector.setChoices(viewRepresentation.possibleValues[columnSelection]);
			var selections;
			if (typeof value.values != undefined && value.values != null) {
				selections = value.values;
			} else {
				selections = representation.defaultValues;
			}
			selector.setSelections(selections);
			selector.addValueChangedListener(callUpdate);
		}
		resizeParent();
		callUpdate();
	};

	valueFilter.value = function() {
		viewValue.values = selector.getSelections();
		if (!viewRepresentation.lockColumn) {
			viewValue.column = colselection.find(':selected').text();
		}
		return viewValue;
	};

	function selectionChanged() {
		var col = colselection.find(':selected').text();
		selector.setChoices(viewRepresentation.possibleValues[col]);
		selector.setSelection([]);
	}
	
	return valueFilter;
	
}();
