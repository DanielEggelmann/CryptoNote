package de.danieleggelmann.cryptonote.library;

import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptedBlockFile extends BlockFile {
	
	protected static final char[] MAGIC = {'E', 'D', 'E'};
	protected static final int SALT_LENGTH = 32;
	protected static final int BLOCK_DIRECTORY_POSITION = MAGIC.length + SALT_LENGTH;
	
	protected static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
	protected static final int IV_LENGTH = 16;
	protected static final int ITERATION_COUNT = 11111;
	protected static final int KEY_LENGTH = 256;
	protected static final String SECRET_KEY_FACTORY_ALGORITHM = "PBKDF2WithHmacSHA256";
	
	
	protected SecretKey mSecretKey;
	
	public EncryptedBlockFile(MemoryFile file, String password) throws InvalidBlockFileException {
		super(file, null, 0);
		
		mFile.Load();
		
		if(mFile.GetSize() < MAGIC.length + SALT_LENGTH + Integer.BYTES)
			throw new InvalidBlockFileException();
		
		byte[] magic = mFile.Read(0, MAGIC.length); // check magic
		for(int i = 0; i < MAGIC.length; i++) {
			if((char)magic[i] != MAGIC[i])
				throw new InvalidBlockFileException();
		}
		
		byte[] salt = mFile.Read(MAGIC.length, SALT_LENGTH);
		
		try {
			mSecretKey = GenerateKey(password, salt);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new InvalidBlockFileException();
		}
		
		_ParseBlockDirectory(_ReadBlockAtPosition(BLOCK_DIRECTORY_POSITION));
		
	}
	
	public EncryptedBlockFile(MemoryFile file, Map<Integer, Integer> blockDirectory, int nextId, SecretKey secretKey) {
		super(file, blockDirectory, nextId);
		mSecretKey = secretKey;
	}

	protected byte[] _Decrypt(byte[] data) throws InvalidBlockCryptographyException {
		
		if(data.length == 0)
			return data;
		
		ByteBuffer buffer = ByteBuffer.wrap(data);

        byte[] iv = new byte[IV_LENGTH];
        buffer.get(iv);
        
        byte[] encryptedData = new byte[buffer.remaining()];
        buffer.get(encryptedData);
        
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        Cipher cipher;
        byte[] result;
		try {
			cipher = Cipher.getInstance(CIPHER_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, mSecretKey, ivSpec);
			result = cipher.doFinal(encryptedData);
			return result;
			
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
			e.printStackTrace();
			throw new InvalidBlockCryptographyException();
		}   
	}
	
	protected byte[] _Encrypt(byte[] data) throws InvalidBlockCryptographyException {
		
		if(data.length == 0)
			return data;
		
		byte[] iv = new byte[IV_LENGTH];

        SecureRandom secRandom = new SecureRandom() ;
        secRandom.nextBytes(iv);

        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, mSecretKey, ivSpec, secRandom);

            byte[] encryptedData = cipher.doFinal(data);

            ByteBuffer buffer = ByteBuffer.allocate(iv.length + encryptedData.length);
            buffer.put(iv);
            buffer.put(encryptedData);
            
            return buffer.array();
            
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        	throw new InvalidBlockCryptographyException();
        }
	}
	
	@Override
	protected int _BlockDirectoryLengthChange() {
		int oldSize = mFile.ReadInteger(BLOCK_DIRECTORY_POSITION);
		
		try {
			byte[] encryptedData = _Encrypt(_SerializeBlockDirectory()); // encrypt block directory to determine the new size
			
			return encryptedData.length - oldSize;
		} catch (InvalidBlockCryptographyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
	}
	
	@Override
	protected void _ParseBlockDirectory(byte[] data) throws InvalidBlockFileException {
		try {
			byte[] decryptedData = _Decrypt(data);
			super._ParseBlockDirectory(decryptedData);
		} catch (InvalidBlockCryptographyException e) {
			throw new InvalidBlockFileException();
		}
	}
	
	@Override
	protected void _WriteBlockDirectory() throws InvalidBlockException {		
		byte[] data = _SerializeBlockDirectory();
		
		byte[] encryptedData;
		
		try {
			encryptedData = _Encrypt(data);
		} catch (InvalidBlockCryptographyException e) {
			e.printStackTrace();
			throw new InvalidBlockException();
		}
		
		if(mFile.ReadInteger(BLOCK_DIRECTORY_POSITION) == encryptedData.length) { // no entries were added or removed
			_WriteBlockAtPosition(BLOCK_DIRECTORY_POSITION, encryptedData);
		}
		else {
			_ReplaceBlockAtPosition(BLOCK_DIRECTORY_POSITION, encryptedData);
		}
	}
	
	@Override
	public byte[] ReadBlock(int id) throws InvalidBlockIdException, InvalidBlockException {
		byte[] data = super.ReadBlock(id);
		
		try {
			return _Decrypt(data);
		} catch (InvalidBlockCryptographyException e) {
			throw new InvalidBlockException();
		}
	}
	
	@Override
	public void ReplaceBlock(int id, byte[] data) throws InvalidBlockIdException, InvalidBlockException {
		try {
			byte[] encryptedData = _Encrypt(data);
			super.ReplaceBlock(id, encryptedData);
		} catch (InvalidBlockCryptographyException e) {
			throw new InvalidBlockException();
		}
	}
	
	@Override
	public int NewBlock(byte[] data) throws InvalidBlockException {
		try {
			byte[] encryptedData = _Encrypt(data);
			return super.NewBlock(encryptedData);
		} catch (InvalidBlockCryptographyException e) {
			throw new InvalidBlockException();
		}
	}
	
	public static EncryptedBlockFile Create(MemoryFile file, String password) throws InvalidBlockFileException {
		file.Clear();
		
		ByteBuffer data = ByteBuffer.allocate(MAGIC.length + SALT_LENGTH + Integer.BYTES);
		for(int i = 0; i < MAGIC.length; i++) {
			data.put((byte)MAGIC[i]);
		}
		
		SecureRandom secRandom = new SecureRandom() ;
		byte[] salt = new byte[SALT_LENGTH];
		secRandom.nextBytes(salt);
		data.put(salt);
		
		data.putInt(0);
		file.Append(data.array());
		
		Map<Integer, Integer> blockDirectory = new HashMap<Integer, Integer>();
		
		SecretKey secretKey;
		try {
			secretKey = GenerateKey(password, salt);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new InvalidBlockFileException();
		}
		
		return new EncryptedBlockFile(file, blockDirectory, 0, secretKey);
	}
	
	public static SecretKey GenerateKey(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(SECRET_KEY_FACTORY_ALGORITHM);

        SecretKey key = secretKeyFactory.generateSecret(spec);
        key = new SecretKeySpec(key.getEncoded(), "AES");
        
        return key;
	}

}