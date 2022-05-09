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

import static bloodbank.utility.MyConstants.APPLICATION_API_VERSION;
import static bloodbank.utility.MyConstants.DEFAULT_ADMIN_USER;
import static bloodbank.utility.MyConstants.DEFAULT_ADMIN_USER_PASSWORD;
import static bloodbank.utility.MyConstants.DEFAULT_USER_PASSWORD;
import static bloodbank.utility.MyConstants.DEFAULT_USER_PREFIX;
import static bloodbank.utility.MyConstants.PERSON_RESOURCE_NAME;
import static bloodbank.utility.MyConstants.RESOURCE_PATH_ID_PATH;
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
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import bloodbank.entity.Address;
import bloodbank.entity.Person;

/**
 * @File: TestAddressResource.java
 * @Group: Lily An, Donglin Li, Xu Leng, Jianchuan Li
 * @Author: Jianchuan Li 040956867
 * @Date: 2021-04-18 9:23 a.m.
 * @Version: 1.0
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public class TestAddressResource {
	private static final Class<?> _thisClaz = MethodHandles.lookup().lookupClass();
    private static final Logger logger = LogManager.getLogger(_thisClaz);

    static final String APPLICATION_CONTEXT_ROOT = "REST-BloodBank";
    static final String HTTP_SCHEMA = "http";
    static final String HOST = "localhost";
    static final int PORT = 8080;
    static final int ORIGINAL_SIZE = 2;

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
    public void test01_all_addresses_adminrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            //.register(userAuth)
            .register(adminAuth)
            .path("address")
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<Address> adds = response.readEntity(new GenericType<List<Address>>(){});
        assertThat(adds, is(not(empty())));
        assertThat(adds, hasSize(1));
    }
    
    @Test
    public void test02_all_addresses_userrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(userAuth)
            //.register(adminAuth)
            .path("address")
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<Address> adds = response.readEntity(new GenericType<List<Address>>(){});
        assertThat(adds, is(not(empty())));
        assertThat(adds, hasSize(1));
    }
    
    @Test
    public void test03_address_adminrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            //.register(userAuth)
            .register(adminAuth)
            .path("address/1")
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        Address add = response.readEntity(new GenericType<Address>(){});
        assertEquals(add.getId(), 1);
        
    }
    
    @Test
    public void test04_address_userrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            //.register(userAuth)
            .register(userAuth)
            .path("address/1")
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        Address add = response.readEntity(new GenericType<Address>(){});
        assertEquals(add.getId(), 1);
    }
    
    @Test
    public void test05_add_address_adminrole() throws JsonMappingException, JsonProcessingException {
    	Address address = new Address();
		address.setAddress( "1234", "ddd Dr.E", "Halifox", "BS", "CA", "Z9Y8X7W");
        Response response = webTarget
        	.register(adminAuth)
            .path("address")
            //.register(adminAuth)
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json(address));
        assertThat(response.getStatus(), is(200));
        Address add = response.readEntity(new GenericType<Address>(){});
        assertEquals(add.getStreetNumber(), "1234");
               
    }
    
    @Test
    public void test06_add_address_userrole() throws JsonMappingException, JsonProcessingException {
    	Address address = new Address();
		address.setAddress( "1234", "ddd Dr.E", "Halifox", "BS", "CA", "Z9Y8X7W");
        Response response = webTarget
            .register(userAuth)
          //.register(adminAuth)
            .path("address")
            
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json(address));
        assertThat(response.getStatus(), is(403));
    }
    
    @Test
    public void test07_update_address_adminrole() throws JsonMappingException, JsonProcessingException {
    	Address address = new Address();
    	address.setId(1);
		address.setAddress( "1222", "kkk", "Halifox", "BS", "CA", "Z9Y8X7W");
        Response response = webTarget
        	.register(adminAuth)
            //.register(userAuth)
            .path("address/1")
            
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.json(address));
        assertThat(response.getStatus(), is(200));
        Address add = response.readEntity(new GenericType<Address>(){});
        assertEquals(add.getStreetNumber(), "1222");
               
    }
    
    @Test
    public void test08_update_address_userrole() throws JsonMappingException, JsonProcessingException {
    	Address address = new Address();
		address.setAddress( "1222", "kkk", "Halifox", "BS", "CA", "Z9Y8X7W");
        Response response = webTarget
        	//.register(adminAuth)
            .register(userAuth)
            .path("address/1")
            
            .request(MediaType.APPLICATION_JSON)
            .put(Entity.json(address));
        assertThat(response.getStatus(), is(403));
    }
    
    @Test
    public void test09_delete_address_adminrole() throws JsonMappingException, JsonProcessingException {
    	
        Response response = webTarget
        	.register(adminAuth)
            //.register(userAuth)
            .path("address/1")
            .request()
            .delete();
        assertThat(response.getStatus(), is(200));
        Address add = response.readEntity(new GenericType<Address>(){});
        assertEquals(add.getStreetNumber(), "1222");
               
    }
    
    @Test
    public void test10_delete_address_userrole() throws JsonMappingException, JsonProcessingException {
    	
        Response response = webTarget
        	//.register(adminAuth)
            .register(userAuth)
            .path("address/1")
            .request()
            .delete();
        assertThat(response.getStatus(), is(403));
    }
}