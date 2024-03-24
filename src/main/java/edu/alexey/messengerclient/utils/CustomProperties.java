package edu.alexey.messengerclient.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CustomProperties {

	static final String FILENAME = "custom.properties";
	static final String KEY_LOGIN = "login";
	static final String KEY_PASSWORD = "password";

	private String login;

	private String passwordEncoded;

	public CustomProperties() {
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		if (passwordEncoded == null) {
			return null;
		}
		try {
			return decode(passwordEncoded);
		} catch (Exception e) {
			log.error("Cannot decode password.", e);
			throw new RuntimeException(e);
		}
	}

	public void setPassword(String password) {
		if (password == null || password.isBlank()) {
			throw new IllegalArgumentException();
		}
		try {
			this.passwordEncoded = encode(password);
		} catch (Exception e) {
			log.error("Cannot encode password.", e);
			throw new RuntimeException(e);
		}
	}

	public void load() throws IOException {
		clear();

		File file = Path.of(FILENAME).toFile();
		if (!file.exists()) {
			return;
		}

		Properties properties = new Properties();
		try (FileInputStream inputStream = new FileInputStream(file)) {
			properties.load(inputStream);
		} catch (IOException e) {
			log.error("Unable to read custom properties.", e);
			throw e;
		}
		load(properties);
	}

	private void load(Properties properties) {
		this.login = properties.getProperty(KEY_LOGIN);
		this.passwordEncoded = properties.getProperty(KEY_PASSWORD);
	}

	private void clear() {
		login = null;
		passwordEncoded = null;
	}

	public void save() throws IOException {
		Properties properties = new Properties();
		properties.setProperty(KEY_LOGIN, login);
		properties.setProperty(KEY_PASSWORD, passwordEncoded);

		File file = Path.of(FILENAME).toFile();
		try (FileOutputStream outputStream = new FileOutputStream(file)) {
			properties.store(outputStream, "Custom Properties");
		} catch (IOException e) {
			log.error("Unable to save custom properties.", e);
			throw e;
		}
	}

	private static final byte KEY[] = new byte[] { -14, 117, -23, 86, -121, 7, -19, 22, -35, 114, 34, 76, 60, 46, -15,
			102 };
	private static final byte IV[] = new byte[] { 24, 69, -32, -51, 47, 2, -69, -119, 54, -46, 23, -31, -111, -106, 41,
			58 };

	private String encode(String inputStr)
			throws NoSuchAlgorithmException,
			NoSuchPaddingException,
			InvalidKeyException,
			InvalidAlgorithmParameterException,
			ShortBufferException,
			IllegalBlockSizeException,
			BadPaddingException {

		byte[] input = inputStr.getBytes();
		SecretKeySpec key = new SecretKeySpec(KEY, "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(IV);
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
		byte[] encrypted = new byte[cipher.getOutputSize(input.length)];
		int encLen = cipher.update(input, 0, input.length, encrypted, 0);
		encLen += cipher.doFinal(encrypted, encLen);

		byte[] base64encoded = Base64.getEncoder().encode(encrypted);
		return new String(base64encoded);
	}

	private String decode(String inputStr)
			throws NoSuchAlgorithmException,
			NoSuchPaddingException,
			InvalidKeyException,
			InvalidAlgorithmParameterException,
			ShortBufferException,
			IllegalBlockSizeException,
			BadPaddingException {
		byte[] input = Base64.getDecoder().decode(inputStr);
		SecretKeySpec key = new SecretKeySpec(KEY, "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(IV);
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
		byte[] decrypted = new byte[cipher.getOutputSize(input.length)];
		int decLen = cipher.update(input, 0, input.length, decrypted, 0);
		decLen += cipher.doFinal(decrypted, decLen);

		return new String(decrypted);
	}

	//	public static void main(String[] args)
	//			throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
	//			InvalidAlgorithmParameterException, ShortBufferException, IllegalBlockSizeException, BadPaddingException {
	//
	//		var tmp = Stream.<Integer>generate(() -> ThreadLocalRandom.current().nextInt(-128, 128)).limit(16).toArray();
	//		System.out.println(Arrays.toString(tmp));
	//
	//		CustomProperties customProperties = new CustomProperties();
	//
	//		customProperties.load();
	//		System.out.println(customProperties.getLogin());
	//		System.out.println(customProperties.getPassword());
	//
	//		customProperties.setLogin("buzz");
	//		customProperties.setPassword("parole");
	//		customProperties.save();
	//
	//		CustomProperties customProperties2 = new CustomProperties();
	//
	//		customProperties2.load();
	//		System.out.println(customProperties2.getLogin());
	//		System.out.println(customProperties2.getPassword());
	//
	//		customProperties.setLogin("another@user");
	//		customProperties.setPassword("strongpass");
	//		customProperties.save();
	//
	//		customProperties2.load();
	//		System.out.println(customProperties2.getLogin());
	//		System.out.println(customProperties2.getPassword());
	//
	//		System.out.println();
	//		Provider[] providers = Security.getProviders();
	//		System.out.println(Arrays.toString(providers));
	//
	//		String encodedPass = customProperties.encode("My@Str^ong%Pass");
	//		System.out.println(encodedPass);
	//
	//		System.out.println(customProperties.decode(encodedPass));
	//	}

}
