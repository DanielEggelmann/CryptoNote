package de.danieleggelmann.cryptonote.library;

import java.nio.ByteBuffer;

public class NotebookDirectoryEntry {
	
	public static final int MINIMUM_BYTES = (Short.BYTES * 2) + Integer.BYTES;
	
	public short mType;
	public String mName;
	public int mBlockId;
	
	public NotebookDirectoryEntry(short type, String name, int blockId) {
		mType = type; mName = name; mBlockId = blockId;
	}
	
	public static NotebookDirectoryEntry Parse(ByteBuffer buffer) throws OperationFailedException{
		if(buffer.remaining() < Short.BYTES)
			throw new OperationFailedException();
		short type = buffer.getShort();
		if(buffer.remaining() < Short.BYTES)
			throw new OperationFailedException();
		short nameLength = buffer.getShort();
		if(buffer.remaining() < nameLength)
			throw new OperationFailedException();
		byte[] name = new byte[nameLength];
		buffer.get(name);
		if(buffer.remaining() < Integer.BYTES)
			throw new OperationFailedException();
		int blockId = buffer.getInt();
		
		return new NotebookDirectoryEntry(type, new String(name), blockId);
	}
	
	public byte[] Serialize() {
		
		byte[] name = mName.getBytes();
		
		ByteBuffer buffer = ByteBuffer.allocate(MINIMUM_BYTES + name.length);
		buffer.putShort(mType);
		buffer.putShort((short)name.length);
		buffer.put(name);
		buffer.putInt(mBlockId);
		
		return buffer.array();
		
	}

}

