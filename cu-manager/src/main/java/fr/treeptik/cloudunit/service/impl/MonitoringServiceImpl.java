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

import fr.treeptik.cloudunit.exception.ErrorDockerJSONException;
import fr.treeptik.cloudunit.model.Application;
import fr.treeptik.cloudunit.model.Module;
import fr.treeptik.cloudunit.model.Server;
import fr.treeptik.cloudunit.service.ApplicationService;
import fr.treeptik.cloudunit.service.MonitoringService;
import fr.treeptik.cloudunit.utils.ContainerMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by nicolas on 25/08/2014.
 */
@Service
public class MonitoringServiceImpl
        implements MonitoringService {

    // Dictionnaire pour mettre en relation une application avec un ou plusieurs
    // volumes
    private static ConcurrentHashMap<String, String> containerIdByName = new ConcurrentHashMap<>();

    private Logger logger = LoggerFactory
            .getLogger(MonitoringServiceImpl.class);

    @Value("${cadvisor.url}")
    private String cAdvisorURL;

    @Inject
    private ContainerMapper containerMapper;

    @Inject
    private ApplicationService applicationService;
    @Value("${cloudunit.instance.name}")
    private String cuInstanceName;

    public String getFullContainerId(String containerName) {
        return containerIdByName.get(containerName);
    }

    @Override
    public String getJsonFromCAdvisor(String containerId) {
        String result = "";
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet httpget = new HttpGet(cAdvisorURL
                    + "/api/v1.0/containers/docker/" + containerId);
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                result = EntityUtils.toString(response.getEntity());
                if (logger.isDebugEnabled()) {
                    logger.debug(result);
                }
            } finally {
                response.close();
            }
        } catch (Exception e) {
            logger.error(containerId, e);
        }
        return result;
    }

    @Override
    public String getJsonMachineFromCAdvisor() {
        String result = "";
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet httpget = new HttpGet(cAdvisorURL + "/api/v1.0/machine");
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                result = EntityUtils.toString(response.getEntity());
                if (logger.isDebugEnabled()) {
                    logger.debug(result);
                }
            } finally {
                response.close();
            }
        } catch (Exception e) {
            logger.error("" + e);
        }
        return result;
    }

}
