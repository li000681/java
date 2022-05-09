package bloodbank.rest.resource;

import bloodbank.ejb.BloodBankService;
import bloodbank.entity.BloodDonation;
import bloodbank.entity.Contact;
import bloodbank.entity.DonationRecord;
import bloodbank.entity.Person;
import bloodbank.entity.Phone;
import bloodbank.entity.SecurityUser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.soteria.WrappingCallerPrincipal;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.security.enterprise.SecurityContext;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static bloodbank.utility.MyConstants.*;

/**
 * @File: DonationRecordResource.java
 * @Group: Lily An, Donglin Li, Xu Leng, Jianchuan Li
 * @Author: Xu Leng 040943886
 * @Date: 2021-04-16 2:53 p.m.
 * @Version: 1.0
 */
@Path("donationRecord")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DonationRecordResource {

    private static final Logger LOG = LogManager.getLogger();

    @EJB
    protected BloodBankService service;

    @Inject
    protected SecurityContext sc;

    @GET
    @RolesAllowed({ADMIN_ROLE})
    public Response getDonationRecord() 
    {
        LOG.debug("retrieving all donation records ...");
        List<DonationRecord> donationRecords = service.getAll(DonationRecord.class, DonationRecord.ALL_RECORDS_QUERY_NAME);
        Response response = Response.ok(donationRecords).build();
        return response;
    }


    @GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response getDonationRecordById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) 
    {
    	LOG.debug( "try to retrieve specific donation record " + id);
    	Response response = null;
        DonationRecord record = null;

        if (sc.isCallerInRole(ADMIN_ROLE)) {
            record = service.getById(DonationRecord.class, DonationRecord.RECORDS_QUERY_ID, id);
            response = Response.status(record == null ? Status.NOT_FOUND : Status.OK).entity(record).build();
        } else if (sc.isCallerInRole(USER_ROLE)) {
            WrappingCallerPrincipal wCallerPrincipal = (WrappingCallerPrincipal) sc.getCallerPrincipal();
            SecurityUser sUser = (SecurityUser) wCallerPrincipal.getWrapped();
            Person person = sUser.getPerson();
            if (person != null) {
                Set<DonationRecord> records = service.getById(Person.class, Person.ID_PERSON_QUERY_NAME, person.getId()).getDonations();
                List<DonationRecord> list = new LinkedList<>(records);
                AtomicBoolean isrecordBelongToPerson = new AtomicBoolean(false);
                list.forEach(re -> {
                    if (re.getId() == id) {
                    	isrecordBelongToPerson.set(true);
                    }
                });
                if (isrecordBelongToPerson.get()) {
                    response = Response.ok(service.getById(DonationRecord.class, DonationRecord.RECORDS_QUERY_ID, id)).build();
                } else {
                    throw new ForbiddenException("User trying to access resource it does not own");
                }
            }
        } else {
            response = Response.status(Status.BAD_REQUEST).build();
        }
        return response;
    }
    
    @PUT
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response updateDonationRecord(@PathParam( RESOURCE_PATH_ID_ELEMENT) int id, DonationRecord newDonationRecord)
    {
    	Response response = null;
    	DonationRecord updatedDonationRecord = service.updateById(DonationRecord.class, id, newDonationRecord, DonationRecord.RECORDS_QUERY_ID);
		response = Response.ok(updatedDonationRecord).build();
		return response;
    }
    
    @DELETE
	@RolesAllowed( { ADMIN_ROLE })
    @Path( "/{id}")
    public Response deleteDonationRecord( @PathParam( RESOURCE_PATH_ID_ELEMENT) int id, DonationRecord newDonationRecord)
    {
    	LOG.debug("delete a donation record");
		Response response = null;
		DonationRecord donationRecord = service.getById( DonationRecord.class, DonationRecord.RECORDS_QUERY_ID,id);
		service.deleteDonationRecordById(id);
		response = Response.ok(donationRecord).build();
		return response;
    }

}
