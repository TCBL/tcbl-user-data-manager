package be.ugent.idlab.tcbl.userdatamanager.model;

import gluu.scim2.client.Scim2Client;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class ScimTCBLUserRepository implements TCBLUserRepository {

	private final ConcurrentMap<String, TCBLUser> users = new ConcurrentHashMap<>(); //TODO: remove
	private final Scim2Client client;

	public ScimTCBLUserRepository(final Environment environment) {
		Map<String, String> clientProperties = resolveScimClientProperties(environment);
		String domain = clientProperties.get("domain");
		String metaDataUrl = clientProperties.get("meta-data-url");
		String clientId = clientProperties.get("aat-client-id");
		String jksPath = clientProperties.get("aat-client-jks-path");
		String jksPassword = clientProperties.get("aat-client-jks-password");
		String keyId = clientProperties.get("aat-client-key-id");
		client = Scim2Client.umaInstance(domain, metaDataUrl, clientId, jksPath, jksPassword, keyId);
	}

	@Override
	public Iterable<TCBLUser> findAll() {
		return users.values();
	}

	@Override
	public TCBLUser save(TCBLUser tcblUser) {
		String userName = tcblUser.getUserName();
		users.put(userName, tcblUser);
		return tcblUser;
	}

	@Override
	public TCBLUser find(String userName) {
		return users.get(userName);
	}

	@Override
	public void deleteTCBLUser(String userName) {
		users.remove(userName);
	}

	private static Map<String, String> resolveScimClientProperties(final Environment environment) {
		Binder binder = Binder.get(environment);
		BindResult<Map<String, String>> result = binder.bind(
				"security.scim", Bindable.mapOf(String.class, String.class));
		return result.get();
	}
}
