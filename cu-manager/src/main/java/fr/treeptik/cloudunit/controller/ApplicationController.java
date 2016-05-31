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

import fr.treeptik.cloudunit.dto.*;
import fr.treeptik.cloudunit.exception.CheckException;
import fr.treeptik.cloudunit.exception.ServiceException;
import fr.treeptik.cloudunit.model.Application;
import fr.treeptik.cloudunit.model.Status;
import fr.treeptik.cloudunit.model.User;
import fr.treeptik.cloudunit.service.ApplicationService;
import fr.treeptik.cloudunit.utils.AuthentificationUtils;
import fr.treeptik.cloudunit.utils.CheckUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;

/**
 * Controller about Application lifecycle Application is the main concept for CloudUnit : it composed by Server, Module
 * and Metadata
 */
@Controller
@RequestMapping("/application")
public class ApplicationController
        implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Logger logger = LoggerFactory.getLogger(ApplicationController.class);

    @Inject
    private ApplicationService applicationService;

    @Inject
    private AuthentificationUtils authentificationUtils;

    /**
     * To verify if an application exists or not.
     *
     * @param applicationName
     * @param serverName
     * @return
     * @throws ServiceException
     * @throws CheckException
     */
    @ResponseBody
    @RequestMapping(value = "/verify/{applicationName}/{serverName}", method = RequestMethod.GET)
    public JsonResponse isValid(@PathVariable String applicationName, @PathVariable String serverName)
            throws ServiceException, CheckException {

        if (logger.isInfoEnabled()) {
            logger.info("applicationName:" + applicationName);
            logger.info("serverName:" + serverName);
        }

        if (serverName != null) {
            CheckUtils.validateInput(applicationName, "check.app.name");
            CheckUtils.validateInput(serverName, "check.server.name");
            User user = authentificationUtils.getAuthentificatedUser();
            applicationService.isValid(user, applicationName, serverName);
        }
        return new HttpOk();
    }

    /**
     * CREATE AN APPLICATION
     *
     * @param input
     * @return
     * @throws ServiceException
     * @throws CheckException
     * @throws InterruptedException
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public JsonResponse createApplication(@RequestBody JsonInput input)
            throws ServiceException, CheckException, InterruptedException {

        // validate the input
        input.validateCreateApp();

        // We must be sure there is no running action before starting new one
        User user = authentificationUtils.getAuthentificatedUser();
        authentificationUtils.canStartNewAction(user, null, Locale.ENGLISH);

        applicationService.create(user,
                input.getApplicationName(),
                input.getServerName(), "latest");

        return new HttpOk();
    }

    /**
     * START AN APPLICATION
     *
     * @param input {applicatioName:myApp-johndoe-admin}
     * @return
     * @throws ServiceException
     * @throws CheckException
     * @throws InterruptedException
     */
    @ResponseBody
    @RequestMapping(value = "/restart", method = RequestMethod.POST)
    public JsonResponse restartApplication(@RequestBody JsonInput input)
            throws ServiceException, CheckException, InterruptedException {

        // validate the input
        input.validateStartApp();

        String applicationName = input.getApplicationName();
        User user = authentificationUtils.getAuthentificatedUser();
        Application application = applicationService.findByNameAndUser(user, applicationName);

        if (application != null && application.getStatus().equals(Status.PENDING)) {
            // If application is pending do nothing
            return new HttpErrorServer("application is pending. No action allowed.");
        }

        // We must be sure there is no running action before starting new one
        authentificationUtils.canStartNewAction(user, application, Locale.ENGLISH);

        if (application.getStatus().equals(Status.START)) {
            applicationService.stop(application);
            applicationService.start(application);
        } else if (application.getStatus().equals(Status.STOP)) {
            applicationService.start(application);
        }

        return new HttpOk();
    }


    /**
     * START AN APPLICATION
     *
     * @param input {applicatioName:myApp-johndoe-admin}
     * @return
     * @throws ServiceException
     * @throws CheckException
     * @throws InterruptedException
     */
    @ResponseBody
    @RequestMapping(value = "/start", method = RequestMethod.POST)
    public JsonResponse startApplication(@RequestBody JsonInput input)
            throws ServiceException, CheckException, InterruptedException {

        // validate the input
        input.validateStartApp();

        String applicationName = input.getApplicationName();
        User user = authentificationUtils.getAuthentificatedUser();
        Application application = applicationService.findByNameAndUser(user, applicationName);

        if (application != null && application.getStatus().equals(Status.START)) {
            // If appliction is already start, we return the status
            return new HttpErrorServer("application already started");
        }

        // We must be sure there is no running action before starting new one
        authentificationUtils.canStartNewAction(user, application, Locale.ENGLISH);

        applicationService.start(application);

        return new HttpOk();
    }

    /**
     * STOP a running application
     *
     * @param input
     * @return
     * @throws ServiceException
     * @throws CheckException
     */
    @ResponseBody
    @RequestMapping(value = "/stop", method = RequestMethod.POST)
    public JsonResponse stopApplication(@RequestBody JsonInput input)
            throws ServiceException, CheckException {

        if (logger.isDebugEnabled()) {
            logger.debug(input.toString());
        }

        String name = input.getApplicationName();
        User user = authentificationUtils.getAuthentificatedUser();
        Application application = applicationService.findByNameAndUser(user, name);

        // We must be sure there is no running action before starting new one
        authentificationUtils.canStartNewAction(user, application, Locale.ENGLISH);

        // stop the application
        applicationService.stop(application);

        return new HttpOk();
    }

    /**
     * DELETE AN APPLICATION
     *
     * @param jsonInput
     * @return
     * @throws ServiceException
     * @throws CheckException
     */
    @ResponseBody
    @RequestMapping(value = "/{applicationName}", method = RequestMethod.DELETE)
    public JsonResponse deleteApplication(JsonInput jsonInput)
            throws ServiceException, CheckException {

        jsonInput.validateRemoveApp();

        String applicationName = jsonInput.getApplicationName();
        User user = this.authentificationUtils.getAuthentificatedUser();
        Application application = applicationService.findByNameAndUser(user, applicationName);

        // We must be sure there is no running action before starting new one
        authentificationUtils.canStartNewAction(user, application, Locale.ENGLISH);

        try {
            // Application busy
            applicationService.setStatus(application, Status.PENDING);

            logger.info("delete application :" + applicationName);
            applicationService.remove(application, user);

        } catch (ServiceException e) {
            logger.error(application.toString(), e);
            applicationService.setStatus(application, Status.FAIL);
        }

        logger.info("Application " + applicationName + " is deleted.");

        return new HttpOk();
    }

    /**
     * Return detail information about application
     *
     * @return
     * @throws ServiceException
     */
    @ResponseBody
    @RequestMapping(value = "/{applicationName}", method = RequestMethod.GET)
    public Application detail(JsonInput jsonInput)
            throws ServiceException, CheckException {

        jsonInput.validateDetail();

        User user = authentificationUtils.getAuthentificatedUser();
        Application application = applicationService.findByNameAndUser(user, jsonInput.getApplicationName());
        return application;
    }

    /**
     * Return the list of applications for an User
     *
     * @return
     * @throws ServiceException
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.GET)
    public List<Application> findAllByUser()
            throws ServiceException {
        User user = this.authentificationUtils.getAuthentificatedUser();
        List<Application> applications = applicationService.findAllByUser(user);

        logger.debug("Number of applications " + applications.size());
        return applications;
    }

    /**
     * Deploy a web application
     *
     * @return
     * @throws IOException
     * @throws ServiceException
     * @throws CheckException
     */
    @ResponseBody
    @RequestMapping(value = "/{applicationName}/deploy", method = RequestMethod.POST, consumes = {"multipart/form-data"})
    public JsonResponse deploy(@RequestPart("file") MultipartFile fileUpload, @PathVariable String applicationName,
                               HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServiceException, CheckException {

        logger.info("applicationName = " + applicationName + "file = " + fileUpload.getOriginalFilename());

        User user = authentificationUtils.getAuthentificatedUser();
        Application application = applicationService.findByNameAndUser(user, applicationName);

        // We must be sure there is no running action before starting new one
        authentificationUtils.canStartNewAction(user, application, Locale.ENGLISH);

        //applicationService.deploy(fileUpload, application);

        logger.info("--DEPLOY APPLICATION WAR ENDED--");
        return new HttpOk();
    }

}