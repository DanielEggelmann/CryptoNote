package de.danieleggelmann.cryptonote.library;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class NotebookDirectory extends NotebookElement {
	
	public static final short TYPE = 1;
	
	protected ArrayList<NotebookElement> mElements;

	public NotebookDirectory(Notebook notebook, int blockId, String name) {
		super(notebook, blockId, name);
	}
	
	public NotebookDirectory(Notebook notebook, int blockId, String name, ArrayList<NotebookElement> elements) {
		super(notebook, blockId, name);
		
		mElements = elements;
	}
	
	protected void _Parse(byte[] data) throws OperationFailedException {
		
		mElements = new ArrayList<NotebookElement>();
		
		ByteBuffer buffer = ByteBuffer.wrap(data);
		
		while(buffer.remaining() >= NotebookDirectoryEntry.MINIMUM_BYTES) {
			NotebookDirectoryEntry entry = NotebookDirectoryEntry.Parse(buffer);
			
			if(entry.mType == NotebookDirectory.TYPE) {
				mElements.add(new NotebookDirectory(mNotebook, entry.mBlockId, entry.mName));
			}
			else {
				mElements.add(new NotebookNote(mNotebook, entry.mBlockId, entry.mName));
			}
		}
	}
	
	protected byte[] _Serialize() {
		byte[][] entries = new byte[mElements.size()][];
		
		int totalLength = 0;
		for(int i = 0; i < mElements.size(); i++) {
			NotebookElement element = mElements.get(i);
			
			NotebookDirectoryEntry entry = new NotebookDirectoryEntry(element.GetType(), element.GetName(), element.GetBlockId());
			entries[i] = entry.Serialize();
			
			totalLength += entries[i].length;
		}
		
		ByteBuffer buffer = ByteBuffer.allocate(totalLength);
		
		for(int i = 0; i < entries.length; i++) {
			buffer.put(entries[i]);
		}
		
		return buffer.array();
	}
	
	protected void _LazyLoad() throws OperationFailedException {
		if(mElements == null) {
			_Parse(mNotebook.ReadBlock(mBlockId));
		}
	}
	
	public int GetElementCount() throws OperationFailedException {
		_LazyLoad();
		
		return mElements.size();
	}
	
	public NotebookElement GetElement(int index) throws OperationFailedException {
		_LazyLoad();
		
		return mElements.get(index);
	}
	
	public void AddElement(NotebookElement element) throws OperationFailedException {
		_LazyLoad();
		
		mElements.add(element);
	}
	
	public void RemoveElement(NotebookElement element) throws OperationFailedException {
		_LazyLoad();
		
		mElements.remove(element);
	}
	
	@Override
	public void Save() throws OperationFailedException {
		_LazyLoad();
		
		byte[] data = _Serialize();
		mNotebook.WriteBlock(mBlockId, data);
	}
	
	@Override
	public void Remove() throws OperationFailedException {
		_LazyLoad();
		
		for(int i = 0; i < mElements.size(); i++) {
			mElements.get(i).Remove();
		}
		mElements.clear();
		
		super.Remove();
	}
	
	@Override
	public short GetType() {
		return TYPE;
	}

}
