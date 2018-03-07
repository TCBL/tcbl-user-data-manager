package be.ugent.idlab.tcbl.userdatamanager.controller.support;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

/**
 * A storage interface for profile picture files.
 *
 * Inspired on https://spring.io/guides/gs/uploading-files/
 *
 * <p>Copyright 2018 IDLab (Ghent University - imec)</p>
 *
 * @author Martin Vanbrabant
 */

public interface ProfilePictureStorage {

	class LoadResult {
		public Resource resource;
		public MediaType mediaType;
	}

	/**
	 * Stores a profile picture file for a user
	 * @param file the file as a MultipartFile (can be empty)
	 * @param username the name of the user
	 * @return key to refer to this file in other methods of this interface (null if the file was empty)
	 */
	String store(MultipartFile file, String username);

	/**
	 * Loads a profile picture file for a user
	 * @param key as returned from the store method
	 * @return the picture file information
	 */
	LoadResult load(String key);

	/**
	 * Deletes a profile picture file for a user
	 * @param key as returned from the store method
	 */
	void delete(String key);

}
