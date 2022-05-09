package bloodbank;

import bloodbank.entity.DonationRecord;
import bloodbank.entity.Person;
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
 * @File: DonationRecordResourceTest.java
 * @Group: Lily An, Donglin Li, Xu Leng, Jianchuan Li
 * @Author: Xu Leng 040943886
 * @Date: 2021-04-20 3:42 p.m.
 * @Version: 1.0
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DonationRecordResourceTest {

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
    void addDonationRecord_admin() {
    	DonationRecord newRecord = new DonationRecord();
    	newRecord.setTested(false);
        Response response = webTarget
//                .register(userAuth)
                .register(adminAuth)
                .path("person/1/record")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(newRecord));
        assertThat(response.getStatus(), is(200));
        DonationRecord record = response.readEntity(new GenericType<DonationRecord>(){});
        assertEquals(1, record.getOwner().getId());
    }

    @Order(2)
    @Test
    void addDonationRecord_user() {
    	DonationRecord newRecord = new DonationRecord();
    	newRecord.setTested(false);
        Response response = webTarget
                .register(userAuth)
//                .register(adminAuth)
                .path("person/1/record")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(newRecord));
        assertThat(response.getStatus(), is(403));
    }

    @Order(3)
    @Test
    void getAllDonationRecords_adminrole() {
        Response response = webTarget
//                .register(userAuth)
                .register(adminAuth)
                .path("donationRecord")
                .request()
                .get();
        assertThat(response.getStatus(), is(200));
        List<DonationRecord> records = response.readEntity(new GenericType<List<DonationRecord>>(){});
        assertThat(records, is(not(empty())));
        assertThat(records, hasSize(1));
    }

    @Order(4)
    @Test
    void getAllDonationRecords_userrole() {
        Response response = webTarget
                .register(userAuth)
//                .register(adminAuth)
                .path("donationRecord")
                .request()
                .get();
        assertThat(response.getStatus(), is(403));
    }

    @Order(5)
    @Test
    void getDonationRecordById_userrole() {
        Response response = webTarget
                .register(userAuth)
                .path("donationRecord/2")
                .request()
                .get();
        assertThat(response.getStatus(), is(200));
        DonationRecord record = response.readEntity(new GenericType<DonationRecord>(){});
        assertEquals(1, record.getOwner().getId());
        assertEquals(0, record.getTested());
        
    }

    @Order(6)
    @Test
    void updateDonationRecord_admin() {
    	DonationRecord newRecord = new DonationRecord();
    	newRecord.setId(2);
    	Person person = new Person();
    	person.setFullName("test", "test");
    	newRecord.setOwner(person);
    	newRecord.setTested(true);
        Response response = webTarget
//                .register(userAuth)
                .register(adminAuth)
                .path("donationRecord/2")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(newRecord));
        assertThat(response.getStatus(), is(200));
        DonationRecord record = response.readEntity(new GenericType<DonationRecord>(){});
        assertEquals(2, record.getId());
        assertEquals(1, record.getVersion());
        assertEquals(1, record.getTested());
    }
    
    @Order(7)
    @Test
    void updateDonationRecord_user() {
    	DonationRecord newRecord = new DonationRecord();
    	newRecord.setId(2);
    	Person person = new Person();
    	person.setFullName("sec", "sec");
    	newRecord.setOwner(person);
    	newRecord.setTested(false);
        Response response = webTarget
                .register(userAuth)
//                .register(adminAuth)
                .path("donationRecord/2")
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(newRecord));
        assertThat(response.getStatus(), is(403));

    }

    @Order(8)
    @Test
    void deleteDonationRecord_User() {
        Response response = webTarget
                .register(userAuth)
//                .register(adminAuth)
                .path("donationRecord/2")
                .request()
                .delete();
        assertThat(response.getStatus(), is(403));

    }

    @Order(9)
    @Test
    void deleteDonationRecord_Admin() {
        Response response = webTarget
//                .register(userAuth)
                .register(adminAuth)
                .path("donationRecord/2")
                .request()
                .delete();
        assertThat(response.getStatus(), is(200));
        DonationRecord donation = response.readEntity(new GenericType<DonationRecord>(){});
        assertEquals(2, donation.getId());
        assertEquals(1, donation.getVersion());
        assertEquals(1, donation.getTested());

    }

}