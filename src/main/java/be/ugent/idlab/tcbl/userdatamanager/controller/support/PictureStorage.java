package be.ugent.idlab.tcbl.userdatamanager.controller.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

/**
 * A storage on the file system for picture files.
 *
 * Inspired on https://spring.io/guides/gs/uploading-files/
 *
 * <p>Copyright 2018 IDLab (Ghent University - imec)</p>
 *
 * @author Martin Vanbrabant
 */
public class PictureStorage {

	public class LoadResult {
		public Resource resource;
		public MediaType mediaType;
	}

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final static int MAX_FILENAMELENGTH = 255; // https://en.wikipedia.org/wiki/Comparison_of_file_systems#Limits
	private final Path rootLocation;
	private final Map<String, MediaType> mediaTypeMap;

	public PictureStorage(Environment environment) {
		this.rootLocation = Paths.get("picture-storage");
		this.mediaTypeMap = new HashMap<String, MediaType>();
		this.mediaTypeMap.put(".jpg", MediaType.IMAGE_JPEG);
		this.mediaTypeMap.put(".png", MediaType.IMAGE_PNG);

		try {
			Files.createDirectories(rootLocation);
		}
		catch (IOException e) {
			String msg = "Could not initialize picture storage";
			log.error(msg);
			throw new StorageException(msg, e);
		}
	}

	/**
	 * Stores a picture file. In case of an empty MultipartFile, any previous picture for the same key will be deleted.
	 * @param mpf the file as a MultipartFile (can be empty)
	 * @param category a picture category, containing valid filename characters only
	 * @param key unique key, containing valid filename characters only
	 * @return true for non-empty mpf input; else false
	 */
	public boolean store(MultipartFile mpf, String category, String key) {
		try {
			if (category.length() > MAX_FILENAMELENGTH) {
				String msg = String.format("Category name '%s' too long", category);
				log.error(msg);
				throw new Exception(msg);
			}
			if (key.length() > MAX_FILENAMELENGTH - 4) {
				String msg = String.format("Filename '%s' too long", key);
				log.error(msg);
				throw new Exception(msg);
			}

			if (mpf.isEmpty()) {
				// delete all possible previous files
				delete(category, key);
				return false;
			}
			else {
				String contentType = mpf.getContentType();
				String fileExtension = null;
				for (Map.Entry<String, MediaType> entry : mediaTypeMap.entrySet()) {
					if (entry.getValue().toString().equals(contentType)) {
						fileExtension = entry.getKey();
						break;
					}
				}
				if (fileExtension == null) {
					String msg = String.format("Content type '%s' is not supported in picture storage", contentType);
					log.error(msg);
					throw new BadContentTypeException(msg);
				}
				// save/overwrite this file
				Path dir = rootLocation.resolve(category);
				Files.createDirectories(dir);
				Files.copy(mpf.getInputStream(), dir.resolve(key + fileExtension),
						StandardCopyOption.REPLACE_EXISTING);
				// delete possible previous files (with different extension) for same key
				for (String fx : mediaTypeMap.keySet()) {
					if (!fx.equals(fileExtension)) {
						Files.deleteIfExists(dir.resolve(key + fx));
					}
				}
				log.info(String.format("Stored picture file for category '%s', key '%s' in '%s", category, key,
						dir.resolve(key + fileExtension).toString()));
			}
			return true;
		}
		catch (Exception e) {
			String msg = String.format("Failed to store picture file for category '%s', key '%s'", category, key);
			log.error(msg);
			throw new StorageException(msg, e);
		}
	}

	/**
	 * Loads a picture file
	 * @param category a picture category, containing valid filename characters only
	 * @param key unique key
	 * @return the picture file information
	 */
	public LoadResult load(String category, String key) {
		try {
			LoadResult ret = null;
			for (Map.Entry<String, MediaType> entry : mediaTypeMap.entrySet()) {
				Path dir = rootLocation.resolve(category);
				Path file = dir.resolve(key + entry.getKey());
				Resource resource = new UrlResource(file.toUri());
				if (resource.exists() && resource.isReadable()) {
					ret = new LoadResult();
					ret.resource = resource;
					ret.mediaType = entry.getValue();
					break;
				}
			}
			if (ret == null) {
				throw new NullPointerException();
			}
			log.info(String.format("Loading picture file for category '%s', key '%s'", category, key));
			return ret;
		}
		catch (Exception e) {
			String msg = String.format("Could not find picture file for category '%s', key '%s'", category, key);
			log.error(msg);
			throw new StorageFileNotFoundException(msg);
		}
	}

	/**
	 * Deletes a picture file
	 * @param category a picture category, containing valid filename characters only
	 * @param key unique key
	 */
	public void delete(String category, String key) {
		try {
			Path dir = rootLocation.resolve(category);
			for (String fx : mediaTypeMap.keySet()) {
				Files.deleteIfExists(dir.resolve(key + fx));
			}
			log.info(String.format("Deleted picture file for category '%s', key '%s'", category, key));
		} catch (Exception e) {
			String msg = String.format("Could not delete picture file for category '%s', key '%s'", category, key);
			log.error(msg);
			throw new StorageException(msg, e);
		}
	}

}
