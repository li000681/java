package bloodbank;

import bloodbank.entity.Person;
import bloodbank.entity.Phone;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.junit.jupiter.api.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.List;

import static bloodbank.utility.MyConstants.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @File: TestPhoneResource.java
 * @Group: Lily An, Donglin Li, Xu Leng, Jianchuan Li
 * @Author: Lily An 040973266
 * @Date: 2021-04-18 8:06 a.m.
 * @Version: 1.0
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestPhoneResource {
    private static final Class<?> _thisClaz = MethodHandles.lookup().lookupClass();
    private static final Logger logger = LogManager.getLogger(_thisClaz);

    static final String APPLICATION_CONTEXT_ROOT = "REST-BloodBank";
    static final String HTTP_SCHEMA = "http";
    static final String HOST = "localhost";
    static final int PORT = 8080;
    static final int ORIGINAL_SIZE = 2;
    static final int OK = 200;
    static final int NOT_FOUND = 404;
    static final int UNAUTHORIZED = 401;
    static final int FORBIDDEN = 403;

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
    
    // Create a new phone
    @Order(1)
    @Test
    public void testCreatePhone_adminrole() throws JsonMappingException, JsonProcessingException {
    	Phone phone = new Phone();
    	phone.setNumber("2", "222", "2222222");
        Response response = webTarget
                .register(adminAuth)
                .path("phone")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(phone));
        assertThat(response.getStatus(), is(OK));
        Phone phoneBack = response.readEntity(new GenericType<Phone>(){});
        assertEquals(3, phoneBack.getId());
        assertEquals("2", phoneBack.getCountryCode());
        assertEquals("222", phoneBack.getAreaCode());
        assertEquals("2222222", phoneBack.getNumber());
    }
    
    // Add a new phone to normal user
    @Order(2)
    @Test
    public void testAddNewPhoneForPerson_adminrole() throws JsonMappingException, JsonProcessingException {
    	Phone phone = new Phone();
    	phone.setNumber("3", "333", "3333333");
        Response response = webTarget
                .register(adminAuth)
                .path("person/1/phone")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(phone));
        assertThat(response.getStatus(), is(OK));
        Person personBack = response.readEntity(new GenericType<Person>(){});
        assertEquals("Shawn", personBack.getFirstName());
    }

    // Try to read all phones in admin role, should get a collection that contains 4 phones
    @Order(3)
    @Test
    public void testReadAllPhones_adminrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
                .register(adminAuth)
                .path("phone")
                .request()
                .get();
        assertThat(response.getStatus(), is(OK));
        List<Phone> phones = response.readEntity(new GenericType<List<Phone>>(){});
        assertThat(phones, is(not(empty())));
        assertThat(phones, hasSize(ORIGINAL_SIZE + 2));
    }
    
    // User name and password are correct, but user role is not allowed to read all phones, so the request feedback is forbidden
    @Order(4)
    @Test
    public void testReadAllPhones_userrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
                .register(userAuth)
                .path("phone")
                .request()
                .get();
        assertThat(response.getStatus(), is(FORBIDDEN));
    }
    
    // Admin role can read each phone through its ID
    @Order(5)
    @Test
    public void testReadPhoneById_adminrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
                .register(adminAuth)
                .path("phone/3")
                .request()
                .get();
        assertThat(response.getStatus(), is(OK));
        Phone phone = response.readEntity(new GenericType<Phone>(){});
        assertEquals(3, phone.getId());
        assertEquals("2", phone.getCountryCode());
        assertEquals("222", phone.getAreaCode());
        assertEquals("2222222", phone.getNumber());
    }
    
    // User can only read info of self owned phone, so new user can't get phone2's information, but can get phone3's information
    @Order(6)
    @Test
    public void testReadPhoneById_userrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
                .register(userAuth)
                .path("phone/3")
                .request()
                .get();
        
        Response response2 = webTarget
                .register(userAuth)
                .path("phone/4")
                .request()
                .get();
        
        assertThat(response.getStatus(), is(FORBIDDEN));
        assertThat(response2.getStatus(), is(OK));
        
        Phone phone = response2.readEntity(new GenericType<Phone>(){});
        assertEquals(4, phone.getId());
        assertEquals("3", phone.getCountryCode());
        assertEquals("333", phone.getAreaCode());
        assertEquals("3333333", phone.getNumber());
    }
    
    // admin role can update any phone's info
    @Order(7)
    @Test
    public void testUpdatePhoneById_adminrole() throws JsonMappingException, JsonProcessingException {
    	Phone updatingPhone = new Phone();
    	updatingPhone.setId(3);
    	updatingPhone.setNumber("8", "888", "8888888");
        Response response = webTarget
                .register(adminAuth)
                .path("phone/3")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(updatingPhone));
        assertThat(response.getStatus(), is(OK));
        Phone phone = response.readEntity(new GenericType<Phone>(){});
        assertEquals(3, phone.getId());
        assertEquals("8", phone.getCountryCode());
        assertEquals("888", phone.getAreaCode());
        assertEquals("8888888", phone.getNumber());
    }
    
    // User role can only update the phone he owned
    @Order(8)
    @Test
    public void testUpdatePhoneById_userrole() throws JsonMappingException, JsonProcessingException {
    	Phone updatingPhone = new Phone();
    	updatingPhone.setId(4);
    	updatingPhone.setNumber("4", "444", "4444444");
        Response response = webTarget
                .register(userAuth)
                .path("phone/3")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(updatingPhone));
        
        Response response2 = webTarget
                .register(userAuth)
                .path("phone/4")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(updatingPhone));
        
        assertThat(response.getStatus(), is(FORBIDDEN));
        assertThat(response2.getStatus(), is(OK));
        
        Phone phone = response2.readEntity(new GenericType<Phone>(){});
        assertEquals(4, phone.getId());
        assertEquals("4", phone.getCountryCode());
        assertEquals("444", phone.getAreaCode());
        assertEquals("4444444", phone.getNumber());
    }
    
    // Only admin role can delete phone
    @Order(9)
    @Test
    public void testDeletePhone_adminrole() throws JsonMappingException, JsonProcessingException {
    	
        Response response = webTarget
                .register(adminAuth)
                .path("phone/4")
                .request()
                .delete();
                
        assertThat(response.getStatus(), is(OK));
        
        Phone phone = response.readEntity(new GenericType<Phone>(){});
        assertEquals(4, phone.getId());
        assertEquals("4", phone.getCountryCode());
        assertEquals("444", phone.getAreaCode());
        assertEquals("4444444", phone.getNumber());
    }
    
    // User role can not delete phone
    @Order(10)
    @Test
    public void testDeletePhone_userrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
                .register(userAuth)
                .path("phone/3")
                .request()
                .delete();
                
        assertThat(response.getStatus(), is(FORBIDDEN));
    }
}
