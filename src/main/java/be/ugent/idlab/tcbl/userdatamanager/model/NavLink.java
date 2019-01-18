/*
 *  Copyright 2019 imec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package be.ugent.idlab.tcbl.userdatamanager.model;

/**
 * @author Martin Vanbrabant
 */
public class NavLink {
	public enum DisplayCondition {
		// enum names are used in ThymeLeaf, don't change
		ALWAYS,
		ANONYMOUS,
		AUTHENTICATED
	}

	private final DisplayCondition displayCondition;
	private final String text;
	private final String location; // to be interpreted by ThymeLeaf's @{}

	public NavLink(DisplayCondition displayCondition, String text, String location) {
		this.displayCondition = displayCondition;
		this.text = text;
		this.location = location;
	}

	public DisplayCondition getDisplayCondition() {
		return displayCondition;
	}

	public String getText() {
		return text;
	}

	public String getLocation() {
		return location;
	}
}
