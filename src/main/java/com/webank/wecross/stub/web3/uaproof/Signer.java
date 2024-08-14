package com.webank.wecross.stub.web3.uaproof;

import com.webank.wecross.stub.web3.common.Web3SignatureException;
import java.math.BigInteger;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

public class Signer {
  public static byte[] sign(ECKeyPair keyPair, byte[] srcData) {
    byte[] hashData = Hash.sha3(srcData);
    Sign.SignatureData signatureData = Sign.signPrefixedMessage(hashData, keyPair);
    byte[] r = signatureData.getR();
    byte[] s = signatureData.getS();
    byte[] v = signatureData.getV();
    byte[] signByte = Arrays.copyOf(r, v.length + r.length + s.length);
    System.arraycopy(s, 0, signByte, r.length, s.length);
    System.arraycopy(v, 0, signByte, r.length + s.length, v.length);
    return signByte;
  }

  public static boolean verify(byte[] signData, byte[] srcData, String address) {
    byte[] hashData = Hash.sha3(srcData);
    Sign.SignatureData signatureData =
        new Sign.SignatureData(
            signData[64],
            Arrays.copyOfRange(signData, 0, 32),
            Arrays.copyOfRange(signData, 32, 64));
    BigInteger publicKey;
    try {
      publicKey = Sign.signedPrefixedMessageToKey(hashData, signatureData);
    } catch (Exception e) {
      throw new Web3SignatureException("verify failed: " + e.getMessage());
    }
    return StringUtils.equalsIgnoreCase(
        Keys.getAddress(publicKey), Numeric.cleanHexPrefix(address));
  }
}
