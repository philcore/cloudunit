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

import fr.treeptik.cloudunit.dao.ApplicationDAO;
import fr.treeptik.cloudunit.dao.ImageDAO;
import fr.treeptik.cloudunit.dto.ContainerUnit;
import fr.treeptik.cloudunit.exception.CheckException;
import fr.treeptik.cloudunit.exception.ServiceException;
import fr.treeptik.cloudunit.model.*;
import fr.treeptik.cloudunit.service.*;
import fr.treeptik.cloudunit.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.PersistenceException;
import java.io.File;
import java.util.*;

@Service
public class ApplicationServiceImpl
        implements ApplicationService {

    Locale locale = Locale.ENGLISH;

    private Logger logger = LoggerFactory
            .getLogger(ApplicationServiceImpl.class);

    @Inject
    private ApplicationDAO applicationDAO;

    @Inject
    private ImageDAO imageDAO;

    @Inject
    private ServerService serverService;

    @Inject
    private ImageService imageService;

    @Inject
    private ShellUtils shellUtils;

    @Inject
    private AuthentificationUtils authentificationUtils;

    @Inject
    private UserService userService;

    @Inject
    private MessageSource messageSource;

    @Value("${docker.manager.ip:192.168.50.4:2376}")
    private String dockerManagerIp;

    @Value("${suffix.cloudunit.io}")
    private String suffixCloudUnitIO;

    @Value("${java.version.default}")
    private String javaVersionDefault;

    @Value("${cloudunit.manager.ip}")
    private String restHost;

    @Value("${cloudunit.instance.name}")
    private String cuInstanceName;

    @Value("${cloudunit.manager.ip}")
    private String hostName2;

    public ApplicationDAO getApplicationDAO() {
        return this.applicationDAO;
    }

    /**
     * Check if the application already exists for the user
     *
     * @param application
     * @param repository
     * @throws CheckException
     * @throws ServiceException
     */
    @Override
    public void checkCreate(Application application, String repository)
            throws CheckException, ServiceException {

        logger.debug("--CHECK APP COUNT--");
        try {
            if (checkAppExist(application.getUser(), application.getName())) {
                throw new CheckException(messageSource.getMessage("app.exists", null, locale));
            }
            if (checkNameLength(application.getName())) {
                throw new CheckException("This name has length equal to zero : " + application.getName());
            }
        } catch (Exception e) {
            StringBuilder msgError = new StringBuilder();
            msgError.append(application).append(", ").append("repository=").append(repository);
            throw new ServiceException(msgError.toString(), e);
        }

    }

    /**
     * Test if the application already exists
     *
     * @param user
     * @param applicationName
     * @return
     * @throws ServiceException
     * @throws CheckException
     */
    @Override
    public boolean checkAppExist(User user, String applicationName)
            throws ServiceException, CheckException {
        return applicationDAO.countByNameAndUser(user.getId(), applicationName) > 0;
    }

    public boolean checkNameLength(String applicationName) {
        if (applicationName.length() == 0)
            return true;
        return false;
    }

    /**
     * Save app in just in DB, not create container use principally to charge
     * status.PENDING of entity until it's really functionnal
     */
    @Override
    @Transactional
    public Application saveInDB(Application application)
            throws ServiceException {
        logger.debug("-- SAVE -- : " + application);
        // Do not affect application with save return.
        // You could lose the relationships.
        applicationDAO.save(application);
        return application;
    }

    /**
     * Create a new application
     *
     * @param user
     * @param applicationName
     * @param repository
     * @param tag
     * @return
     * @throws ServiceException
     * @throws CheckException
     */
    @Transactional(rollbackFor = ServiceException.class)
    public Application create(User user, String applicationName, String repository, String tag)
            throws ServiceException,
            CheckException {

        Application application = new Application();

        if (logger.isDebugEnabled()) {
            logger.debug("applicationName="+applicationName);
            logger.debug("repository="+applicationName);
            logger.debug("applicationName="+tag);
        }

        application.setName(applicationName);
        application.setDisplayName(applicationName);
        application.setUser(user);

        // verify if application exists already
        this.checkCreate(application, repository);

        // Application is now pending
        application.setStatus(Status.PENDING);
        application = this.saveInDB(application);

        try {
            // BLOC SERVER
            Server server = new Server();
            Image image = imageService.findByRepositoryAndTag(repository, tag);
            server.setImage(image);
            server.setApplication(application);
            server = serverService.create(repository, tag);

            List<Server> servers = new ArrayList<>();
            servers.add(server);
            application.setServers(servers);
            application = applicationDAO.save(application);

        } catch (DataAccessException e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }

        return application;
    }

    /**
     * Remove an application
     *
     * @param application
     * @param user
     * @return
     * @throws ServiceException
     */
    @Override
    @Transactional
    public Application remove(Application application, User user)
            throws ServiceException, CheckException {

        throw new RuntimeException("NOT IMPLEMENTED");
    }

    /**
     * Methode permettant de mettre l'application dans un état particulier pour
     * se prémunir d'éventuel problème de concurrence au niveau métier
     */
    @Override
    @Transactional
    public void setStatus(Application application, Status status)
            throws ServiceException {
        try {
            Application _application = applicationDAO.findOne(application.getId());
            _application.setStatus(status);
            application.setStatus(status);
            applicationDAO.saveAndFlush(_application);
        } catch (PersistenceException e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    @Transactional
    public Application start(Application application)
            throws ServiceException {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    @Override
    @Transactional
    public Application stop(Application application)
            throws ServiceException {
        throw new RuntimeException("NOT IMPLEMENTED");
    }




}
