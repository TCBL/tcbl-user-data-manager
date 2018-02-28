package be.ugent.idlab.tcbl.userdatamanager.controller;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

import static junit.framework.TestCase.assertEquals;

/**
 * <p>Copyright 2018 IDLab (Ghent University - imec)</p>
 *
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
