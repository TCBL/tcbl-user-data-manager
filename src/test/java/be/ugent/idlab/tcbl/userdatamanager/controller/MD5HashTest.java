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

package be.ugent.idlab.tcbl.userdatamanager.controller;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

import static junit.framework.TestCase.assertEquals;

/**
 * @author Gerald Haesendonck
 */
public class MD5HashTest {

	@Test
	public void md5Test() throws NoSuchAlgorithmException {
		String input = "urist.mcvankab+8@freddiesjokes.com";
		String expectedHash = "20dbbf20d91106a9377bb671ba83f381";
		String hashValue = org.springframework.util.DigestUtils.md5DigestAsHex(input.getBytes(StandardCharsets.UTF_8));
		assertEquals(expectedHash, hashValue);
	}
}
