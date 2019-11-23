package de.danieleggelmann.cryptonote;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;

public class Database{

    private static final int ITERATION_COUNT = 11111;  // amount of iterations used to generate key from password
    private static final int SALT_SIZE = 128;  // salt size used to generate key from password
    private static final int KEY_LENGTH = 256;
    private static final int IV_LENGTH = 16;
    private static final String SECRET_KEY_FACTORY_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int ID_LENGTH = 128;
    private static final String NOTEBOOK_NAME = "MAIN";

    private String mFilename;
    private SecretKey mSecretKey;
    private transient Context mContext;
    private Notebook mNotebook;

    private Database(String filename, SecretKey secretKey, Context context) {
        mFilename = filename;
        mSecretKey = secretKey;
        mContext = context;

        if(!(new File(mContext.getFilesDir(), filename)).exists())
        {
            mNotebook = new Notebook(filename, this, NOTEBOOK_NAME, new DatabaseElement[0]);
            mNotebook.Save();
        }
        else
        {
            mNotebook = new Notebook(filename, this);
        }

    }

    public byte[] DecryptFile(String filename)
    {
        File file = new File(mContext.getFilesDir(), filename);

        try {
            FileInputStream inputStream = new FileInputStream(file);
            byte[] content = new byte[inputStream.available()];
            inputStream.read(content);
            inputStream.close();

            ByteBuffer buffer = ByteBuffer.wrap(content);

            byte[] iv = new byte[IV_LENGTH];
            buffer.get(iv);

            content = buffer.array();

            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, mSecretKey, ivSpec);

            return cipher.doFinal(content);

        } catch (NoSuchAlgorithmException | IOException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();

            return null;
        }
    }

    public void EncryptFile(String filename, byte[] content)
    {
        byte[] iv = new byte[IV_LENGTH];

        SecureRandom secRandom = new SecureRandom() ;
        secRandom.nextBytes(iv);

        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, mSecretKey, ivSpec, secRandom);

            byte[] data = cipher.doFinal(content);

            ByteBuffer buffer = ByteBuffer.allocate(iv.length + data.length);
            buffer.put(iv);
            buffer.put(data);

            File file = new File(mContext.getFilesDir(), filename);
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(buffer.array());
            outputStream.close();

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | IOException e) {
            e.printStackTrace();
        }

    }

    public String GetNewId()
    {
        String id;
        File file;

        do {
            id = GenerateRandomId();
            file = new File(mContext.getFilesDir(), id);
        }
        while(file.exists());

        return id;
    }

    public Notebook GetNotebook()
    {
        return mNotebook;
    }

    private static String GenerateRandomId()
    {
        Random r = new Random();

        String result = "";

        for(int i = 0; i < ID_LENGTH; i++)
        {
            result += (char)(r.nextInt(26) + 'a');
        }

        return result;
    }

    private static SecretKey GenerateKey(String password, byte[] salt)  throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(SECRET_KEY_FACTORY_ALGORITHM);

        SecretKey key = secretKeyFactory.generateSecret(spec);

        return key;
    }

    public static Database Create(String filename, String password, Context context) throws NoSuchAlgorithmException, InvalidKeySpecException{
        //SecureRandom secureRandom = new SecureRandom();

        byte[] salt = new byte[SALT_SIZE];
        //secureRandom.nextBytes(salt);

        SecretKey key = GenerateKey(password, salt);

        return new Database(filename, key, context);
    }

    public static Database Open(String filename, String password, Context context) throws InvalidKeySpecException, NoSuchAlgorithmException {
        byte[] salt = new byte[SALT_SIZE];

        SecretKey key = GenerateKey(password, salt);

        return new Database(filename, key, context);
    }
}
