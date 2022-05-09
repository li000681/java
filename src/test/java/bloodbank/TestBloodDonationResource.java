package bloodbank;

import bloodbank.entity.BloodDonation;
import bloodbank.entity.BloodType;
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
import static org.junit.jupiter.api.Assertions.*;

/**
 * @File: TestBloodDonationResource.java
 * @Group: Lily An, Donglin Li, Xu Leng, Jianchuan Li
 * @Author: Lily An 040973266
 * @Date: 2021-04-20 11:16 p.m.
 * @Version: 1.0
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TestBloodDonationResource {

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
    
    @Order(1)
    @Test
    void addBloodDonation_admin() {
    	BloodDonation newDonation = new BloodDonation();
    	newDonation.setMilliliters(111);
    	BloodType type = new BloodType();
    	type.setType("O", "1");
    	newDonation.setBloodType(type);
    	Response response = webTarget
//                .register(userAuth)
                .register(adminAuth)
                .path("bloodDonation/bank/1")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(newDonation));
        assertThat(response.getStatus(), is(200));
        BloodDonation donation = response.readEntity(new GenericType<BloodDonation>(){});
        assertEquals(3, donation.getId());
        assertEquals("O", donation.getBloodType().getBloodGroup());
    }

    @Order(2)
    @Test
    void addBloodDonation_user() {
    	BloodDonation newDonation = new BloodDonation();
    	newDonation.setMilliliters(111);
    	BloodType type = new BloodType();
    	type.setType("O", "1");
    	newDonation.setBloodType(type);
    	Response response = webTarget
                .register(userAuth)
                .path("bloodDonation/bank/1")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(newDonation));
        assertThat(response.getStatus(), is(403));
    }

    @Order(3)
    @Test
    void getAllBloodDonations_noAuth() {
        Response response = webTarget
//                .register(userAuth)
//                .register(adminAuth)
                .path("bloodDonation")
                .request()
                .get();
        assertThat(response.getStatus(), is(200));
        List<BloodDonation> donations = response.readEntity(new GenericType<List<BloodDonation>>(){});
        assertThat(donations, is(not(empty())));
        assertThat(donations, hasSize(3));
    }

    @Order(4)
    @Test
    void getBloodDonationById_noAuth() {
        Response response = webTarget
                .register(userAuth)
                .path("bloodDonation/3")
                .request()
                .get();
        assertThat(response.getStatus(), is(200));
        BloodDonation donation = response.readEntity(new GenericType<BloodDonation>(){});
        assertEquals(3, donation.getId());
        assertEquals(111, donation.getMilliliters());
        
    }

    @Order(5)
    @Test
    void updateBloodDonation_admin() {
    	BloodDonation donation = new BloodDonation();
    	donation.setId(3);
    	BloodType type = new BloodType();
    	type.setType("A", "0");
    	donation.setBloodType(type);
        Response response = webTarget
//                .register(userAuth)
                .register(adminAuth)
                .path("bloodDonation/3/bank/2")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(donation));
        assertThat(response.getStatus(), is(200));
        BloodDonation don = response.readEntity(new GenericType<BloodDonation>(){});
        assertEquals(3, don.getId());
        assertEquals("A", don.getBloodType().getBloodGroup());
    }
    
    @Order(6)
    @Test
    void updateBloodDonation_user() {
    	BloodDonation donation = new BloodDonation();
    	donation.setId(2);
    	BloodType type = new BloodType();
    	type.setType("B", "0");
    	donation.setBloodType(type);
        Response response = webTarget
//                .register(userAuth)
                .register(userAuth)
                .path("bloodDonation/2/bank/1")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(donation));
        assertThat(response.getStatus(), is(403));
    }

    @Order(7)
    @Test
    void deleteBloodDonation_user() {
        Response response = webTarget
                .register(userAuth)
//                .register(adminAuth)
                .path("bloodDonation/3")
                .request()
                .delete();
        assertThat(response.getStatus(), is(403));

    }

    @Order(8)
    @Test
    void deleteBloodDonation_admin() {
        Response response = webTarget
//                .register(userAuth)
                .register(adminAuth)
                .path("bloodDonation/3")
                .request()
                .delete();
        assertThat(response.getStatus(), is(200));
        BloodDonation donation = response.readEntity(new GenericType<BloodDonation>(){});
        assertEquals(3, donation.getId());
        assertEquals("A", donation.getBloodType().getBloodGroup());
        assertEquals(0, donation.getBloodType().getRhd());

    }

}