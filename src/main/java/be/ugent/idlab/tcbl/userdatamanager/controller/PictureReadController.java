package be.ugent.idlab.tcbl.userdatamanager.controller;

import be.ugent.idlab.tcbl.userdatamanager.controller.support.PictureStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * <p>Copyright 2018 IDLab (Ghent University - imec)</p>
 *
 * @author Martin Vanbrabant
 */
@RestController
@RequestMapping("/p")
public class PictureReadController {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final PictureStorage pictureStorage;

	/**
	 * Creates a PictureReadController; Spring injects the parameters.
	 */
	public PictureReadController(PictureStorage pictureStorage) {
		this.pictureStorage = pictureStorage;
	}

	@GetMapping("/{category}/{key}")
	public ResponseEntity<Resource> servePicture(@PathVariable String category, @PathVariable String key) {
		try {
			PictureStorage.LoadResult loadResult = pictureStorage.load(category, key);
			ResponseEntity<Resource> ret = ResponseEntity.ok().contentType(loadResult.mediaType).body(loadResult.resource);
			log.info(String.format("Serving picture for category '%s', key '%s'", category, key));
			return ret;
		} catch (Exception e) {
			log.info(String.format("Not found: picture for category '%s', key '%s'", category, key));
			return ResponseEntity.notFound().build();
		}
	}

}
