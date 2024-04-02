package edu.alexey.messengerclient.utils;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Properties;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import com.google.common.base.Objects;

import edu.alexey.messengerclient.bundles.LocaleManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomProperties {

	static final String FILENAME = "custom.properties";
	static final String KEY_CLIENT_UUID = "client_uuid";
	static final String KEY_SERVER_HOST = "host";
	static final String KEY_SERVER_PORT = "port";
	static final String KEY_USERNAME = "username";
	static final String KEY_PASSWORD = "password";
	static final String KEY_DISPLAY_NAME = "display_name";
	static final String KEY_LANGUAGE = "language";

	static final String DEFAULT_SERVER_HOST = "localhost";
	static final String DEFAULT_SERVER_PORT = "8080";
	static final String DEFAULT_LANGUAGE = LocaleManager.LANGUAGES.getFirst().getCode();

	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private volatile boolean suppressPropertyChangeEvent;

	public CustomProperties() {
		try {
			load();
		} catch (IOException e) {}
	}

	private UUID clientUuid;
	private String language;
	private String serverHost;
	private String serverPort;
	private String displayName;
	private String username;
	private String password;

	public UUID getClientUuid() {
		return clientUuid;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String value) {
		value = StringUtils.nullifyEmpty(value);
		String oldValue = this.language;
		if (!Objects.equal(value, oldValue)) {
			this.language = value;
			if (!suppressPropertyChangeEvent)
				pcs.firePropertyChange("language", oldValue, value);
		}
	}

	public String getServerHost() {
		return serverHost;
	}

	public void setServerHost(String value) {
		value = StringUtils.nullifyEmpty(value);
		String oldValue = this.serverHost;
		if (!Objects.equal(value, oldValue)) {
			this.serverHost = value;
			if (!suppressPropertyChangeEvent)
				pcs.firePropertyChange("serverHost", oldValue, value);
		}
	}

	public String getServerPort() {
		return serverPort;
	}

	public void setServerPort(String value) {
		value = StringUtils.nullifyEmpty(value);
		String oldValue = this.serverPort;
		if (!Objects.equal(value, oldValue)) {
			this.serverPort = value;
			if (!suppressPropertyChangeEvent)
				pcs.firePropertyChange("serverPort", oldValue, value);
		}
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String value) {
		value = StringUtils.nullifyEmpty(value);
		String oldValue = this.displayName;
		if (!Objects.equal(value, oldValue)) {
			this.displayName = value;
			if (!suppressPropertyChangeEvent)
				pcs.firePropertyChange("displayName", oldValue, value);
		}
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String value) {
		value = StringUtils.nullifyEmpty(value);
		String oldValue = this.username;
		if (!Objects.equal(value, oldValue)) {
			this.username = value;
			if (!suppressPropertyChangeEvent)
				pcs.firePropertyChange("username", oldValue, value);
		}
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String value) {
		value = StringUtils.nullifyEmpty(value);
		String oldValue = this.password;
		if (!Objects.equal(value, oldValue)) {
			this.password = value;
			if (!suppressPropertyChangeEvent)
				pcs.firePropertyChange("password", oldValue, value);
		}
	}

	private void load() throws IOException {
		clear();

		File file = Path.of(FILENAME).toFile();
		if (!file.exists()) {
			return;
		}

		Properties properties = new Properties();
		try (FileInputStream inputStream = new FileInputStream(file)) {

			properties.load(inputStream);
			load(properties);

		} catch (IOException e) {
			log.error("Unable to read custom properties.", e);
			clear();
		}
	}

	private void load(Properties properties) {
		String clientUuidStr = properties.getProperty(KEY_CLIENT_UUID);
		this.clientUuid = clientUuidStr != null ? UUID.fromString(clientUuidStr) : UUID.randomUUID();
		this.language = properties.getProperty(KEY_LANGUAGE, DEFAULT_LANGUAGE);
		this.serverHost = properties.getProperty(KEY_SERVER_HOST, DEFAULT_SERVER_HOST);
		this.serverPort = properties.getProperty(KEY_SERVER_PORT, DEFAULT_SERVER_PORT);
		this.displayName = properties.getProperty(KEY_DISPLAY_NAME, null);
		this.username = properties.getProperty(KEY_USERNAME, null);
		String passwordEncoded = properties.getProperty(KEY_PASSWORD, null);
		this.password = null;
		if (!StringUtils.isNullOrBlank(passwordEncoded)) {
			try {
				this.password = decrypt(passwordEncoded);
			} catch (Exception e) {
				log.error("Cannot decode password.", e);
				throw new RuntimeException(e);
			}
		}
	}

	private void clear() {
		this.clientUuid = UUID.randomUUID();
		this.language = DEFAULT_LANGUAGE;
		this.serverHost = DEFAULT_SERVER_HOST;
		this.serverPort = DEFAULT_SERVER_PORT;
		this.displayName = null;
		this.username = null;
		this.password = null;
	}

	public void save() throws IOException {
		Properties properties = new Properties();

		setPropertyNullFriendly(properties, KEY_CLIENT_UUID, this.clientUuid);
		setPropertyNullFriendly(properties, KEY_LANGUAGE, this.language);
		setPropertyNullFriendly(properties, KEY_SERVER_HOST, this.serverHost);
		setPropertyNullFriendly(properties, KEY_SERVER_PORT, this.serverPort);
		setPropertyNullFriendly(properties, KEY_DISPLAY_NAME, this.displayName);
		setPropertyNullFriendly(properties, KEY_USERNAME, this.username);
		String passwordEncoded = null;
		if (!StringUtils.isNullOrBlank(this.password)) {
			try {
				passwordEncoded = encrypt(this.password);
			} catch (Exception e) {
				log.error("Cannot encode password.", e);
				throw new RuntimeException(e);
			}
		}
		setPropertyNullFriendly(properties, KEY_PASSWORD, passwordEncoded);

		File file = Path.of(FILENAME).toFile();
		try (FileOutputStream outputStream = new FileOutputStream(file)) {
			properties.store(outputStream, "Custom Properties");
		} catch (IOException e) {
			log.error("Unable to save custom properties.", e);
			throw e;
		}
	}

	public void addPropertyChangeListener(PropertyChangeListener pcl) {
		pcs.addPropertyChangeListener(pcl);
	}

	public void removePropertyChangeListener(PropertyChangeListener pcl) {
		pcs.removePropertyChangeListener(pcl);
	}

	private static final byte KEY[] = new byte[] { -14, 117, -23, 86, -121, 7, -19, 22, -35, 114, 34, 76, 60, 46, -15,
			102 };
	private static final byte IV[] = new byte[] { 24, 69, -32, -51, 47, 2, -69, -119, 54, -46, 23, -31, -111, -106, 41,
			58 };

	private String encrypt(String inputStr)
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

		if (encrypted.length != encLen) {
			encrypted = Arrays.copyOf(encrypted, encLen);
		}

		byte[] base64encoded = Base64.getEncoder().encode(encrypted);
		return new String(base64encoded);
	}

	private String decrypt(String inputStr)
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

		if (decrypted.length != decLen) {
			decrypted = Arrays.copyOf(decrypted, decLen);
		}

		return new String(decrypted);
	}

	public void setSuppressPropertyChangeEvent(boolean suppressPropertyChangeEvent) {
		this.suppressPropertyChangeEvent = suppressPropertyChangeEvent;
	}

	private static void setPropertyNullFriendly(Properties properties, String key, Object value) {
		if (value != null) {
			properties.setProperty(key, value.toString());
		}
	}

	//	public static void main(String[] args) throws Exception {
	//		var encoded = new CustomProperties().encrypt("strongpass");
	//		System.out.println(encoded);
	//		var decoded = new CustomProperties().decrypt(encoded);
	//		System.out.println(decoded);
	//		System.out.println(Arrays.toString(decoded.getBytes()));
	//	}
}
