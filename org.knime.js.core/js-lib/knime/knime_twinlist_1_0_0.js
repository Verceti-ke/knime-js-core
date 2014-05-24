/*
 * ------------------------------------------------------------------------
 *
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
 *   Oct 14, 2013 (Patrick Winter, KNIME.com AG, Zurich, Switzerland): created
 */
function twinlist() {

	// Define member variables

	var element;
	var values;
	var excludes;
	var includes;

	// Define inner functions

	setAvailableValues = function(newValues) {
		values = newValues;
		refreshLists();
		refreshSize();
	};

	refreshSize = function() {
		excludes.attr('size', values.length);
		includes.attr('size', values.length);
		// All buttons get the same width
		var widthMax = Math.max(add.width(), addAll.width(), remove.width(),
				removeAll.width());
		add.width(widthMax);
		addAll.width(widthMax);
		remove.width(widthMax);
		removeAll.width(widthMax);
		// All columns get the same height
		var heightMax = Math.max(excludesSpan.height(), includesSpan.height(), span.height());
		excludes.height(excludes.height() + heightMax - excludesSpan.height());
		includes.height(includes.height() + heightMax - includesSpan.height());
		buttonfiller.height(heightMax-excludesHeader.height(true));
		// Lists get the same width
		var listWidthMax = Math.max(excludes.width(), includes.width(), 150);
		excludes.width(listWidthMax);
		includes.width(listWidthMax);
		// Outer element gets overall width and height
		element.width(excludesSpan.outerWidth(true) + span.outerWidth(true)
				+ includesSpan.outerWidth(true));
		element.height(excludesSpan.outerHeight(true));
	};

	getIncludes = function() {
		return getContainedValues(includes);
	};

	getExcludes = function() {
		return getContainedValues(excludes);
	};

	getElement = function() {
		return element;
	};

	setIncludes = function(includeValues) {
		setOptions(true, includeValues);
	};

	setExcludes = function(excludeValues) {
		setOptions(false, excludeValues);
	};

	setOptions = function(isIncludes, vals) {
		excludes.empty();
		includes.empty();
		for ( var i in values) {
			var value = values[i];
			var option = $('<option>' + value + '</option>');
			if (isIncludes) {
				if (contains(vals, value)) {
					option.appendTo(includes);
				} else {
					option.appendTo(excludes);
				}
			} else {
				if (contains(vals, value)) {
					option.appendTo(excludes);
				} else {
					option.appendTo(includes);
				}
			}
		}
		refreshListeners();
	};

	getContainedValues = function(selectElement) {
		var containedValues = [];
		selectElement.find('option').each(function() {
			containedValues.push($(this).text());
		});
		return containedValues;
	};

	refreshLists = function() {
		var inc = getIncludes();
		var exc = getExcludes();
		excludes.empty();
		includes.empty();
		for ( var i in values) {
			var value = values[i];
			var option = $('<option>' + value + '</option>');
			if (contains(inc, value)) {
				option.appendTo(includes);
			} else {
				option.appendTo(excludes);
			}
		}
		refreshListeners();
	};

	contains = function(array, value) {
		return $.inArray(value, array) >= 0;
	};

	refreshListeners = function() {
		excludes.find('option').dblclick(execAdd);
		includes.find('option').dblclick(execRemove);
	};

	execAdd = function() {
		excludes.find(':selected').each(function() {
			includes.append($(this));
		});
		refreshLists();
	};

	execAddAll = function() {
		excludes.find('option').each(function() {
			includes.append($(this));
		});
		refreshLists();
	};

	execRemove = function() {
		includes.find(':selected').each(function() {
			excludes.append($(this));
		});
		refreshLists();
	};

	execRemoveAll = function() {
		includes.find('option').each(function() {
			excludes.append($(this));
		});
		refreshLists();
	};

	// Init DOM

	element = $('<div>')
	excludes = $('<select>');
	excludes.css('float', 'left');
	excludes.prop('multiple', true);
	excludes.css('border-color', 'red');
	includes = $('<select>');
	includes.css('float', 'left');
	includes.prop('multiple', true);
	includes.css('border-color', 'green');
	var excludesSpan = $('<span>');
	excludesSpan.css('float', 'left');
	var includesSpan = $('<span>');
	includesSpan.css('float', 'left');
	var span = $('<span>');
	span.css('float', 'left');
	var buttonfiller = $('<div>');
	var add = $('<button type="button">&gt;</button>');
	add.click(execAdd);
	add.attr('title', 'Add selected to includes');
	var addAll = $('<button type="button">&gt;&gt;</button>');
	addAll.click(execAddAll);
	addAll.attr('title', 'Add all to includes');
	var remove = $('<button type="button">&lt;</button>');
	remove.click(execRemove);
	remove.attr('title', 'Remove selected from includes');
	var removeAll = $('<button type="button">&lt;&lt;</button>');
	removeAll.click(execRemoveAll);
	removeAll.attr('title', 'Remove all from includes');
	element.append(excludesSpan);
	element.append(span);
	element.append(includesSpan);
	span.append(buttonfiller);
	span.append('<br>');
	span.append(add);
	span.append('<br>');
	span.append(addAll);
	span.append('<br>');
	span.append(remove);
	span.append('<br>');
	span.append(removeAll);
	var excludesHeader = $('<span>Excludes</span>');
	excludesSpan.append(excludesHeader);
	excludesSpan.append($('<br>'));
	excludesSpan.append(excludes);
	var includesHeader = $('<span>Includes</span>');
	includesSpan.append(includesHeader);
	includesSpan.append($('<br>'));
	includesSpan.append(includes);

	// Define which functions are callable from the outside

	this.setAvailableValues = setAvailableValues;
	this.setIncludes = setIncludes;
	this.setExcludes = setExcludes;
	this.getIncludes = getIncludes;
	this.getExcludes = getExcludes;
	this.getElement = getElement;
	this.refreshSize = refreshSize;

}
