package fr.treeptik.cloudunit.service.impl;

import fr.treeptik.cloudunit.initializer.CloudUnitApplicationContext;
import fr.treeptik.cloudunit.model.User;
import fr.treeptik.cloudunit.service.ApplicationService;
import fr.treeptik.cloudunit.service.UserService;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.util.Random;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {CloudUnitApplicationContext.class})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ActiveProfiles("integration")
public class ApplicationServiceImplTest {

    private final Logger logger = LoggerFactory.getLogger(ApplicationServiceImplTest.class);

    public static String applicationName;
    @BeforeClass
    public static void initEnv() {
        applicationName = "app" + new Random().nextInt(100000);
    }

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private UserService userService;

    @Test(expected = fr.treeptik.cloudunit.exception.CheckException.class)
    public void cannotCreateDoublon() throws Exception {
        User user = userService.findByLogin("johndoe");
        applicationService.create(user, "doublon1", "wildfly-10", "latest");
        applicationService.isValid(user, "doublon1", "wildfly-10");
    }

    @Test(expected = fr.treeptik.cloudunit.exception.CheckException.class)
    public void isValidButNot() throws Exception {
        User user = userService.findByLogin("johndoe");
        applicationService.create(user, "doublon2", "wildfly-10", "latest");
        applicationService.create(user, "doublon2", "wildfly-10", "latest");
    }

    @Test(expected = fr.treeptik.cloudunit.exception.ServiceException.class)
    public void wrongImage() throws Exception {
        User user = userService.findByLogin("johndoe");
        applicationService.create(user, "demo1", "XXX", "latest");
    }

    @Test()
    public void createApp() throws Exception {
        User user = userService.findByLogin("johndoe");
        applicationService.isValid(user, applicationName, "wildfly-10");
        applicationService.create(user, applicationName, "wildfly-10", "latest");
    }

}