package fr.treeptik.cloudunit.service.impl;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificates;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.*;
import fr.treeptik.cloudunit.exception.ServiceException;
import fr.treeptik.cloudunit.model.User;
import fr.treeptik.cloudunit.service.DockerService;
import fr.treeptik.cloudunit.service.JenkinsService;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is a wrapper to docker spotify api to purpose main functions for CloudUnit Business
 */
@Service
public class DockerServiceImpl implements DockerService {

    private final Logger logger = LoggerFactory.getLogger(DockerServiceImpl.class);

    @Value("${docker.manager.ip:192.168.50.4:2376}")
    private String dockerManagerIp;

    @Value("${certs.dir.path}")
    private String certsDirPath;

    @Value("${docker.endpoint.mode}")
    private String dockerEndpointMode;

    private boolean isHttpMode;

    @PostConstruct
    public void initDockerEndPointMode() {
        if ("http".equalsIgnoreCase(dockerEndpointMode)) {
            logger.warn("Docker TLS mode is disabled");
            isHttpMode = true;
        } else {
            isHttpMode = false;
        }
    }

    @Override
    public void runContainer(String containerName, String image, String sharedDir) {
        DockerClient dockerClient = null;
        try {
            // Bind container ports to host ports
            final String[] ports = {"8080", "22"};
            final Map<String, List<PortBinding>> portBindings = new HashMap<String, List<PortBinding>>();
            for (String port : ports) {
                List<PortBinding> hostPorts = new ArrayList<PortBinding>();
                hostPorts.add(PortBinding.of("0.0.0.0", port));
                portBindings.put(port, hostPorts);
            }

            final HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

            // Create container with exposed ports
            final ContainerConfig containerConfig = ContainerConfig.builder()
                    .hostConfig(hostConfig)
                    .image(image).exposedPorts(ports)
                    .build();

            final ContainerCreation creation = dockerClient.createContainer(containerConfig);
            final String id = creation.id();

            // Inspect container
            final ContainerInfo info = dockerClient.inspectContainer(id);

            // Start container
            dockerClient.startContainer(id);

        } catch (Exception e) {
            logger.error(containerName, e);
        } finally {
            if (dockerClient != null) { dockerClient.close(); }
        }
    }

    /**
     * Execute a shell conmmad into a container. Return the output as String
     *
     * @param containerName
     * @param command
     * @return
     */
    @Override
    public String exec(String containerName, String command) {
        DockerClient dockerClient = null;
        String execOutput = null;
        try {
            dockerClient = getDockerClient();
            final String[] commands = {"bash", "-c", command};
            String execId = dockerClient.execCreate(containerName, commands,
                    DockerClient.ExecCreateParam.attachStdout(),
                    DockerClient.ExecCreateParam.attachStderr());
            final LogStream output = dockerClient.execStart(execId);
            execOutput = output.readFully();
            if (output != null) {
                output.close();
            }
        } catch (InterruptedException | DockerException e) {
            StringBuilder msgError = new StringBuilder();
            msgError.append("containerName:[").append(containerName).append("]");
            msgError.append(", command:[").append(command).append("]");
            logger.error(msgError.toString(), e);
        } finally {
            if (dockerClient != null) { dockerClient.close(); }
        }
        return execOutput;
    }

    /**
     * Return an instance of Spotify DockerClient
     *
     * @return
     */
    private DockerClient getDockerClient() {
        DockerClient dockerClient = null;
        try {
            if (isHttpMode) {
                dockerClient = DefaultDockerClient
                        .builder()
                        .uri("http://" + dockerManagerIp).build();
            } else {
                final DockerCertificates certs = new DockerCertificates(Paths.get(certsDirPath));
                dockerClient = DefaultDockerClient
                        .builder()
                        .uri("https://" + dockerManagerIp).dockerCertificates(certs).build();
            }
        } catch (Exception e) {
            logger.error("cannot instance docker client : ", e);
        }
        return dockerClient;
    }
}
