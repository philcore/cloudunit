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
import fr.treeptik.cloudunit.dao.ServerDAO;
import fr.treeptik.cloudunit.exception.CheckException;
import fr.treeptik.cloudunit.exception.DockerJSONException;
import fr.treeptik.cloudunit.exception.ServiceException;
import fr.treeptik.cloudunit.functions.ContainerNaming;
import fr.treeptik.cloudunit.hooks.HookAction;
import fr.treeptik.cloudunit.model.*;
import fr.treeptik.cloudunit.service.*;
import fr.treeptik.cloudunit.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.PersistenceException;
import java.io.UnsupportedEncodingException;
import java.util.*;

@Service
public class ServerServiceImpl
        implements ServerService {

    private Logger logger = LoggerFactory.getLogger(ServerServiceImpl.class);

    @Inject
    private ServerDAO serverDAO;

    @Inject
    private ApplicationDAO applicationDAO;

    @Inject
    private HookService hookService;

    @Inject
    private DockerService dockerService;

    @Inject
    private ShellUtils shellUtils;

    @Value("${cloudunit.max.servers:1}")
    private String maxServers;

    @Value("${suffix.cloudunit.io}")
    private String suffixCloudUnitIO;

    @Value("${database.password}")
    private String databasePassword;

    @Value("${env.exec}")
    private String envExec;

    @Value("${cloudunit.instance.name}")
    private String cuInstanceName;

    @Value("${database.hostname}")
    private String databaseHostname;

    public ServerDAO getServerDAO() {
        return this.serverDAO;
    }

    /**
     * Save app in just in DB, not create container use principally to charge
     * status.PENDING of entity until it's really functionnal
     */
    @Override
    @Transactional
    public Server saveInDB(Server server)
            throws ServiceException {
        server = serverDAO.save(server);
        return server;
    }

    /**
     * Create a server for an image (repository + tag)
     *
     * @param repository
     * @param tag
     * @return
     * @throws ServiceException
     * @throws CheckException
     */
    @Override
    @Transactional
    public Server create(String repository, String tag)
            throws ServiceException, CheckException {

        if (logger.isDebugEnabled()) {
            logger.debug("repository:"+repository);
            logger.debug("tag:"+tag);
        }
        Server server = new Server();

        Application application = server.getApplication();
        User user = server.getApplication().getUser();

        // Build a custom container
        String containerName = null;
        try {
            containerName = ContainerNaming.generateName(user.getLogin(), application.getName(), server.getName());
            logger.debug("containerName:" + containerName);

            String sharedDir = JvmOptionsUtils.extractDirectory(server.getJvmOptions());
            logger.info("shared dir : " + sharedDir);

            dockerService.runContainer(containerName, repository+":"+tag, null);

            server.setStatus(Status.START);
            server.setJvmMemory(512L);
            server.setJvmRelease("jdk1.7.0_55");

            server = this.update(server);

        } catch (Exception e) {
            StringBuilder msgError = new StringBuilder(512);
            msgError.append("server=").append(server);
            msgError.append(", repository=[").append(repository).append("]");
            msgError.append(", tag=[").append(tag).append("]");
            logger.error(msgError.toString(), e);
        }
        logger.info("ServerService : Server " + server.getName()
                + " successfully created.");
        return server;
    }

    /**
     * Test if the user can create new server associated to this application
     *
     * @param application
     * @throws ServiceException
     * @throws CheckException
     */
    public void checkMaxNumberReach(Application application)
            throws ServiceException, CheckException {
        logger.info("check number of server of " + application.getName());
        if (application.getServers() != null) {
            try {
                if (application.getServers().size() >= Integer
                        .parseInt(maxServers)) {
                    throw new CheckException("You have already created your "
                            + maxServers + " server for your application");
                }
            } catch (PersistenceException e) {
                logger.error("ServerService Error : check number of server" + e);
                throw new ServiceException(e.getLocalizedMessage(), e);
            }
        }
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Server update(Server server)
            throws ServiceException {
        try {
            server = serverDAO.save(server);
        } catch (PersistenceException e) {
            logger.error("ServerService Error : update Server" + e);
            throw new ServiceException("Error database : "
                    + e.getLocalizedMessage(), e);
        }
        return server;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Server remove(String serverName)
            throws ServiceException {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    @Override
    public Server findById(Integer id)
            throws ServiceException {
        try {
            logger.debug("findById : Methods parameters : " + id);
            Server server = serverDAO.findOne(id);
            if (server != null) {
                logger.info("Server with id " + id + " found!");
                logger.info("" + server);
            }
            return server;
        } catch (PersistenceException e) {
            logger.error("Error ServerService : error findById Method : " + e);
            throw new ServiceException("Error database :  "
                    + e.getLocalizedMessage(), e);

        }
    }

    @Override
    public List<Server> findAll()
            throws ServiceException {
        try {
            logger.debug("start findAll");
            List<Server> servers = serverDAO.findAll();
            logger.info("ServerService : All Servers found ");
            return servers;
        } catch (PersistenceException e) {
            logger.error("Error ServerService : error findAll Method : " + e);
            throw new ServiceException("Error database :  "
                    + e.getLocalizedMessage(), e);

        }
    }

    @Override
    public List<Server> findAllStatusStartServers()
            throws ServiceException {
        List<Server> listServers = this.findAll();
        List<Server> listStatusStopServers = new ArrayList<>();

        for (Server server : listServers) {
            if (Status.START == server.getStatus()) {
                listStatusStopServers.add(server);
            }
        }
        return listStatusStopServers;
    }

    @Override
    public List<Server> findAllStatusStopServers()
            throws ServiceException {
        List<Server> listServers = this.findAll();
        List<Server> listStatusStopServers = new ArrayList<>();

        for (Server server : listServers) {
            if (Status.STOP == server.getStatus()) {
                listStatusStopServers.add(server);
            }
        }
        return listStatusStopServers;
    }

    @Override
    @Transactional
    public Server startServer(Server server)
            throws ServiceException {

        throw new RuntimeException("NOT IMPLEMENTED");
    }

    @Override
    @Transactional
    public Server stopServer(Server server)
            throws ServiceException {
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    @Override
    public Server findByName(String serverName)
            throws ServiceException {
        try {
            return serverDAO.findByName(serverName);
        } catch (PersistenceException e) {
            throw new ServiceException("Error database : "
                    + e.getLocalizedMessage(), e);
        }
    }

    @Override
    public List<Server> findByApp(Application application)
            throws ServiceException {
        try {
            return serverDAO.findByApp(application.getId());
        } catch (PersistenceException e) {
            throw new ServiceException("Error database : "
                    + e.getLocalizedMessage(), e);
        }
    }

    @Override
    public Server findByContainerID(String id)
            throws ServiceException {
        try {
            return serverDAO.findByContainerID(id);
        } catch (PersistenceException e) {
            throw new ServiceException("Error database : "
                    + e.getLocalizedMessage(), e);
        }
    }

    @Override
    @Transactional
    public Server update(Server server, String jvmMemory, String jvmOptions,
                         String jvmRelease, boolean restorePreviousEnv)
            throws ServiceException {

        Map<String, String> configShell = new HashMap<>();
        configShell.put("port", server.getSshPort());
        configShell.put("dockerManagerAddress", server.getApplication().getManagerIp());
        // We don't need to set userLogin because shell script caller must be root.
        configShell.put("password", server.getApplication().getUser().getPassword());
        // Protection without double slashes into jvm options

        jvmOptions = jvmOptions.replaceAll("//", "\\\\/\\\\/");

        String previousJvmOptions = server.getJvmOptions();
        String previousJvmMemory = server.getJvmMemory().toString();
        String previousJvmRelease = server.getJvmRelease();

        try {

            // If jvm memory or options changes...
            if (!jvmMemory.equalsIgnoreCase(server.getJvmMemory().toString())
                    || !jvmOptions.equalsIgnoreCase(server.getJvmOptions())) {
                // Changement configuration MEMOIRE + OPTIONS

                String command = "bash /cloudunit/appconf/scripts/change-server-config.sh "
                        + jvmMemory + " " + "\"" + jvmOptions + "\"";


                if (server.getImage().getName().contains("jar")) {
                    command = "bash /cloudunit/scripts/change-server-config.sh "
                            + jvmMemory + " " + "\"" + jvmOptions + "\"";
                }
                logger.info("command shell to execute [" + command + "]");
                int status = shellUtils.executeShell(command, configShell);
            }

            // If jvm release changes...
            if (!jvmRelease.equalsIgnoreCase(server.getJvmRelease())) {
                changeJavaVersion(server.getApplication(), jvmRelease);
            }

            server.setJvmMemory(Long.valueOf(jvmMemory));
            server.setJvmOptions(jvmOptions);
            server.setJvmRelease(jvmRelease);
            server = saveInDB(server);

        } catch (Exception e) {
            // Exception would be one RuntimeException coming from shell error
            // If second call and no way to start gracefully tomcat, we need to stop application
            if (!restorePreviousEnv) {
                StringBuilder msgError = new StringBuilder();
                msgError.append("jvmMemory:").append(jvmMemory).append(",");
                msgError.append("jvmOptions:").append(jvmOptions).append(",");
                msgError.append("jvmRelease:").append(jvmRelease);
                throw new ServiceException(msgError.toString(), e);
            } else {
                // Rollback to previous configuration
                logger.warn("Restore the previous environment for jvm configuration. Maybe a syntax error");
                update(server, previousJvmMemory, previousJvmOptions, previousJvmRelease, false);
            }
        }

        return server;

    }

    /**
     * Change the version of the jvm
     *
     * @param application
     * @param javaVersion
     * @throws CheckException
     * @throws ServiceException
     */
    @Override
    public void changeJavaVersion(Application application, String javaVersion)
            throws CheckException, ServiceException {

        logger.info("Starting changing to java version " + javaVersion
                + ", the application " + application.getName());

        Map<String, String> configShell = new HashMap<>();
        String command = null;

        // Servers
        List<Server> listServers = application.getServers();
        for (Server server : listServers) {
            try {
                configShell.put("password", server.getApplication().getUser().getPassword());
                configShell.put("port", server.getSshPort());
                configShell.put("dockerManagerAddress", application.getManagerIp());

                // Need to be root for shell call because we modify /etc/environme,t
                command = "bash /cloudunit/scripts/change-java-version.sh " + javaVersion;
                logger.info("command shell to execute [" + command + "]");
                shellUtils.executeShell(command, configShell);

            } catch (Exception e) {
                server.setStatus(Status.FAIL);
                saveInDB(server);
                logger.error("java version = " + javaVersion + " - " + application.toString() + " - "
                        + server.toString(), e);
                throw new ServiceException(application + ", javaVersion:" + javaVersion, e);
            }
        }
    }


}
