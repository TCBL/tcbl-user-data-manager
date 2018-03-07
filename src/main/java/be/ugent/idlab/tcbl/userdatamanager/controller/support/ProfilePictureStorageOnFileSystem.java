package be.ugent.idlab.tcbl.userdatamanager.controller.support;

import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

/**
 * Based on https://spring.io/guides/gs/uploading-files/
 *
 * A storage implementation for profile picture files. Stores on the file system.
 *
 * Inspired on https://spring.io/guides/gs/uploading-files/
 *
 * <p>Copyright 2018 IDLab (Ghent University - imec)</p>
 *
 * @author Martin Vanbrabant
 */
public class ProfilePictureStorageOnFileSystem implements ProfilePictureStorage {

	private final Path rootLocation;
	private final Map<String, MediaType> mediaTypeMap;

	public ProfilePictureStorageOnFileSystem(Environment environment) {
		this.rootLocation = Paths.get("profile-pictures");
		this.mediaTypeMap = new HashMap<String, MediaType>();
		this.mediaTypeMap.put(".jpg", MediaType.IMAGE_JPEG);
		this.mediaTypeMap.put(".png", MediaType.IMAGE_PNG);

		try {
			Files.createDirectories(rootLocation);
		}
		catch (IOException e) {
			throw new StorageException("Could not initialize storage", e);
		}
	}

	@Override
	public String store(MultipartFile profilePictureFile, String username) {
		// make a simple yet (hopefully) unique key, suitable as filename base
		String key = username.replaceAll("[^A-Za-z0-9]", "_");
		if (profilePictureFile.isEmpty()) {
			// delete all possible previous files
			delete(key);
			key = null;
		}
		else {
			String contentType = profilePictureFile.getContentType();
			String fileExtension = null;
			for (Map.Entry<String, MediaType> entry : mediaTypeMap.entrySet()) {
				if (entry.getValue().toString().equals(contentType)) {
					fileExtension = entry.getKey();
					break;
				}
			}
			if (fileExtension == null) {
				throw new BadContentTypeException(String.format("Content type '%s' is not supported", contentType));
			}
			String filename = key + fileExtension;
			try {
				// save/overwrite this file
				Files.copy(profilePictureFile.getInputStream(), rootLocation.resolve(filename),
						StandardCopyOption.REPLACE_EXISTING);
				// delete possible previous files (with different extension) for same key
				for (String fx : mediaTypeMap.keySet()) {
					if (!fx.equals(fileExtension)) {
						Files.deleteIfExists(rootLocation.resolve(key + fx));
					}
				}
			}
			catch (IOException e) {
				throw new StorageException("Failed to store file: " + filename, e);
			}
		}
		return key;
	}

	@Override
	public LoadResult load(String key) {
		LoadResult ret = null;
		try {
			for (Map.Entry<String, MediaType> entry : mediaTypeMap.entrySet()) {
				Path file = rootLocation.resolve(key + entry.getKey());
				Resource resource = new UrlResource(file.toUri());
				if (resource.exists() && resource.isReadable()) {
					ret = new LoadResult();
					ret.resource = resource;
					ret.mediaType = entry.getValue();
					break;
				}
			}
			if (ret == null) {
				throw new StorageFileNotFoundException("Could not read file for: " + key);
			}
		}
		catch (MalformedURLException e) {
			throw new StorageFileNotFoundException("Could not read file for: " + key, e);
		}
		return ret;
	}

	@Override
	public void delete(String key) {
		try {
			for (String fx : mediaTypeMap.keySet()) {
				Files.deleteIfExists(rootLocation.resolve(key + fx));
			}
		} catch (IOException e) {
			throw new StorageException("Failed to delete file for:" + key, e);
		}
	}

}
