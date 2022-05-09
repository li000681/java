/**
 * File: PersonResource.java Course materials (21W) CST 8277
 *
 * @author Shariar (Shawn) Emami
 * @author (original) Mike Norman update by : I. Am. A. Student 040nnnnnnn
 */
package bloodbank.rest.resource;

import static bloodbank.utility.MyConstants.ADMIN_ROLE;
import static bloodbank.utility.MyConstants.CUSTOMER_ADDRESS_RESOURCE_PATH;
import static bloodbank.utility.MyConstants.PERSON_RESOURCE_NAME;
import static bloodbank.utility.MyConstants.RESOURCE_PATH_ID_ELEMENT;
import static bloodbank.utility.MyConstants.RESOURCE_PATH_ID_PATH;
import static bloodbank.utility.MyConstants.USER_ROLE;

import java.util.List;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.soteria.WrappingCallerPrincipal;



import bloodbank.ejb.BloodBankService;
import bloodbank.entity.Contact;
import bloodbank.entity.Address;
import bloodbank.entity.SecurityUser;

/**
 * @File: AddressResource.java
 * @Group: Lily An, Donglin Li, Xu Leng, Jianchuan Li
 * @Author: Jianchuan Li 040956867
 * @Date: 2021-04-16 7:46 a.m.
 * @Version: 1.0
 */
@Path( "address")
@Consumes( MediaType.APPLICATION_JSON)
@Produces( MediaType.APPLICATION_JSON)
public class AddressResource {

	private static final Logger LOG = LogManager.getLogger();

	@EJB
	protected BloodBankService service;

	@Inject
	protected SecurityContext sc;

	@GET
	@RolesAllowed( { ADMIN_ROLE, USER_ROLE })
	public Response getAddress() {
		LOG.debug( "retrieving all addresses ...");
		List< Address> addresses = service.getAll(Address.class, Address.ALL_ADDRESSES_QUERY_NAME);
		Response response = Response.ok( addresses).build();
		return response;
	}

	@GET
	@RolesAllowed( { ADMIN_ROLE, USER_ROLE })
	@Path( RESOURCE_PATH_ID_PATH)
	public Response getById( @PathParam( RESOURCE_PATH_ID_ELEMENT) int id) {
		LOG.debug( "try to retrieve specific address " + id);
		 
		Address address = service.getById( Address.class, Address.ADDRESS_QUERY_NAME,id);
		Response response = Response.ok( address).build();
		
		return response;
	}

	@POST
	@RolesAllowed( { ADMIN_ROLE })
	
	public Response addAddress( Address newAddress) {
		Response response = null;
		Address newaddress = service.persist(newAddress);
		
		response = Response.ok( newaddress).build();
		return response;
	}

	@PUT
	@RolesAllowed( { ADMIN_ROLE })
	@Path(RESOURCE_PATH_ID_PATH)
	public Response updateAddress( @PathParam( RESOURCE_PATH_ID_ELEMENT) int id, Address newAddress) {
		Response response = null;
		Address updatedAddress = service.updateById(Address.class, id, newAddress, Address.ADDRESS_QUERY_NAME);
		response = Response.ok( updatedAddress).build();
		return response;
	}
	@DELETE
	@RolesAllowed( { ADMIN_ROLE })
	@Path(RESOURCE_PATH_ID_PATH)
	public Response deleteAddress( @PathParam( RESOURCE_PATH_ID_ELEMENT) int id) {
		LOG.debug("delete a specific Address...");
		Response response = null;
		Address address = service.getById( Address.class, Address.ADDRESS_QUERY_NAME,id);
		service.deleteAddressById( id);
		response = Response.ok(address).build();
		return response;
	}
}