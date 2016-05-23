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

import fr.treeptik.cloudunit.dto.HttpOk;
import fr.treeptik.cloudunit.dto.JsonInput;
import fr.treeptik.cloudunit.dto.JsonResponse;
import fr.treeptik.cloudunit.exception.CheckException;
import fr.treeptik.cloudunit.exception.ServiceException;
import fr.treeptik.cloudunit.model.Application;
import fr.treeptik.cloudunit.model.Server;
import fr.treeptik.cloudunit.model.Status;
import fr.treeptik.cloudunit.model.User;
import fr.treeptik.cloudunit.service.ApplicationService;
import fr.treeptik.cloudunit.service.ServerService;
import fr.treeptik.cloudunit.utils.AuthentificationUtils;
import fr.treeptik.cloudunit.utils.CheckUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Locale;

@Controller
@RequestMapping("/server")
public class ServerController
        implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Locale locale = Locale.ENGLISH;

    private Logger logger = LoggerFactory.getLogger(ServerController.class);

    @Inject
    private ApplicationService applicationService;

    @Inject
    private ServerService serverService;

    @Inject
    private AuthentificationUtils authentificationUtils;

    /**
     * Set the JVM Options and Memory
     *
     * @param input
     * @return
     * @throws ServiceException
     * @throws CheckException
     */
    @RequestMapping(value = "/configuration/jvm", method = RequestMethod.PUT)
    @ResponseBody
    public JsonResponse setOptionsJVM(@RequestBody JsonInput input)
            throws ServiceException, CheckException {

        if (logger.isDebugEnabled()) {
            logger.debug("" + input);
        }

        User user = authentificationUtils.getAuthentificatedUser();
        Application application = applicationService.findByNameAndUser(user,
                input.getApplicationName());

        authentificationUtils.canStartNewAction(user, application, locale);

        // Check the input for jvm options
        boolean isApplicatioRunning = application.getStatus().equals(Status.START);
        CheckUtils.checkJavaOpts(input.getJvmOptions(), input.getJvmMemory(), input.getJvmRelease());

        applicationService.setStatus(application, Status.PENDING);

        try {
            for (Server server : application.getServers()) {
                serverService.update(server, input.getJvmMemory(),
                        input.getJvmOptions(), input.getJvmRelease(),
                        false);
            }
        } catch (Exception e) {
            applicationService.setStatus(application, Status.FAIL);
        }

        applicationService.setStatus(application, Status.START);

        return new HttpOk();
    }



}
