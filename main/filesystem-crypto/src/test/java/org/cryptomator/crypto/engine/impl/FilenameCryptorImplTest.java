/*******************************************************************************
 * Copyright (c) 2015 Sebastian Stenzel and others.
 * This file is licensed under the terms of the MIT license.
 * See the LICENSE.txt file for more info.
 *
 * Contributors:
 *     Sebastian Stenzel - initial API and implementation
 *******************************************************************************/
package org.cryptomator.crypto.engine.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.cryptomator.crypto.engine.AuthenticationFailedException;
import org.cryptomator.crypto.engine.FilenameCryptor;
import org.junit.Assert;
import org.junit.Test;

public class FilenameCryptorImplTest {

	@Test
	public void testDeterministicEncryptionOfFilenames() throws IOException {
		final byte[] keyBytes = new byte[32];
		final SecretKey encryptionKey = new SecretKeySpec(keyBytes, "AES");
		final SecretKey macKey = new SecretKeySpec(keyBytes, "AES");
		final FilenameCryptor filenameCryptor = new FilenameCryptorImpl(encryptionKey, macKey);

		// some random
		for (int i = 0; i < 2000; i++) {
			final String origName = UUID.randomUUID().toString();
			final String encrypted1 = filenameCryptor.encryptFilename(origName);
			final String encrypted2 = filenameCryptor.encryptFilename(origName);
			Assert.assertEquals(encrypted1, encrypted2);
			final String decrypted = filenameCryptor.decryptFilename(encrypted1);
			Assert.assertEquals(origName, decrypted);
		}

		// block size length file names
		final String originalPath3 = "aaaabbbbccccdddd"; // 128 bit ascii
		final String encryptedPath3a = filenameCryptor.encryptFilename(originalPath3);
		final String encryptedPath3b = filenameCryptor.encryptFilename(originalPath3);
		Assert.assertEquals(encryptedPath3a, encryptedPath3b);
		final String decryptedPath3 = filenameCryptor.decryptFilename(encryptedPath3a);
		Assert.assertEquals(originalPath3, decryptedPath3);
	}

	@Test
	public void testDeterministicHashingOfDirectoryIds() throws IOException {
		final byte[] keyBytes = new byte[32];
		final SecretKey encryptionKey = new SecretKeySpec(keyBytes, "AES");
		final SecretKey macKey = new SecretKeySpec(keyBytes, "AES");
		final FilenameCryptor filenameCryptor = new FilenameCryptorImpl(encryptionKey, macKey);

		// some random
		for (int i = 0; i < 2000; i++) {
			final String originalDirectoryId = UUID.randomUUID().toString();
			final String hashedDirectory1 = filenameCryptor.hashDirectoryId(originalDirectoryId);
			final String hashedDirectory2 = filenameCryptor.hashDirectoryId(originalDirectoryId);
			Assert.assertEquals(hashedDirectory1, hashedDirectory2);
		}
	}

	@Test(expected = AuthenticationFailedException.class)
	public void testDecryptionOfManipulatedFilename() {
		final byte[] keyBytes = new byte[32];
		final SecretKey encryptionKey = new SecretKeySpec(keyBytes, "AES");
		final SecretKey macKey = new SecretKeySpec(keyBytes, "AES");
		final FilenameCryptor filenameCryptor = new FilenameCryptorImpl(encryptionKey, macKey);

		final byte[] encrypted = filenameCryptor.encryptFilename("test").getBytes(StandardCharsets.UTF_8);
		encrypted[0] ^= (byte) 0x01; // change 1 bit in first byte
		filenameCryptor.decryptFilename(new String(encrypted, StandardCharsets.UTF_8));
	}

}