package bloodbank.rest.resource;

import static bloodbank.utility.MyConstants.RESOURCE_PATH_ID_ELEMENT;
import static bloodbank.utility.MyConstants.RESOURCE_PATH_ID_PATH;
import static bloodbank.utility.MyConstants.USER_ROLE;
import static bloodbank.utility.MyConstants.ADMIN_ROLE;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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

import bloodbank.ejb.BloodBankService;
import bloodbank.entity.BloodBank;
import bloodbank.entity.BloodDonation;
import bloodbank.entity.DonationRecord;

/**
 * @File: BloodDonationResource.java
 * @Group: Lily An, Donglin Li, Xu Leng, Jianchuan Li
 * @Author: Donglin Li 040938007
 * @Date: 2021-04-16 10:15 a.m.
 * @Version: 1.0
 */
@Path( "bloodDonation")
@Consumes( MediaType.APPLICATION_JSON)
@Produces( MediaType.APPLICATION_JSON)
public class BloodDonationResource {
	
	private static final Logger LOG = LogManager.getLogger();
	
	@EJB
	protected BloodBankService service;
	
	@POST
	@RolesAllowed({ADMIN_ROLE})
	@Path( "/bank/{id}")
	public Response addBloodDonationForBank(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id, BloodDonation newDonation) {
		Response response=null;
		BloodBank bloodBank=service.getById(BloodBank.class, BloodBank.ALL_BLOODBANKS_QUERY_BY_ID, id);
		newDonation.setBank(bloodBank);
		BloodDonation donation = service.persist(newDonation);
		response=Response.ok(donation).build();
		return response;
	}
	
	@GET
	public Response getBloodDonations() {
		LOG.debug("retrieving all blood donation...");
		List<BloodDonation> list=service.getAll(BloodDonation.class, BloodDonation.ALL_BLOODDONATIONS_QUERY);
		LOG.trace("found={}", list);
		Response res=Response.ok(list).build();
		return res;
	}
	
	@GET
	@Path( RESOURCE_PATH_ID_PATH)
	public Response getStoreById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
		LOG.debug("TRY TO RETRIEVE BLOODdonation BY ID="+id);
		BloodDonation bld=service.getById(BloodDonation.class, BloodDonation.ALL_BLOODDONATIONS_QUERY_BY_ID, id);
		Response response=Response.ok(bld).build();
		return response;
	}
	
	@RolesAllowed({ADMIN_ROLE})
	@PUT
	@Path( "/{id}/bank/{bid}")
	public Response updateBloodDonationByid(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id, @PathParam("bid") int bid, BloodDonation bd){
		Response res=null;
//		BloodDonation donation = service.getById(BloodDonation.class, BloodDonation.ALL_BLOODDONATIONS_QUERY_BY_ID, id);
		BloodBank bank = service.getById(BloodBank.class, BloodBank.ALL_BLOODBANKS_QUERY_BY_ID, bid);
		bd.setBank(bank);
		if (bank.getDonations() == null) {
			Set<BloodDonation> donations = new HashSet<>();
			donations.add(bd);
			bank.setDonations(donations);
		} else {
			bank.getDonations().add(bd);
		}
		BloodDonation updatedBD=service.updateById(BloodDonation.class, id, bd, BloodDonation.ALL_BLOODDONATIONS_QUERY_BY_ID);
		res=Response.ok(updatedBD).build();
		return res;
	}
	
	@RolesAllowed({ADMIN_ROLE})
	@PUT
	@Path( "/{id}/record/{rid}")
	public Response addRecordForDonation(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id, @PathParam("rid") int rid){
		Response res=null;
		BloodDonation donation = service.getById(BloodDonation.class, BloodDonation.ALL_BLOODDONATIONS_QUERY_BY_ID, id);
		DonationRecord record = service.getById(DonationRecord.class, DonationRecord.RECORDS_QUERY_ID, rid);
		if (donation.getRecord() != null) {
			donation.getRecord().setDonation(null);
		}
		if (record.getDonation() != null) {
			record.getDonation().setRecord(null);
		}
		donation.setRecord(record);
		record.setDonation(donation);
		BloodDonation updatedBD=service.updateById(BloodDonation.class, id, donation, BloodDonation.ALL_BLOODDONATIONS_QUERY_BY_ID);
		res=Response.ok(updatedBD).build();
		return res;
	}
	
	@RolesAllowed({ADMIN_ROLE})
	@DELETE
	@Path( RESOURCE_PATH_ID_PATH)
	public Response deleteBloodDonationById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
		Response res=null;
		BloodDonation deleteBD=service.getById(BloodDonation.class, BloodDonation.ALL_BLOODDONATIONS_QUERY_BY_ID, id);
		service.deleteBloodDonation(id);
		res=Response.ok(deleteBD).build();
		return res;
	}

}
