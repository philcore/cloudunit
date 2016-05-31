package fr.treeptik.cloudunit.service;

import com.spotify.docker.client.messages.Container;
import fr.treeptik.cloudunit.exception.CheckException;
import fr.treeptik.cloudunit.exception.ServiceException;
import fr.treeptik.cloudunit.model.User;

import java.util.List;

/**
 * Created by Nicolas MULLER on 03/05/16.
 */
public interface DockerService {

    public void pullContainer(String image) throws CheckException, ServiceException;

    public void runContainer(String containerName, String image, String sharedDir) throws CheckException, ServiceException;

    public void removeContainer(String containerName, boolean force) throws CheckException, ServiceException;

    public String exec(String containerName, String command) throws CheckException, ServiceException;

    public List<Container> list(boolean all) throws CheckException, ServiceException;

    public Boolean isRunning(String containerName) throws CheckException, ServiceException;


}
