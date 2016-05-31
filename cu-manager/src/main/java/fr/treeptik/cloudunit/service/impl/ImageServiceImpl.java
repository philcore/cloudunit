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

package fr.treeptik.cloudunit.service.impl;

import fr.treeptik.cloudunit.dao.ImageDAO;
import fr.treeptik.cloudunit.exception.ServiceException;
import fr.treeptik.cloudunit.model.Image;
import fr.treeptik.cloudunit.service.ImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.PersistenceException;
import java.util.List;

@Service
public class ImageServiceImpl
        implements ImageService {
    private Logger logger = LoggerFactory.getLogger(ImageServiceImpl.class);

    @Inject
    private ImageDAO imageDAO;

    public ImageDAO getImageDAO() {
        return this.imageDAO;
    }

    @Override
    @Transactional
    public Image create(Image image)
            throws ServiceException {

        logger.debug("create : Methods parameters : " + image.toString());
        logger.info("ImageService : Starting creating image " + image.getName());

        try {
            imageDAO.save(image);
        } catch (PersistenceException e) {
            logger.error("ImageService Error : Create Image" + e);
            throw new ServiceException(e.getLocalizedMessage(), e);
        }

        logger.info("ImageService : Image " + image.getName()
                + "successfully created.");

        return image;
    }

    @Override
    @Transactional
    public Image update(Image image)
            throws ServiceException {

        logger.debug("update : Methods parameters : " + image.toString());
        logger.info("ImageService : Starting updating image " + image.getName());

        try {
            imageDAO.saveAndFlush(image);
        } catch (PersistenceException e) {
            logger.error("ImageService Error : update Image" + e);
            throw new ServiceException(e.getLocalizedMessage(), e);
        }

        logger.info("ImageService : Image " + image.getName()
                + "successfully updated.");

        return image;
    }

    @Override
    @Transactional
    public void remove(Image image)
            throws ServiceException {
        try {
            logger.debug("remove : Methods parameters : " + image.toString());
            logger.info("Starting removing application " + image.getName());

            imageDAO.delete(image);

            logger.info("ImageService : Image successfully removed ");

        } catch (PersistenceException e) {

            logger.error("ImageService Error : failed to remove "
                    + image.getName() + " : " + e);

            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public Image findById(Integer id)
            throws ServiceException {
        try {
            logger.debug("findById : Methods parameters : " + id);
            Image image = imageDAO.findOne(id);
            logger.info("image with id " + id + " found!");
            return image;
        } catch (PersistenceException e) {
            logger.error("Error ImageService : error findById Method : " + e);
            throw new ServiceException(e.getLocalizedMessage(), e);

        }
    }

    @Override
    public List<Image> list()
            throws ServiceException {
        try {
            List<Image> images = imageDAO.list();
            logger.info("ImageService : All Images found ");
            return images;
        } catch (PersistenceException e) {
            throw new ServiceException(e.getLocalizedMessage(), e);

        }
    }

    @Override
    public List<Image> filterBytType(String type)
            throws ServiceException {
        try {
            List<Image> images = imageDAO.filterByType(type);
            logger.info("ImageService : All Images found ");
            return images;
        } catch (PersistenceException e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public Image findByName(String name)
            throws ServiceException {
        try {
            logger.debug("name=" + name);
            Image image = imageDAO.findByName(name);
            return image;
        } catch (Exception e) {
            throw new ServiceException(name, e);
        }
    }

    @Override
    public Image findByRepositoryAndTag(String repository, String tag)
            throws ServiceException {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("repository=" + repository);
                logger.debug("tag=" + tag);
            }
            Image image = imageDAO.findByRepositoryAndTag(repository, tag);
            return image;
        } catch (Exception e) {
            throw new ServiceException(repository+":"+tag, e);
        }
    }

}
