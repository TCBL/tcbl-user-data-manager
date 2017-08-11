package be.ugent.idlab.tcbl.userdatamanager.controller;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */

public class CodecTest {

	@Test
	public void codec64Test() {
		String id = "test";
		String encodedId = UserController.encodeBase64(id);
		String decodedId = UserController.decodeBase64(encodedId);
		assertEquals(id, decodedId);
	}

	@Test
	public void codecResetPasswordCodeTest() {
		String id = "test";
		String encodedId = UserController.generateResetPasswordCode(id);
		String decodedId = UserController.decodeIdForPassword(encodedId, 2000);
		assertEquals(id, decodedId);
	}

	@Test
	public void codecResetPasswordCodeExpiredTest() throws InterruptedException {
		String id = "test";
		String encodedId = UserController.generateResetPasswordCode(id);
		Thread.sleep(500);
		String decodedId = UserController.decodeIdForPassword(encodedId, 100);
		assertNull(decodedId);
	}

}
