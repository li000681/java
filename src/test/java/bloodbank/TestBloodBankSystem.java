/**
 * File: OrderSystemTestSuite.java
 * Course materials (20F) CST 8277
 * (Original Author) Mike Norman
 *
 * @date 2020 10
 *
 * (Modified) @author Student Name
 */
package bloodbank;

/**
 * @File: TestBloodBankSystem.java
 * @Group: Lily An, Donglin Li, Xu Leng, Jianchuan Li
 * @Modified: Lily An, Jianchuan Li
 * @Version: 1.0
 */
import static bloodbank.utility.MyConstants.APPLICATION_API_VERSION;
import static bloodbank.utility.MyConstants.DEFAULT_ADMIN_USER;
import static bloodbank.utility.MyConstants.DEFAULT_ADMIN_USER_PASSWORD;
import static bloodbank.utility.MyConstants.DEFAULT_USER_PASSWORD;
import static bloodbank.utility.MyConstants.DEFAULT_USER_PREFIX;
import static bloodbank.utility.MyConstants.PERSON_RESOURCE_NAME;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import bloodbank.entity.Person;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class TestBloodBankSystem {
    private static final Class<?> _thisClaz = MethodHandles.lookup().lookupClass();
    private static final Logger logger = LogManager.getLogger(_thisClaz);

    static final String APPLICATION_CONTEXT_ROOT = "REST-BloodBank";
    static final String HTTP_SCHEMA = "http";
    static final String HOST = "localhost";
    static final int PORT = 8080;

    // test fixture(s)
    static URI uri;
    static HttpAuthenticationFeature adminAuth;
    static HttpAuthenticationFeature userAuth;

    @BeforeAll
    public static void oneTimeSetUp() throws Exception {
        logger.debug("oneTimeSetUp");
        uri = UriBuilder
            .fromUri(APPLICATION_CONTEXT_ROOT + APPLICATION_API_VERSION)
            .scheme(HTTP_SCHEMA)
            .host(HOST)
            .port(PORT)
            .build();
        adminAuth = HttpAuthenticationFeature.basic(DEFAULT_ADMIN_USER, DEFAULT_ADMIN_USER_PASSWORD);
        userAuth = HttpAuthenticationFeature.basic("cst8288", "8288");
    }

    protected WebTarget webTarget;
    @BeforeEach
    public void setUp() {
        Client client = ClientBuilder.newClient(
            new ClientConfig().register(MyObjectMapperProvider.class).register(new LoggingFeature()));
        webTarget = client.target(uri);
    }

    @Test
    public void test01_all_customers_adminrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path(PERSON_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<Person> emps = response.readEntity(new GenericType<List<Person>>(){});
        assertThat(emps, is(not(empty())));
        assertThat(emps, hasSize(1));
    }

    @Test
    public void test02_all_customers_userrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(userAuth)
            .path(PERSON_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(403));
    }

    @Test
    public void test03_add_customer_adminrole() throws JsonMappingException, JsonProcessingException {
    	Person person = new Person();
    	person.setFullName("test", "test");
        Response response = webTarget
            .register(adminAuth)
            .path(PERSON_RESOURCE_NAME)
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json(person));
        assertThat(response.getStatus(), is(200));
        Person emp = response.readEntity(new GenericType<Person>(){});
        assertEquals("test", emp.getFirstName());
        assertEquals("test", emp.getLastName());
        assertEquals(2, emp.getId());
    }
    
    @Test
    public void test04_delete_customer_userrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(userAuth)
            //.register(adminAuth)
            .path("person/2")
            .request()
            .delete();
        assertThat(response.getStatus(), is(403));
    }
    
    @Test
    public void test05_delete_customer_adminrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            //.register(userAuth)
            .register(adminAuth)
            .path("person/2")
            .request()
            .delete();
        assertThat(response.getStatus(), is(200));
        Person emp = response.readEntity(new GenericType<Person>(){});
        assertEquals(emp.getId(), 2);
    }
}