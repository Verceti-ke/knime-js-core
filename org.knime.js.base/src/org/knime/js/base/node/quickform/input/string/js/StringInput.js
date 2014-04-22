/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by 
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
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
 *   Oct 14, 2013 (Patrick Winter, KNIME.com AG, Zurich, Switzerland): created
 */
org_knime_js_base_node_quickform_input_string = function() {
	var stringInput = {
			version: "1.0.0"
	};
	stringInput.name = "String input";
	var viewValue;
	var input;
	var errorMessage;

	stringInput.init = function(representation, value) {
		var body = $('body');
		viewValue = value;
		input = $('<input>');
		input.attr("type", "text");
		input.attr("pattern", representation.regex);
		input.val(representation.defaultvalue);
		body.append(input);
		body.append($('<br>'));
		errorMessage = $('<span>'+representation.errormessage+'</span>');
		errorMessage.css('display', 'none');
		errorMessage.css('color', 'red');
		errorMessage.css('font-style', 'italic');
		errorMessage.css('font-size', '75%');
		body.append(errorMessage);
	};

	stringInput.validate = function() {
		var regex = input.attr("pattern");
		if (regex != null && regex.length > 0) {
			var valid = matchExact(regex, input.val());
			if (!valid) {
				errorMessage.css('display', 'inline');
			} else {
				errorMessage.css('display', 'none');
			}
			return valid;
		} else {
			return true;
		}
	};

	stringInput.value = function() {
		viewValue.string = input.val();
		return viewValue;
	};
	
	function matchExact(r, str) {
		var match = str.match(r);
		return match != null && str == match[0];
	}
	
	return stringInput;
	
}();
