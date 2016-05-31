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

package fr.treeptik.cloudunit.service;

import fr.treeptik.cloudunit.exception.ServiceException;
import fr.treeptik.cloudunit.model.Image;

import java.util.List;

public interface ImageService {

    Image update(Image image)
        throws ServiceException;

    Image create(Image image)
        throws ServiceException;

    void remove(Image image)
        throws ServiceException;

    Image findById(Integer id)
        throws ServiceException;

    List<Image> list()
        throws ServiceException;

    Image findByName(String name)
        throws ServiceException;

    Image findByRepositoryAndTag(String repository, String tag)
            throws ServiceException;

    List<Image> filterBytType(String type)
        throws ServiceException;

}
