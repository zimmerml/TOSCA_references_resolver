package tosca.Abstract;

/*-
 * #%L
 * TOSCA_RR
 * %%
 * Copyright (C) 2017 Stuttgart Uni, IAAS
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

public enum Resolving {
	UNDEFINED, // 0
	EXPANDING, // 1
	ADDITION;// 2

	/**
	 * read resolving method from String
	 * 
	 * @param input
	 *            String to parse
	 * @return resolving method
	 */
	public static Resolving fromString(String input) {
		if (input.equals("REPLACEMENT"))
			return EXPANDING;
		if (input.equals("ADDITION"))
			return ADDITION;
		return UNDEFINED;
	}

	/**
	 * translate resolving method to String
	 * 
	 * @param res
	 *            resolving method
	 * @return String
	 */
	public static String toString(Resolving res) {
		switch (res) {
		case EXPANDING:
			return "REPLACEMENT";
		case ADDITION:
			return "ADDITION";
		default:
			return "UNDEFINED";
		}
	}

	/**
	 * read resolving method from int
	 * 
	 * @param input
	 *            int to parse
	 * @return resolving method
	 */
	public static Resolving fromInt(int input) {
		if (input == 1)
			return EXPANDING;
		if (input == 2)
			return ADDITION;
		return UNDEFINED;
	}

	/**
	 * translate resolving method to String
	 * 
	 * @param res
	 *            resolving method
	 * @return int
	 */
	public static int toInt(Resolving res) {
		switch (res) {
		case EXPANDING:
			return 1;
		case ADDITION:
			return 2;
		default:
			return 0;
		}
	}
}
