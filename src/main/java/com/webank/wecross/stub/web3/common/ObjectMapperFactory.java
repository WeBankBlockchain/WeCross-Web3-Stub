package com.webank.wecross.stub.web3.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.webank.wecross.stub.web3.protocol.response.TransactionCustom;
import java.io.IOException;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;

public class ObjectMapperFactory {
  private static final ObjectMapper DEFAULT_OBJECT_MAPPER = getObjectMapper();

  static {
    configureObjectMapper(DEFAULT_OBJECT_MAPPER);
  }

  public static ObjectMapper getObjectMapper() {
    return configureObjectMapper(new ObjectMapper());
  }

  public static ObjectReader getObjectReader() {
    return DEFAULT_OBJECT_MAPPER.reader();
  }

  private static ObjectMapper configureObjectMapper(ObjectMapper objectMapper) {

    objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    // custom serialization
    SimpleModule simpleModule = new SimpleModule();
    simpleModule.addSerializer(EthBlock.TransactionHash.class, new TransactionHashSerialize());
    simpleModule.addSerializer(Transaction.class, new TransactionSerialize());
    return objectMapper;
  }

  public static class TransactionHashSerialize extends JsonSerializer<EthBlock.TransactionHash> {
    @Override
    public void serialize(
        EthBlock.TransactionHash value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      gen.writeString(value.get());
    }
  }

  public static class TransactionSerialize extends JsonSerializer<Transaction> {
    @Override
    public void serialize(Transaction value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      TransactionCustom transaction = new TransactionCustom();
      transaction.setHash(value.getHash());
      transaction.setNonce(value.getNonceRaw());
      transaction.setBlockHash(value.getBlockHash());
      transaction.setBlockNumber(value.getBlockNumberRaw());
      transaction.setChainId(value.getChainIdRaw());
      transaction.setTransactionIndex(value.getTransactionIndexRaw());
      transaction.setFrom(value.getFrom());
      transaction.setTo(value.getTo());
      transaction.setValue(value.getValueRaw());
      transaction.setGasPrice(value.getGasPriceRaw());
      transaction.setGas(value.getGasRaw());
      transaction.setInput(value.getInput());
      transaction.setCreates(value.getCreates());
      transaction.setPublicKey(value.getPublicKey());
      transaction.setRaw(value.getRaw());
      transaction.setR(value.getR());
      transaction.setS(value.getS());
      transaction.setV(value.getV());
      transaction.setType(value.getType());
      transaction.setMaxFeePerGas(value.getMaxFeePerGasRaw());
      transaction.setMaxPriorityFeePerGas(value.getMaxPriorityFeePerGasRaw());
      transaction.setAccessList(value.getAccessList());
      gen.writeObject(transaction);
    }
  }
}
