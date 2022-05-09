/**
 * File: PersonResource.java Course materials (21W) CST 8277
 *
 * @author Shariar (Shawn) Emami
 * @author (original) Mike Norman update by : I. Am. A. Student 040nnnnnnn
 */
package bloodbank.rest.resource;

/**
 * @File: PersonResource.java
 * @Group: Lily An, Donglin Li, Xu Leng, Jianchuan Li
 * @Modified: Lily An, Jianchuan Li
 * @Version: 1.0
 */
import static bloodbank.utility.MyConstants.ADMIN_ROLE;
import static bloodbank.utility.MyConstants.CUSTOMER_ADDRESS_RESOURCE_PATH;
import static bloodbank.utility.MyConstants.PERSON_RESOURCE_NAME;
import static bloodbank.utility.MyConstants.RESOURCE_PATH_ID_ELEMENT;
import static bloodbank.utility.MyConstants.RESOURCE_PATH_ID_PATH;
import static bloodbank.utility.MyConstants.USER_ROLE;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response.Status;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.security.enterprise.SecurityContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import bloodbank.entity.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.soteria.WrappingCallerPrincipal;

import bloodbank.ejb.BloodBankService;

@Path( PERSON_RESOURCE_NAME)
@Consumes( MediaType.APPLICATION_JSON)
@Produces( MediaType.APPLICATION_JSON)
public class PersonResource {

	private static final Logger LOG = LogManager.getLogger();

	@EJB
	protected BloodBankService service;

	@Inject
	protected SecurityContext sc;

	// C
	@POST
	@RolesAllowed( { ADMIN_ROLE })
	public Response addPerson( Person newPerson) {
		Response response = null;
		service.buildUserForNewPerson( newPerson);
		response = Response.ok( newPerson).build();
		return response;
	}

	// R
	@GET
    @RolesAllowed({ADMIN_ROLE})
	public Response getPersons() {
		LOG.debug( "retrieving all persons ...");
		List< Person> persons = service.getAll(Person.class, Person.ALL_PERSONS_QUERY_NAME);
		Response response = Response.ok( persons).build();
		return response;
	}

	@GET
	@RolesAllowed( { ADMIN_ROLE, USER_ROLE })
	@Path( RESOURCE_PATH_ID_PATH)
	public Response getPersonById( @PathParam( RESOURCE_PATH_ID_ELEMENT) int id) {
		LOG.debug( "try to retrieve specific person " + id);
		Response response = null;
		Person person = null;

		if ( sc.isCallerInRole( ADMIN_ROLE)) {
			person = service.getById(Person.class, Person.ID_PERSON_QUERY_NAME, id);
			response = Response.status( person == null ? Status.NOT_FOUND : Status.OK).entity( person).build();
		} else if ( sc.isCallerInRole( USER_ROLE)) {
			WrappingCallerPrincipal wCallerPrincipal = (WrappingCallerPrincipal) sc.getCallerPrincipal();
			SecurityUser sUser = (SecurityUser) wCallerPrincipal.getWrapped();
			person = sUser.getPerson();
			if ( person != null && person.getId() == id) {
				response = Response.ok( service.getById(Person.class, Person.ID_PERSON_QUERY_NAME, id)).build();
			} else {
				throw new ForbiddenException( "User trying to access resource it does not own (wrong userid)");
			}
		} else {
			response = Response.status( Status.BAD_REQUEST).build();
		}
		return response;
	}

	// U
	@PUT
	@RolesAllowed({ADMIN_ROLE})
	@Path("/{id}")
	public Response updatePerson(@PathParam("id") int id, Person updatingPerson) {
		Person updatedPerson = service.updateById(Person.class, id, updatingPerson, Person.ID_PERSON_QUERY_NAME);
		return Response.ok(updatedPerson).build();
	}

	@PUT
	@RolesAllowed( { ADMIN_ROLE })
	@Path( CUSTOMER_ADDRESS_RESOURCE_PATH)
	public Response addAddressForPerson( @PathParam( RESOURCE_PATH_ID_ELEMENT) int id, Address newAddress) {
		Response response = null;
		Person updatedPerson = service.setAddressFor( id, newAddress);
		response = Response.ok( updatedPerson).build();
		return response;
	}

	@PUT
	@RolesAllowed( { ADMIN_ROLE })
	@Path( "/{id}/phone")
	public Response addPhoneForPerson( @PathParam( RESOURCE_PATH_ID_ELEMENT) int id, Phone newPhone) {
		Response response = null;
		Person person = service.getById(Person.class, Person.ID_PERSON_QUERY_NAME, id);
		Contact contact = new Contact();
		contact.setOwner(person);
		contact.setPhone(newPhone);
		contact.setContactType("Test");
		person.getContacts().add(contact);
		newPhone.getContacts().add(contact);

		Person updatedPerson = service.updateById(Person.class, id, person, Person.ID_PERSON_QUERY_NAME);
		response = Response.ok( updatedPerson).build();
		return response;
	}
	
	
	@PUT
    @RolesAllowed({ADMIN_ROLE})
	@Path("/{id}/record")
    public Response addDonationRecord(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id , DonationRecord newRecord) {
        Response response = null;
        Person person = service.getById(Person.class, Person.ID_PERSON_QUERY_NAME, id);
        newRecord.setOwner(person);
        DonationRecord record = service.persist(newRecord);
        response = Response.ok(record).build();
        return response;
    }
	
	// D
    @DELETE
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response deletePersonById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        Person person = service.deletePerson(id);
        return Response.ok(person).build();
    }
}