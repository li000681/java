package bloodbank;

import static org.junit.jupiter.api.Assertions.*;

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
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import bloodbank.entity.BloodBank;

/**
 * @File: TestBloodBankResource.java
 * @Group: Lily An, Donglin Li, Xu Leng, Jianchuan Li
 * @Author: Donglin Li 040938007
 * @Date: 2021-04-19 11:02 a.m.
 * @Version: 1.0
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
class TestBloodBankResource {
	
    private static final Class<?> _thisClaz = MethodHandles.lookup().lookupClass();
    private static final Logger logger = LogManager.getLogger(_thisClaz);

    static final String APPLICATION_CONTEXT_ROOT = "REST-BloodBank";
    static final String HTTP_SCHEMA = "http";
    static final String HOST = "localhost";
    static final int PORT = 8080;
    static final int ORIGINAL_SIZE=2;

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
    public void test01_all_allBloodBank() throws JsonMappingException, JsonProcessingException{
        Response response = webTarget
                //.register(userAuth)
                .register(adminAuth)
                .path("bloodbank")
                .request()
                .get();
        assertThat(response.getStatus(), is(200));
        List<BloodBank> bbs=response.readEntity(new GenericType<List<BloodBank>>() {});
        assertThat(bbs,is(not(empty())));
        assertThat(bbs,hasSize(ORIGINAL_SIZE));
    }
    
    @Test
    public void test02_all_allBloodBank_noAuth() throws JsonMappingException, JsonProcessingException{
        Response response = webTarget
                //.register(userAuth)
                .path("bloodbank")
                .request()
                .get();
        assertThat(response.getStatus(), is(200));
        List<BloodBank> bbs=response.readEntity(new GenericType<List<BloodBank>>() {});
        assertThat(bbs,is(not(empty())));
        assertThat(bbs,hasSize(ORIGINAL_SIZE));
    }
    
    @Test
    public void test03_all_BloodBankByID() throws JsonMappingException, JsonProcessingException{
        Response response = webTarget
                //.register(userAuth)
                .register(adminAuth)
                .path("bloodbank/1")
                .request()
                .get();
        assertThat(response.getStatus(), is(200));
        //List<BloodBank> bbs=response.readEntity(new GenericType<List<BloodBank>>() {});
        BloodBank bb=response.readEntity(new GenericType<BloodBank>(){});
        assertEquals(1, bb.getId());
    }
    
    @Test
    public void test04_all_BloodBankByID_noAuth() throws JsonMappingException, JsonProcessingException{
        Response response = webTarget
                //.register(userAuth)
                .path("bloodbank/1")
                .request()
                .get();
        assertThat(response.getStatus(), is(200));
        //List<BloodBank> bbs=response.readEntity(new GenericType<List<BloodBank>>() {});
        BloodBank bb=response.readEntity(new GenericType<BloodBank>(){});
        assertEquals(1, bb.getId());
    }
    
    @Test
    public void test05_create_new_bank() throws JsonMappingException, JsonProcessingException{
    	BloodBank newBank = new BloodBank() {
			private static final long serialVersionUID = 1L;
		};
		newBank.setName("Test Bank");
		
        Response response = webTarget
                .register(adminAuth)
                .path("bloodbank")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(newBank));
        assertThat(response.getStatus(), is(200));
        //List<BloodBank> bbs=response.readEntity(new GenericType<List<BloodBank>>() {});
        BloodBank bb=response.readEntity(new GenericType<BloodBank>(){});
        assertEquals("Test Bank", bb.getName());
        assertEquals(3, bb.getId());
    }
    
    @Test
    public void test06_update_bank() throws JsonMappingException, JsonProcessingException{
    	BloodBank updateBank = new BloodBank() {
			private static final long serialVersionUID = 1L;
		};
		updateBank.setId(3);
		updateBank.setName("Changed Bank");
		
        Response response = webTarget
                .register(adminAuth)
                .path("bloodbank/3")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(updateBank));
        assertThat(response.getStatus(), is(200));
        //List<BloodBank> bbs=response.readEntity(new GenericType<List<BloodBank>>() {});
        BloodBank bb=response.readEntity(new GenericType<BloodBank>(){});
        assertEquals("Changed Bank", bb.getName());
        assertEquals(3, bb.getId());
    }
    
    @Test
    public void test07_delete_bank() throws JsonMappingException, JsonProcessingException{
        Response response = webTarget
                .register(adminAuth)
                .path("bloodbank/3")
                .request()
                .delete();
        assertThat(response.getStatus(), is(200));
        //List<BloodBank> bbs=response.readEntity(new GenericType<List<BloodBank>>() {});
        BloodBank bb=response.readEntity(new GenericType<BloodBank>(){});
        assertEquals("Changed Bank", bb.getName());
        assertEquals(3, bb.getId());
    }
}
