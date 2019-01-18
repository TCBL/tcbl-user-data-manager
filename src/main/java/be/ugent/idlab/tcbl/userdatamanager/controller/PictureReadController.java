/*
 *  Copyright 2019 imec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package be.ugent.idlab.tcbl.userdatamanager.controller;

import be.ugent.idlab.tcbl.userdatamanager.controller.support.PictureStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
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
