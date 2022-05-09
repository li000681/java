package bloodbank.rest.resource;

import static bloodbank.utility.MyConstants.ADMIN_ROLE;
import static bloodbank.utility.MyConstants.PERSON_RESOURCE_NAME;
import static bloodbank.utility.MyConstants.RESOURCE_PATH_ID_PATH;
import static bloodbank.utility.MyConstants.USER_ROLE;
import static bloodbank.utility.MyConstants.RESOURCE_PATH_ID_ELEMENT;
import static bloodbank.utility.MyConstants.BLOODBANK_RESOURCE_NAME;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
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
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bloodbank.ejb.BloodBankService;
import bloodbank.entity.BloodBank;
import bloodbank.entity.BloodDonation;
import bloodbank.entity.Person;

/**
 * @File: BloodBankResource.java
 * @Group: Lily An, Donglin Li, Xu Leng, Jianchuan Li
 * @Author: Donglin Li 040938007
 * @Date: 2021-04-16 10:15 a.m.
 * @Version: 1.0
 */
@Path( "bloodbank")
@Consumes( MediaType.APPLICATION_JSON)
@Produces( MediaType.APPLICATION_JSON)
public class BloodBankResource {
	private static final Logger LOG = LogManager.getLogger();

	@EJB
	protected BloodBankService service;
	
	@GET
	public Response getBloodBanks() {
		LOG.debug("retrieving all blood banks...");
		List<BloodBank> list=service.getAll(BloodBank.class, BloodBank.ALL_BLOODBANKS_QUERY_NAME);
		LOG.trace("found={}", list);
		Response res=Response.ok(list).build();
		return res;
	}
	
	@GET
	@Path( RESOURCE_PATH_ID_PATH)
	public Response getStoreById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
		LOG.debug("TRY TO RETRIEVE BLOODBANK BY ID="+id);
		BloodBank bloodbank=service.getById(BloodBank.class, BloodBank.ALL_BLOODBANKS_QUERY_BY_ID, id);
		Response response=Response.ok(bloodbank).build();
		return response;
	}
	
	@RolesAllowed( { ADMIN_ROLE })
	@POST
	public Response addBloodbank(BloodBank newBloodbank) {
		//Response response=null;
		LOG.debug("TRY TO ADD BLOODBANK");
		if(service.isDuplicated(newBloodbank)) {
			HttpErrorResponse er=new HttpErrorResponse(Status.CONFLICT.getStatusCode(),"entity already exists");
			return Response.status(Status.CONFLICT).entity(er).build();
		}else {
			BloodBank tmpBloodBank=service.persist(newBloodbank);
			return Response.ok(tmpBloodBank).build();
		}
		/*int id=newBloodbank.getId();
		
		newBloodbank=service.buildNewBloodbank(newBloodbank);
		BloodBank getted=service.getBloodbankById(id);
		if(newBloodbank.equals(getted)) {
			response=Response.ok(newBloodbank).build();
		}else {
			throw new ForbiddenException("Add filed");
		}*/
		//return response;
		
	}
	
	@RolesAllowed( { ADMIN_ROLE })
	@PUT
	@Path( RESOURCE_PATH_ID_PATH)
	public Response updateBloodBank(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id, BloodBank bb) {
		Response response=null;
		BloodBank updatedBB=service.updateById(BloodBank.class, id, bb, BloodBank.ALL_BLOODBANKS_QUERY_BY_ID);
		//bb=service.updateById(id, BloodBank.class, BLOODBANK_RESOURCE_NAME)
		response=Response.ok(updatedBB).build();
		return response;
	}
	
	@RolesAllowed( { ADMIN_ROLE })
	@DELETE
	@Path( RESOURCE_PATH_ID_PATH)
	public Response deleteBloodBank(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
		Response response=null;
		BloodBank bb=service.getById(BloodBank.class, BloodBank.ALL_BLOODBANKS_QUERY_BY_ID, id);
		service.deleteBloodBank(id);
		response=Response.ok(bb).build();
		return response;
	}

}
