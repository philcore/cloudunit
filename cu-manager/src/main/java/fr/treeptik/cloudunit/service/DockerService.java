package fr.treeptik.cloudunit.service;

import fr.treeptik.cloudunit.exception.CheckException;
import fr.treeptik.cloudunit.model.User;

/**
 * Created by Nicolas MULLER on 03/05/16.
 */
public interface DockerService {

    public void pullContainer(String image) throws CheckException;

    public void runContainer(String containerName, String image, String sharedDir);

    public String exec(String containerName, String command);

}
