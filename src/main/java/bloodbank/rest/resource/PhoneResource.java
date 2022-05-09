package bloodbank.rest.resource;

import bloodbank.ejb.BloodBankService;
import bloodbank.entity.Contact;
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
 * @File: PhoneResource.java
 * @Group: Lily An, Donglin Li, Xu Leng, Jianchuan Li
 * @Author: Lily An 040973266
 * @Date: 2021-04-17 9:06 a.m.
 * @Version: 1.0
 */
@Path("phone")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PhoneResource {
    private static final Logger LOG = LogManager.getLogger();

    @EJB
    protected BloodBankService service;

    @Inject
    protected SecurityContext sc;

    // C
    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response addPhone(Phone newPhone) {
        Phone tempPhone = service.persist(newPhone);
        return Response.ok(tempPhone).build();
    }

    // R
    @GET
    @RolesAllowed({ADMIN_ROLE})
    public Response getPhones() {
        LOG.debug("retrieving all phones ...");
        List<Phone> phones = service.getAll(Phone.class, Phone.ALL_PHONES_QUERY_NAME);
        Response response = Response.ok(phones).build();
        return response;
    }

    @GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response getPhoneById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        LOG.debug("try to retrieve specific phone " + id);
        Response response = null;
        Phone phone = null;

        if (sc.isCallerInRole(ADMIN_ROLE)) {
            phone = service.getById(Phone.class, Phone.ID_PHONE_QUERY_NAME, id);
            response = Response.status(phone == null ? Status.NOT_FOUND : Status.OK).entity(phone).build();
        } else if (sc.isCallerInRole(USER_ROLE)) {
            WrappingCallerPrincipal wCallerPrincipal = (WrappingCallerPrincipal) sc.getCallerPrincipal();
            SecurityUser sUser = (SecurityUser) wCallerPrincipal.getWrapped();
            Person person = sUser.getPerson();
            if (person != null) {
                Set<Contact> contacts = service.getById(Person.class, Person.ID_PERSON_QUERY_NAME, person.getId()).getContacts();
                List<Contact> list = new LinkedList<>(contacts);
                AtomicBoolean isPhoneBelongToPerson = new AtomicBoolean(false);
                list.forEach(con -> {
                    if (con.getPhone().getId() == id) {
                        isPhoneBelongToPerson.set(true);
                    }
                });
                if (isPhoneBelongToPerson.get()) {
                    response = Response.ok(service.getById(Phone.class, Phone.ID_PHONE_QUERY_NAME, id)).build();
                } else {
                    throw new ForbiddenException("User trying to access resource it does not own");
                }
            }
        } else {
            response = Response.status(Status.BAD_REQUEST).build();
        }
        return response;
    }

    // U
    @PUT
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    @Path("/{id}")
    public Response updatePhone(@PathParam("id") int id, Phone updatingPhone) {
    	Response response = null;
        Phone phone = null;

        if (sc.isCallerInRole(ADMIN_ROLE) && id == updatingPhone.getId()) {
            phone = service.updateById(Phone.class, id, updatingPhone, Phone.ID_PHONE_QUERY_NAME);
            response = Response.status(phone == null ? Status.NOT_FOUND : Status.OK).entity(phone).build();
        } else if (sc.isCallerInRole(USER_ROLE)) {
            WrappingCallerPrincipal wCallerPrincipal = (WrappingCallerPrincipal) sc.getCallerPrincipal();
            SecurityUser sUser = (SecurityUser) wCallerPrincipal.getWrapped();
            Person person = sUser.getPerson();
            if (person != null) {
                Set<Contact> contacts = service.getById(Person.class, Person.ID_PERSON_QUERY_NAME, person.getId()).getContacts();
                List<Contact> list = new LinkedList<>(contacts);
                AtomicBoolean isPhoneBelongToPerson = new AtomicBoolean(false);
                list.forEach(con -> {
                    if (con.getPhone().getId() == id && id == updatingPhone.getId()) {
                        isPhoneBelongToPerson.set(true);
                    }
                });
                if (isPhoneBelongToPerson.get()) {
                    response = Response.ok(service.updateById(Phone.class, id, updatingPhone, Phone.ID_PHONE_QUERY_NAME)).build();
                } else {
                    throw new ForbiddenException("User trying to access resource it does not own");
                }
            }
        } else {
            response = Response.status(Status.BAD_REQUEST).build();
        }
        return response;
    }

    // D
    @DELETE
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response deletePhoneById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        Phone phone = service.deletePhone(id);
        return Response.ok(phone).build();
    }
}
