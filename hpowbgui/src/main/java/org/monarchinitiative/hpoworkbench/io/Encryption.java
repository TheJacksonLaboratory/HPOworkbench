package org.monarchinitiative.hpoworkbench.io;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.monarchinitiative.hpoworkbench.exception.CryptoException;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import static org.monarchinitiative.hpoworkbench.gui.PlatformUtil.getPathToSettingsFile;

/**
 * Simple basic encryption to allow users to store their github password in the settings file if desired.
 */
public class Encryption  {
    private static final Logger logger = LogManager.getLogger();
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5";
    /** The encryptiokn does not provide serious security, use only if you are comfortable with
     * storing your github password and username on disk with minimal encryption. */
    private static final String NOT_MUCH_SECURITY_KEY="SuperDuperSecure";
    private final String pathToSettingsFile;

    public Encryption() {
        pathToSettingsFile=getPathToSettingsFile();
    }

    public void decryptSettings() {
        String settings = null;
        try {
            settings=doDecryption();
        } catch (CryptoException ce) {
            ce.printStackTrace();
        }
        if (settings==null) return; // probably means there was no previous file, just skip
        System.out.println(settings);

    }


    public void encryptSettings(String uname, String pword) {
        String settings = String.format("uname:%s\npword:%s",uname,pword);
        try {
            File outputfile = new File(pathToSettingsFile);
            doEncryption(NOT_MUCH_SECURITY_KEY,settings,outputfile);
        } catch (CryptoException ce) {
            ce.printStackTrace();
        }
    }

    private String doDecryption() throws CryptoException {
        try {
            Key secretKey = new SecretKeySpec(NOT_MUCH_SECURITY_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            File inputFile=new File(pathToSettingsFile);
            FileInputStream inputStream = new FileInputStream(inputFile);
            byte[] inputBytes = new byte[(int) inputFile.length()];
            inputStream.read(inputBytes);
            byte[] outputBytes = cipher.doFinal(inputBytes);
            return new String(outputBytes);
        } catch (FileNotFoundException e ) {
            // OK, we will create a new file if the user stores github passwords.
            return null;
        } catch (NoSuchPaddingException | NoSuchAlgorithmException
                | InvalidKeyException | BadPaddingException
                | IllegalBlockSizeException | IOException ex) {
            throw new CryptoException("Error encrypting/decrypting file", ex);
        }
    }



    private static void doEncryption(String key, String inputString,
                                     File outputFile) throws CryptoException {
        try {
            Key secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] inputBytes = new byte[inputString.length()];
            byte[] outputBytes = cipher.doFinal(inputBytes);

            FileOutputStream outputStream = new FileOutputStream(outputFile);
            outputStream.write(outputBytes);


            outputStream.close();

        } catch (NoSuchPaddingException | NoSuchAlgorithmException
                | InvalidKeyException | BadPaddingException
                | IllegalBlockSizeException | IOException ex) {
            throw new CryptoException("Error encrypting/decrypting file", ex);
        }
    }

}