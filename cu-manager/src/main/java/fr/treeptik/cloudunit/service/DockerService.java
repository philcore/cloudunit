package fr.treeptik.cloudunit.service;

import fr.treeptik.cloudunit.model.User;

/**
 * Created by angular5 on 03/05/16.
 */
public interface DockerService {

    public void runContainer(String containerName, String image, String sharedDir);

    public String exec(String containerName, String command);

}
