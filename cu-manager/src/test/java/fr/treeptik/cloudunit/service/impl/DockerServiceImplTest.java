package fr.treeptik.cloudunit.service.impl;

import fr.treeptik.cloudunit.initializer.CloudUnitApplicationContext;
import fr.treeptik.cloudunit.service.DockerService;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.*;

/**
 * Created by nicolas on 24/05/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {CloudUnitApplicationContext.class})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ActiveProfiles("integration")
public class DockerServiceImplTest {

    @Autowired
    private DockerService dockerService;

    @Test(expected = fr.treeptik.cloudunit.exception.CheckException.class)
    public void pullWrongImage() throws Exception {
        dockerService.pullContainer("XXXXXXXXXXX");
    }

    @Test
    public void pullContainer() throws Exception {
        dockerService.pullContainer("tomcat:8.0");
    }

    @Test
    public void runContainer() throws Exception {
        dockerService.runContainer("felix", "tomcat:8.0", null);
    }

    @Test
    public void exec() throws Exception {

    }

}