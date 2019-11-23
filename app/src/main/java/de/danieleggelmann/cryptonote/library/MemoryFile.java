package de.danieleggelmann.cryptonote.library;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MemoryFile {
	
	private File mFile;
    private ByteBuffer mBuffer;

    public MemoryFile(File file) {

        mFile = file;
        mBuffer = ByteBuffer.allocate(0);
    }

    public void Load() {

        try {
            FileInputStream inputStream = new FileInputStream(mFile);
            byte[] content = new byte[0];
            content = new byte[inputStream.available()];
            inputStream.read(content);
            inputStream.close();

            mBuffer = ByteBuffer.wrap(content);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void Save() {

        try {
            FileOutputStream outputStream = new FileOutputStream(mFile);
            outputStream.write(mBuffer.array());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int GetSize() {

        return mBuffer.limit();
    }

    public byte[] Read(int position, int length) {

        if(position + length > mBuffer.limit()) {
            throw new IndexOutOfBoundsException();
        }

        byte[] result = new byte[length];

        mBuffer.position(position);
        mBuffer.get(result);
        mBuffer.position(0);

        return result;
    }

    public void Write(int position, byte[] content) {

        if(position + content.length > mBuffer.limit()) {
            throw new IndexOutOfBoundsException();
        }

        mBuffer.position(position);
        mBuffer.put(content);
        mBuffer.position(0);
    }

    public void Replace(int position, int length, byte[] content) {

        if(position + length > mBuffer.limit()) {
            throw new IndexOutOfBoundsException();
        }

        byte[] before = new byte[position]; // bytes before the position
        mBuffer.get(before);
        byte[] after = new byte[mBuffer.limit() - (position + length)]; // bytes after the replaced content
        mBuffer.position(position + length);
        mBuffer.get(after);

        mBuffer = ByteBuffer.allocate(before.length + content.length + after.length);
        mBuffer.put(before);
        mBuffer.put(content);
        mBuffer.put(after);
        mBuffer.position(0);
    }

    public int Append(byte[] content) {

        byte[] bufferContent = mBuffer.array();

        mBuffer = ByteBuffer.allocate(bufferContent.length + content.length);
        mBuffer.put(bufferContent);       
        int position = mBuffer.position();       
        mBuffer.put(content);
        mBuffer.position(0);
        
        return position;
    }
    
    public void Remove(int position, int length) {
    	
    	if(position + length > mBuffer.limit()) {
            throw new IndexOutOfBoundsException();
        }
    	
    	byte[] before = new byte[position]; // bytes before the position
        mBuffer.get(before);
        byte[] after = new byte[mBuffer.limit() - (position + length)]; // bytes after the replaced content
        mBuffer.position(position + length);
        mBuffer.get(after);
        
        mBuffer = ByteBuffer.allocate(before.length + after.length);
        mBuffer.put(before);
        mBuffer.put(after);
        mBuffer.position(0);
    }
    
    public int ReadInteger(int position) {
    	if(position + Integer.BYTES > mBuffer.limit())
    		throw new IndexOutOfBoundsException();
    	
    	int value = mBuffer.getInt(position);
    	mBuffer.position(0);
    	
    	return value;
    }
    
    public short ReadShort(int position) {
    	if(position + Short.BYTES > mBuffer.limit())
    		throw new IndexOutOfBoundsException();
    	
    	short value = mBuffer.getShort(position);
    	mBuffer.position(0);
    	
    	return value;
    }
    
    public void Clear() {
    	mBuffer = ByteBuffer.allocate(0);
    }

}
