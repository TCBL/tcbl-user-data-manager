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
 * @author Gerald Haesendonck
 */
public class Status {
	public enum Value {
		OK,
		WARNING,
		ERROR
	}

	private final Value value;
	private final String text;

	public Status(Value value, String text) {
		this.value = value;
		this.text = text;
	}

	public Value getValue() {
		return value;
	}

	public String getText() {
		return text;
	}
}
