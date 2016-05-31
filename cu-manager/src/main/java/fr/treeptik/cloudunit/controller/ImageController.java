/*
 * LICENCE : CloudUnit is available under the GNU Affero General Public License : https://gnu.org/licenses/agpl.html
 * but CloudUnit is licensed too under a standard commercial license.
 * Please contact our sales team if you would like to discuss the specifics of our Enterprise license.
 * If you are not sure whether the AGPL is right for you,
 * you can always test our software under the AGPL and inspect the source code before you contact us
 * about purchasing a commercial license.
 *
 * LEGAL TERMS : "CloudUnit" is a registered trademark of Treeptik and can't be used to endorse
 * or promote products derived from this project without prior written permission from Treeptik.
 * Products or services derived from this software may not be called "CloudUnit"
 * nor may "Treeptik" or similar confusing terms appear in their names without prior written permission.
 * For any questions, contact us : contact@treeptik.fr
 */

package fr.treeptik.cloudunit.controller;

import fr.treeptik.cloudunit.exception.ServiceException;
import fr.treeptik.cloudunit.model.Image;
import fr.treeptik.cloudunit.service.ImageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.List;

@Controller
@RequestMapping("/image")
public class ImageController {

    @Inject
    private ImageService imageService;

    @Value("${api.version}")
    private String apiVersion;

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Image> listAllImages()
        throws ServiceException {
        return imageService.list();
    }

    @RequestMapping(value = "/module/enabled", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Image> listAllEnabledModuleImages()
        throws ServiceException {
        return imageService.filterBytType("module");
    }

    @RequestMapping(value = "/server/enabled", method = RequestMethod.GET)
    public
    @ResponseBody
    List<Image> listAllEnabledServerImages()
        throws ServiceException {
        return imageService.filterBytType("server");
    }

    @RequestMapping(value = "/version", method = RequestMethod.GET)
    public
    @ResponseBody
    String getVersion() {
        return apiVersion;
    }

}
