/**
 * File: RestConfig.java Course materials (21W) CST 8277
 *
 * @author Shariar (Shawn) Emami
 * @date Mar 31, 2021
 * @author Mike Norman
 * @date 2020 10
 */
package bloodbank.rest.serializer;

/**
 * @File: BloodDonationSerializer.java
 * @Group: Lily An, Donglin Li, Xu Leng, Jianchuan Li
 * @Author: Lily An 040973266
 * @Date: 2021-04-19 1:25 p.m.
 * @Version: 1.0
 */
import java.io.IOException;
import java.io.Serializable;
import java.util.Set;

import javax.ejb.EJB;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import bloodbank.ejb.BloodBankService;
import bloodbank.entity.BloodBank;
import bloodbank.entity.BloodDonation;

public class BloodDonationSerializer extends StdSerializer< BloodDonation> implements Serializable {
	private static final long serialVersionUID = 1L;
	
	
	private static final Logger LOG = LogManager.getLogger();
	
    @EJB
    protected BloodBankService service;

	public BloodDonationSerializer() {
		this( null);
	}

	public BloodDonationSerializer( Class< BloodDonation> t) {
		super( t);
	}

	/**
	 * This is to prevent back and forth serialization between Many to Many relations.<br>
	 * This is done by setting the relation to null.
	 */
	@Override
	public void serialize( BloodDonation original, JsonGenerator generator, SerializerProvider provider)
			throws IOException {
		LOG.trace("serializeing={}",original);
		generator.writeStartObject();
		generator.writeNumberField( "id", original.getId());
		String bloodBank = original.getBank()==null?"null":original.getBank().getName();
		generator.writeStringField( "bank", bloodBank);
		generator.writeNumberField( "milliliters", original.getMilliliters());
		generator.writeObjectField( "bloodType", original.getBloodType());
		generator.writeObjectField( "created", original.getCreated());
		generator.writeObjectField( "updated", original.getUpdated());
		generator.writeNumberField( "version", original.getVersion());
		generator.writeEndObject();
	}
}