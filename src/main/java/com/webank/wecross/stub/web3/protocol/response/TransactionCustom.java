package com.webank.wecross.stub.web3.protocol.response;

import java.math.BigInteger;
import java.util.List;
import org.web3j.crypto.TransactionUtils;
import org.web3j.protocol.core.methods.response.AccessListObject;
import org.web3j.utils.Numeric;

/** Transaction serialize object, before Numeric.decodeQuantity() check null */
public class TransactionCustom {
  private String hash;
  private String nonce;
  private String blockHash;
  private String blockNumber;
  private String chainId;
  private String transactionIndex;
  private String from;
  private String to;
  private String value;
  private String gasPrice;
  private String gas;
  private String input;
  private String creates;
  private String publicKey;
  private String raw;
  private String r;
  private String s;
  private long v; // see https://github.com/web3j/web3j/issues/44
  private String type;
  private String maxFeePerGas;
  private String maxPriorityFeePerGas;
  private List<AccessListObject> accessList;

  public TransactionCustom() {}

  public void setChainId(String chainId) {
    this.chainId = chainId;
  }

  public String getHash() {
    return hash;
  }

  public void setHash(String hash) {
    this.hash = hash;
  }

  public BigInteger getNonce() {
    if (nonce == null) return null;
    return Numeric.decodeQuantity(nonce);
  }

  public void setNonce(String nonce) {
    this.nonce = nonce;
  }

  public String getNonceRaw() {
    return nonce;
  }

  public String getBlockHash() {
    return blockHash;
  }

  public void setBlockHash(String blockHash) {
    this.blockHash = blockHash;
  }

  public BigInteger getBlockNumber() {
    if (blockNumber == null) return null;
    return Numeric.decodeQuantity(blockNumber);
  }

  public void setBlockNumber(String blockNumber) {
    this.blockNumber = blockNumber;
  }

  public String getBlockNumberRaw() {
    return blockNumber;
  }

  public BigInteger getTransactionIndex() {
    if (transactionIndex == null) return null;
    return Numeric.decodeQuantity(transactionIndex);
  }

  public void setTransactionIndex(String transactionIndex) {
    this.transactionIndex = transactionIndex;
  }

  public String getTransactionIndexRaw() {
    return transactionIndex;
  }

  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public String getTo() {
    return to;
  }

  public void setTo(String to) {
    this.to = to;
  }

  public BigInteger getValue() {
    if (value == null) return null;
    return Numeric.decodeQuantity(value);
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getValueRaw() {
    return value;
  }

  public BigInteger getGasPrice() {
    if (gasPrice == null) return null;
    return Numeric.decodeQuantity(gasPrice);
  }

  public void setGasPrice(String gasPrice) {
    this.gasPrice = gasPrice;
  }

  public String getGasPriceRaw() {
    return gasPrice;
  }

  public BigInteger getGas() {
    if (gas == null) return null;
    return Numeric.decodeQuantity(gas);
  }

  public void setGas(String gas) {
    this.gas = gas;
  }

  public String getGasRaw() {
    return gas;
  }

  public String getInput() {
    return input;
  }

  public void setInput(String input) {
    this.input = input;
  }

  public String getCreates() {
    return creates;
  }

  public void setCreates(String creates) {
    this.creates = creates;
  }

  public String getPublicKey() {
    return publicKey;
  }

  public void setPublicKey(String publicKey) {
    this.publicKey = publicKey;
  }

  public String getRaw() {
    return raw;
  }

  public void setRaw(String raw) {
    this.raw = raw;
  }

  public String getR() {
    return r;
  }

  public void setR(String r) {
    this.r = r;
  }

  public String getS() {
    return s;
  }

  public void setS(String s) {
    this.s = s;
  }

  public long getV() {
    return v;
  }

  // Workaround until Geth & Parity return consistent values. At present
  // Parity returns a byte value, Geth returns a hex-encoded string
  // https://github.com/ethereum/go-ethereum/issues/3339
  public void setV(Object v) {
    if (v instanceof String) {
      // longValueExact() is not implemented on android 11 or later only on 12 so it was
      // replaced with longValue.
      this.v = Numeric.toBigInt((String) v).longValue();
    } else if (v instanceof Integer) {
      this.v = ((Integer) v).longValue();
    } else {
      this.v = (Long) v;
    }
  }

  //    public void setV(byte v) {
  //        this.v = v;
  //    }

  public Long getChainId() {
    if (chainId != null) {
      return Numeric.decodeQuantity(chainId).longValue();
    }

    return TransactionUtils.deriveChainId(v);
  }

  public String getChainIdRaw() {
    return this.chainId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public BigInteger getMaxFeePerGas() {
    if (maxFeePerGas == null) return null;
    return Numeric.decodeQuantity(maxFeePerGas);
  }

  public String getMaxFeePerGasRaw() {
    return maxFeePerGas;
  }

  public void setMaxFeePerGas(String maxFeePerGas) {
    this.maxFeePerGas = maxFeePerGas;
  }

  public String getMaxPriorityFeePerGasRaw() {
    return maxPriorityFeePerGas;
  }

  public BigInteger getMaxPriorityFeePerGas() {
    if (maxPriorityFeePerGas == null) return null;
    return Numeric.decodeQuantity(maxPriorityFeePerGas);
  }

  public void setMaxPriorityFeePerGas(String maxPriorityFeePerGas) {
    this.maxPriorityFeePerGas = maxPriorityFeePerGas;
  }

  public List<AccessListObject> getAccessList() {
    return accessList;
  }

  public void setAccessList(List<AccessListObject> accessList) {
    this.accessList = accessList;
  }
}
