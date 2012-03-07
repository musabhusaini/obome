package edu.sabanciuniv.dataMining.data;

import java.io.Serializable;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

/**
 * An object that has an identifier.
 * @author Mus'ab Husaini
 */
@MappedSuperclass
public class IdentifiableObject implements Identifiable, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Create UUID from a byte array.
	 * @param uuidBytes Byte representation of the UUID.
	 * @return The UUID object.
	 */
	public static UUID createUuid(byte[] uuidBytes) {
		try {
			ByteBuffer bb = ByteBuffer.wrap(uuidBytes);
			return new UUID(bb.getLong(), bb.getLong());
		} catch (BufferUnderflowException ex) {
			return null;
		}
	}
	
	public static UUID createUuid(String uuidString) {
		UUID uuid = UUID.randomUUID();
		
		if (uuidString.length() == uuid.toString().length() - 4) {
			StringBuilder sb = new StringBuilder();
			sb.append(uuidString.substring(0, 7));
			sb.append("-");
			sb.append(uuidString.substring(8, 11));
			sb.append("-");
			sb.append(uuidString.substring(12, 15));
			sb.append("-");
			sb.append(uuidString.substring(16, 19));
			sb.append("-");
			sb.append(uuidString.substring(20, 31));
			uuidString = sb.toString();
		}
		
		uuid = UUID.fromString(uuidString);
		return uuid;
	}
	
	/**
	 * Gets the UUID in a byte array format.
	 * @param uuid The UUID to convert bytes.
	 * @return The byte array of the UUID.
	 */
	public static byte[] getUuidBytes(UUID uuid) {
		byte[] uuidBytes = new byte[16];
		ByteBuffer bb = ByteBuffer.wrap(uuidBytes);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());
		return uuidBytes;
	}
	
	private byte[] id;

	/**
	 * Creates a new object of type {@link IdentifiableObject}.
	 */
	public IdentifiableObject() {
		this(UUID.randomUUID());
	}
	
	/**
	 * Creates a new object of type {@link IdentifiableObject}.
	 * @param id Identifier of this object.
	 */
	public IdentifiableObject(UUID id) {
		this.setIdentifier(id);
	}
	
	/**
	 * Creates a new object of type {@link IdentifiableObject}.
	 * @param id Identifier of the object.
	 */
	public IdentifiableObject(String id) {
		this.setIdentifier(id);
	}
	
	/**
	 * Sets the identifier for this object.
	 * @param id The identifier to set.
	 */
	public void setIdentifier(String id) {
		this.setIdentifier(createUuid(id));
	}
	
	/**
	 * Sets the identifier for this object.
	 * @param id The identifier to set.
	 */
	public void setIdentifier(UUID id) {
		if (id == null) {
			throw new IllegalArgumentException();
		}
		
		this.id = getUuidBytes(id);
	}

	@Transient
	public UUID getIdentifier() {
		return createUuid(this.id);
	}

	@Id
	@Lob
	@Column(name="uuid", columnDefinition="VARBINARY(16)", length=16, updatable=false, nullable=false)
	public byte[] getId() {
		return this.id;
	}

	public void setId(byte[] id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		return this.id.toString();
	}
}