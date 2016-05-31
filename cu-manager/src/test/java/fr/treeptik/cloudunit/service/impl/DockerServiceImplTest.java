package fr.treeptik.cloudunit.service.impl;

import com.spotify.docker.client.messages.Container;
import fr.treeptik.cloudunit.initializer.CloudUnitApplicationContext;
import fr.treeptik.cloudunit.service.DockerService;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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

    public static Predicate<Container> isNamed(String name) {
        return p -> p.names().contains("/"+name);
    }

    public static String containerName;
    @BeforeClass
    public static void initEnv() {
        containerName = "felix" + new Random().nextInt(100000);
    }

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
        dockerService.runContainer(containerName, "tomcat:8.0", null);
        List<Container> containerList = dockerService.list(true);
        containerList.stream().forEach(p -> System.out.println(p.names()));
        Assert.assertEquals(1, containerList.stream().filter(isNamed(containerName)).count());
        dockerService.removeContainer(containerName, true);
        containerList = dockerService.list(true);
        Assert.assertEquals(0, containerList.stream().filter(isNamed(containerName)).count());
    }

    @Test
    public void exec() throws Exception {

    }

}