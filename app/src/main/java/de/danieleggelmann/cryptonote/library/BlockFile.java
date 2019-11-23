package de.danieleggelmann.cryptonote.library;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class BlockFile {
	
	protected static final char[] MAGIC = {'D', 'E'};
	protected static final int BLOCK_DIRECTORY_POSITION = MAGIC.length;
	protected static final int BLOCK_DIRECTORY_ENTRY_SIZE = Integer.BYTES + Integer.BYTES;
	
	protected MemoryFile mFile;
	protected Map<Integer, Integer> mBlockDirectory;
	protected int mNextId;
	
	public BlockFile(MemoryFile file) throws InvalidBlockFileException, InvalidBlockException {
		mFile = file;
		mFile.Load();
		
		if(mFile.GetSize() < MAGIC.length + Integer.BYTES)
			throw new InvalidBlockFileException();
		
		byte[] magic = mFile.Read(0, MAGIC.length); // check magic
		for(int i = 0; i < MAGIC.length; i++) {
			if((char)magic[i] != MAGIC[i])
				throw new InvalidBlockFileException();
		}
		
		mNextId = 0;
		
		_ParseBlockDirectory(_ReadBlockAtPosition(BLOCK_DIRECTORY_POSITION)); // read and parse block directory
	}
	
	protected BlockFile(MemoryFile file, Map<Integer, Integer> blockDirectory, int nextId) {
		mFile = file;
		mBlockDirectory = blockDirectory;
		mNextId = nextId;
	}
	
	protected byte[] _ReadBlockAtPosition(int position) {
		int length = mFile.ReadInteger(position);
		
		byte[] data = mFile.Read(position + Integer.BYTES, length);
		
		return data;
	}
	
	protected void _WriteBlockAtPosition(int position, byte[] data) {
		mFile.Write(position + Integer.BYTES, data); // length does not change and can be skipped
	}
	
	protected void _ReplaceBlockAtPosition(int position, byte[] data) {
		int oldSize = mFile.ReadInteger(position);
		
		ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + data.length);
		buffer.putInt(data.length);
		buffer.put(data);
		
		mFile.Replace(position, oldSize + Integer.BYTES, buffer.array());
	}
	
	protected void _ParseBlockDirectory(byte[] data) throws InvalidBlockFileException {
		ByteBuffer buffer = ByteBuffer.wrap(data);
		
		mBlockDirectory = new HashMap<Integer, Integer>();
		
		while(buffer.remaining() >= BLOCK_DIRECTORY_ENTRY_SIZE) {
			int id = buffer.getInt();
			int position = buffer.getInt();
			
			if(id >= mNextId) {
				mNextId = id + 1;
			}
			
			mBlockDirectory.put(id, position);
		}
	}
	
	protected int _BlockDirectoryLengthChange() {
		int oldSize = mFile.ReadInteger(BLOCK_DIRECTORY_POSITION);
		int newSize = mBlockDirectory.size() * BLOCK_DIRECTORY_ENTRY_SIZE;
		return newSize - oldSize;
	}
	
	protected void _UpdateBlockDirectory() throws InvalidBlockException { // update block directory positions by length
		int length = _BlockDirectoryLengthChange();
		
		for(int id : mBlockDirectory.keySet()) {
			mBlockDirectory.replace(id, mBlockDirectory.get(id) + length);
		}
		
		_WriteBlockDirectory();
	}
	
	protected byte[] _SerializeBlockDirectory() {
		int size = mBlockDirectory.size() * BLOCK_DIRECTORY_ENTRY_SIZE;
		
		ByteBuffer buffer = ByteBuffer.allocate(size);
		
		for(Entry<Integer, Integer> entry : mBlockDirectory.entrySet()) {
			buffer.putInt(entry.getKey());
			buffer.putInt(entry.getValue());
		}
		
		return buffer.array();
	}
	
	protected void _WriteBlockDirectory() throws InvalidBlockException {		
		byte[] data = _SerializeBlockDirectory();
		
		if(mFile.ReadInteger(BLOCK_DIRECTORY_POSITION) == data.length) { // no entries were added or removed
			_WriteBlockAtPosition(BLOCK_DIRECTORY_POSITION, data);
		}
		else {
			_ReplaceBlockAtPosition(BLOCK_DIRECTORY_POSITION, data);
		}
	}
	
	public byte[] ReadBlock(int id) throws InvalidBlockIdException, InvalidBlockException {
		if(!mBlockDirectory.containsKey(id)) {
			throw new InvalidBlockIdException();
		}
		
		return _ReadBlockAtPosition(mBlockDirectory.get(id));
	}
	
	public int NewBlock(byte[] data) throws InvalidBlockException {
		int id = mNextId;
		mNextId++;
		
		ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + data.length);
		buffer.putInt(data.length);
		buffer.put(data);
		
		int position = mFile.Append(buffer.array());
		
		mBlockDirectory.put(id, position);
		
		_UpdateBlockDirectory();
		
		return id;
	}
	
	public void RemoveBlock(int id) throws InvalidBlockIdException, InvalidBlockException {
		if(!mBlockDirectory.containsKey(id)) {
			throw new InvalidBlockIdException();
		}
		
		int position = mBlockDirectory.get(id);
		int length = mFile.ReadInteger(position);
		mFile.Remove(position, length + Integer.BYTES); // account  for length value
		
		for(Entry<Integer, Integer> entry : mBlockDirectory.entrySet()) { // update adjourning entries 
			if(entry.getValue() > position) {
				mBlockDirectory.replace(entry.getKey(), entry.getValue() - length - Integer.BYTES); // account for length value of block
			}
		}
		
		mBlockDirectory.remove(id); // remove id from block directory
		
		_UpdateBlockDirectory(); // account for removed entry of block
	}
	
	public void ReplaceBlock(int id, byte[] data) throws InvalidBlockIdException, InvalidBlockException {
		if(!mBlockDirectory.containsKey(id)) {
			throw new InvalidBlockIdException();
		}
		
		int position = mBlockDirectory.get(id);
		int length = mFile.ReadInteger(position);
		
		if(length == data.length) { // no changes to block directory and other blocks needed
			_WriteBlockAtPosition(position, data);
		}
		else {
			int lengthDifference = data.length - length;
			
			_ReplaceBlockAtPosition(position, data);
			
			for(Entry<Integer, Integer> entry : mBlockDirectory.entrySet()) { // update adjourning entries 
				if(entry.getValue() > position) {
					mBlockDirectory.replace(entry.getKey(), entry.getValue() + lengthDifference);
				}
			}
			
			_WriteBlockDirectory(); // write the updated block directory to the file
		}
	}
	
	public Integer[] GetBlockIds() {
		Set<Integer> idSet = mBlockDirectory.keySet();
		Integer[] blockIds = idSet.toArray(new Integer[idSet.size()]);
		return blockIds;
	}
	
	public int GetId(int index) {
		Iterator<Integer> iterator = mBlockDirectory.keySet().iterator();
		int result = -1;
		
		for(int i = 0; i <= index && iterator.hasNext(); i++) {
			result = iterator.next();
		}
		
		return result;
	}
	
	public int GetBlockCount() {
		return mBlockDirectory.size();
	}
	
	public void Save() {
		mFile.Save();
	}
	
	public static BlockFile Create(MemoryFile file) {
		file.Clear();
		
		ByteBuffer data = ByteBuffer.allocate(MAGIC.length + Integer.BYTES);
		for(int i = 0; i < MAGIC.length; i++) {
			data.put((byte)MAGIC[i]);
		}
		data.putInt(0);
		file.Append(data.array());
		
		Map<Integer, Integer> blockDirectory = new HashMap<Integer, Integer>();
		
		return new BlockFile(file, blockDirectory, 0);
	}

}
