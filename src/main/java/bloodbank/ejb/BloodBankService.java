/**
 * File: RecordService.java
 * Course materials (21W) CST 8277
 *
 * @author Shariar (Shawn) Emami
 * @author (original) Mike Norman
 *
 * update by : I. Am. A. Student 040nnnnnnn
 *
 */
package bloodbank.ejb;

import bloodbank.entity.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.security.enterprise.identitystore.Pbkdf2PasswordHash;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;

import java.io.Serializable;
import java.util.*;

import static bloodbank.entity.SecurityRole.ROLE_BY_NAME_QUERY;
import static bloodbank.entity.SecurityUser.USER_FOR_OWNING_PERSON_QUERY;
import static bloodbank.utility.MyConstants.*;


/**
 * @File: BloodBankService.java
 * @Group: Lily An, Donglin Li, Xu Leng, Jianchuan Li
 * @Modified: Lily An, Donglin Li, Xu Leng, Jianchuan Li
 * @Version: 1.0
 */
@Singleton
public class BloodBankService implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LogManager.getLogger();

    @PersistenceContext(name = PU_NAME)
    protected EntityManager em;
    @Inject
    protected Pbkdf2PasswordHash pbAndjPasswordHash;

    public <T> List<T> getAll(Class<T> entity, String namedQuery) {
    	TypedQuery<T> allStoresQuery = em.createNamedQuery(namedQuery, entity);
        return allStoresQuery.getResultList();
	}

    public <T> T getById(Class<T> entity, String namedQuery, int id) {
    	TypedQuery<T> allStoresQuery = em.createNamedQuery(namedQuery, entity);
    	allStoresQuery.setParameter(PARAM1, id);
    	List<T> list = allStoresQuery.getResultList();
        return list.size()==0?null:list.get(0);
	}

	@Transactional
	public <T> T persist(T entity) {
        em.persist(entity);
        return entity;
    }

    @Transactional
    public <T> T updateById(Class<T> clazz, int id, T entity, String namedQuery) {
        T updated = (T) getById(clazz, namedQuery, id);
        if (updated != null) {
            em.refresh(updated);
            em.merge(entity);
            em.flush();
        }
        return updated;
    }

	public boolean isDuplicated(BloodBank bank) {
        TypedQuery<Long> allStoresQuery = em.createNamedQuery(BloodBank.IS_DUPLICATE_QUERY_NAME, Long.class);
        allStoresQuery.setParameter(PARAM1, bank.getName());
        return allStoresQuery.getSingleResult() >= 1;
    }

    public List<Phone> getAllPhones() {
        TypedQuery<Phone> allStoresQuery = em.createNamedQuery(Phone.ALL_PHONES_QUERY_NAME, Phone.class);
        return allStoresQuery.getResultList();
    }

    public List<Person> getAllPeople() {
        TypedQuery<Person> allStoresQuery = em.createNamedQuery(Person.ALL_PERSONS_QUERY_NAME, Person.class);
        return allStoresQuery.getResultList();
    }

    public Person getPersonId(int id) {
    	TypedQuery<Person> allStoresQuery = em.createNamedQuery(Person.ID_PERSON_QUERY_NAME, Person.class);
    	allStoresQuery.setParameter(PARAM1, id);
        return allStoresQuery.getSingleResult();
    }

    @Transactional
    public void buildUserForNewPerson(Person newPerson) {
        SecurityUser userForNewPerson = new SecurityUser();
        userForNewPerson.setUsername(
            DEFAULT_USER_PREFIX + "_" + newPerson.getFirstName() + "." + newPerson.getLastName());
        Map<String, String> pbAndjProperties = new HashMap<>();
        pbAndjProperties.put(PROPERTY_ALGORITHM, DEFAULT_PROPERTY_ALGORITHM);
        pbAndjProperties.put(PROPERTY_ITERATIONS, DEFAULT_PROPERTY_ITERATIONS);
        pbAndjProperties.put(PROPERTY_SALTSIZE, DEFAULT_SALT_SIZE);
        pbAndjProperties.put(PROPERTY_KEYSIZE, DEFAULT_KEY_SIZE);
        pbAndjPasswordHash.initialize(pbAndjProperties);
        String pwHash = pbAndjPasswordHash.generate(DEFAULT_USER_PASSWORD.toCharArray());
        userForNewPerson.setPwHash(pwHash);
        userForNewPerson.setPerson(newPerson);
        SecurityRole userRole = em.createNamedQuery(ROLE_BY_NAME_QUERY, SecurityRole.class)
            .setParameter(PARAM1, USER_ROLE).getSingleResult();
        userForNewPerson.getRoles().add(userRole);
        userRole.getUsers().add(userForNewPerson);
        em.persist(userForNewPerson);
    }

    @Transactional
    public Person setAddressFor(int id, Address newAddress) {
    	Person person = getById(Person.class, Person.ID_PERSON_QUERY_NAME, id);
		Contact contact = new Contact();
		contact.setOwner(person);
		contact.setAddress(newAddress);
		contact.setContactType("Test");
		person.getContacts().add(contact);
		newAddress.getContacts().add(contact);
		
		return updateById(Person.class, id, person, Person.ID_PERSON_QUERY_NAME);
    }


    /**
     * to update a person
     *
     * @param id - id of entity to update
     * @param personWithUpdates - entity with updated information
     * @return Entity with updated information
     */
    @Transactional
    public Person updatePersonById(int id, Person personWithUpdates) {
        Person personToBeUpdated = getPersonId(id);
        if (personToBeUpdated != null) {
            em.refresh(personToBeUpdated);
            em.merge(personWithUpdates);
            em.flush();
        }
        return personToBeUpdated;
    }

    /**
     * to delete a person by id
     *
     * @param id - person id to delete
     */
//    @Transactional
//    public Person deletePersonById(int id) {
//        Person person = getPersonId(id);
//        if (person != null) {
//            em.refresh(person);
//            TypedQuery<SecurityUser> findUser = em
//                .createNamedQuery(USER_FOR_OWNING_PERSON_QUERY, SecurityUser.class)
//                .setParameter(PARAM1, person.getId());
//            SecurityUser sUser = findUser.getSingleResult();
//            em.remove(sUser);
//            em.remove(person);
//        }
//        return person;
//    }

    @Transactional
	public Person deletePerson(int id) {
    	Person person = getById(Person.class, Person.ID_PERSON_QUERY_NAME, id);
    	if (person != null) {
    		Set<Contact> contacts = person.getContacts();
            List<Contact> list = new LinkedList<>(contacts);
            list.forEach(contact -> {
            	contacts.remove(contact);
                em.remove(contact);
            });
            Set<DonationRecord> records = person.getDonations();
            List<DonationRecord> recordList = new LinkedList<>(records);
            recordList.forEach(record -> {
            	records.remove(record);
                em.remove(record);
            });
            SecurityUser user = getById(SecurityUser.class, SecurityUser.USER_FOR_OWNING_PERSON_QUERY, person.getId());
            em.remove(user);
            em.remove(person);
		}
        return person;
	}

    @Transactional
    public Phone deletePhone(int id) {
        Phone phone = getById(Phone.class, Phone.ID_PHONE_QUERY_NAME, id);
        if (phone != null) {
        	Set<Contact> contacts = phone.getContacts();
            List<Contact> list = new LinkedList<>(contacts);
            list.forEach(contact -> {
                contact.getOwner().getContacts().remove(contact);
                em.remove(contact);
            });
            em.remove(phone);
		}
        return phone;
    }

    @Transactional
    public Address deleteAddressById(int id) {
        Address address = getById(Address.class, Address.ADDRESS_QUERY_NAME, id);
        if (address != null) {
        	Set<Contact> contacts = address.getContacts();
            List<Contact> list = new LinkedList<>(contacts);
            list.forEach(contact -> {
                contact.getOwner().getContacts().remove(contact);
                em.remove(contact);
            });
            em.remove(address);
		}
        return address;
    }
    
    @Transactional
    public void deleteBloodBank(int id) {
    	BloodBank deleteBloodbank=getById(BloodBank.class,BloodBank.ALL_BLOODBANKS_QUERY_BY_ID,id);
    	if(deleteBloodbank!=null) {
    		Set<BloodDonation> bds=deleteBloodbank.getDonations();
    		List<BloodDonation> list=new LinkedList<>(bds);
    		list.forEach(bd->{
    			if (bd.getRecord() != null) {
    				bd.getRecord().setDonation(null);
//					DonationRecord dRecord = getById(DonationRecord.class, DonationRecord.RECORDS_QUERY_ID, bd.getRecord().getId());
//					dRecord.setDonation(null);
				}
    			em.remove(bd);
    		});
    		em.remove(deleteBloodbank);
    	}
    }
    
    @Transactional
    public void deleteBloodDonation(int id) {
    	BloodDonation deleteBloodDonation=getById(BloodDonation.class, BloodDonation.ALL_BLOODDONATIONS_QUERY_BY_ID, id);
    	if(deleteBloodDonation!=null) {
    		if (deleteBloodDonation.getRecord() != null) {
    			deleteBloodDonation.getRecord().setDonation(null);
			}
    		deleteBloodDonation.getBank().getDonations().remove(deleteBloodDonation);
    		em.remove(deleteBloodDonation);
    	}
    }

    @Transactional
	public void deleteDonationRecordById(int id) {
		DonationRecord deleteRecord = getById(DonationRecord.class, DonationRecord.RECORDS_QUERY_ID, id);
		if (deleteRecord != null) {
			deleteRecord.getOwner().getDonations().remove(deleteRecord);
			if (deleteRecord.getDonation() != null) {
				deleteRecord.getDonation().setRecord(null);
			}
			em.remove(deleteRecord);
		}
	}
    
}