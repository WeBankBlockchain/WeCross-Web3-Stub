package com.webank.wecross.stub.web3.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import org.reactivestreams.Subscriber;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

public class ClientWrapperImplMock extends ClientWrapperImpl {
  private final ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

  public ClientWrapperImplMock() {
    super(null);
    this.objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
  }

  @Override
  public EthBlock.Block ethGetBlockByNumber(BigInteger blockNumber) throws IOException {
    String blockJson =
        "{\"number\":108214,\"hash\":\"0x8abe7ca191df7a3b0cc3f081d6d5a1bd2d542bb6a6b7a5c6a9e75244d90c5aad\",\"parentHash\":\"0x21a26483f5968cdaecfc8a85bd92fda6692ceee52b6716dd2c8fa65225820496\",\"nonce\":0,\"sha3Uncles\":\"0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347\",\"logsBloom\":\"0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000\",\"transactionsRoot\":\"0xe7acf5d55d7d2e63d7c0989660a72cf099539be3abe06a446b1db73b3ff5adf4\",\"stateRoot\":\"0x2473e460d2818392c43f5775203c160d23fa13774b82798687cf99e3db7ec054\",\"receiptsRoot\":\"0x3998b0b006ea68156060551908975d78935a9add49a23f65ce497af6a5be311e\",\"author\":null,\"miner\":\"0x8943545177806ed17b9f23f0a21ee5948ecaa776\",\"mixHash\":\"0x928fe08f6690213632620d84bae3826710c959bf9f079001a75c6ff537932c19\",\"difficulty\":0,\"totalDifficulty\":1,\"extraData\":\"0xd883010e07846765746888676f312e32322e35856c696e7578\",\"size\":816,\"gasLimit\":30000000,\"gasUsed\":24780,\"timestamp\":1723105141,\"transactions\":[{\"hash\":\"0x4980adef7b9a6d2cd709cdbd1f308ddff18c6486b3a06fd85387450a87bce8c4\",\"nonce\":77,\"blockHash\":\"0x8abe7ca191df7a3b0cc3f081d6d5a1bd2d542bb6a6b7a5c6a9e75244d90c5aad\",\"blockNumber\":108214,\"chainId\":1337,\"transactionIndex\":0,\"from\":\"0xaff0ca253b97e54440965855cec0a8a2e2399896\",\"to\":\"0x7cf92bd66acacf18f46a4e2dc0280cd67bfae923\",\"value\":0,\"gasPrice\":22000000000,\"gas\":4300000,\"input\":\"0x4ed3885e0000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000000000000000000e78697869786978692121217e7e7e000000000000000000000000000000000000\",\"creates\":null,\"publicKey\":null,\"raw\":null,\"r\":\"0x4e608162ddfc7e43284df9d8d6e40b151b116fcb0f7c14690c0a92ead1214c30\",\"s\":\"0x49789a1f93f74ef1060048a5871604c4419650ced2ab37786094f2cdd5677224\",\"v\":2710,\"type\":\"0x0\",\"maxFeePerGas\":null,\"maxPriorityFeePerGas\":null,\"accessList\":null,\"transactionIndexRaw\":\"0x0\",\"blockNumberRaw\":\"0x1a6b6\",\"maxPriorityFeePerGasRaw\":null,\"gasPriceRaw\":\"0x51f4d5c00\",\"nonceRaw\":\"0x4d\",\"gasRaw\":\"0x419ce0\",\"valueRaw\":\"0x0\",\"chainIdRaw\":\"0x539\",\"maxFeePerGasRaw\":null}],\"uncles\":[],\"sealFields\":null,\"baseFeePerGas\":7,\"gasUsedRaw\":\"0x60cc\",\"nonceRaw\":\"0x0000000000000000\",\"difficultyRaw\":\"0x0\",\"sizeRaw\":\"0x330\",\"numberRaw\":\"0x1a6b6\",\"gasLimitRaw\":\"0x1c9c380\",\"timestampRaw\":\"0x66b47f75\",\"totalDifficultyRaw\":\"0x1\",\"baseFeePerGasRaw\":\"0x7\"}";
    return objectMapper.readValue(blockJson, EthBlock.Block.class);
  }

  @Override
  public BigInteger ethBlockNumber() throws IOException {
    return BigInteger.valueOf(108214);
  }

  @Override
  public EthSendTransaction ethSendRawTransaction(String signedTransactionData) throws IOException {
    String transactionHash = "0x4980adef7b9a6d2cd709cdbd1f308ddff18c6486b3a06fd85387450a87bce8c4";
    EthSendTransaction ethSendTransaction = new EthSendTransaction();
    ethSendTransaction.setResult(transactionHash);
    return ethSendTransaction;
  }

  @Override
  public TransactionReceipt ethGetTransactionReceipt(String transactionHash) throws Exception {
    String transactionReceiptJson =
        "{\"transactionHash\":\"0x4980adef7b9a6d2cd709cdbd1f308ddff18c6486b3a06fd85387450a87bce8c4\",\"transactionIndex\":0,\"blockHash\":\"0x8abe7ca191df7a3b0cc3f081d6d5a1bd2d542bb6a6b7a5c6a9e75244d90c5aad\",\"blockNumber\":108214,\"cumulativeGasUsed\":24780,\"gasUsed\":24780,\"contractAddress\":null,\"root\":null,\"status\":\"0x1\",\"from\":\"0xaff0ca253b97e54440965855cec0a8a2e2399896\",\"to\":\"0x7cf92bd66acacf18f46a4e2dc0280cd67bfae923\",\"logs\":[],\"logsBloom\":\"0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000\",\"revertReason\":null,\"type\":\"0x0\",\"effectiveGasPrice\":\"0x51f4d5c00\",\"statusOK\":true,\"blockNumberRaw\":\"0x1a6b6\",\"gasUsedRaw\":\"0x60cc\",\"cumulativeGasUsedRaw\":\"0x60cc\",\"transactionIndexRaw\":\"0x0\"}";
    return objectMapper.readValue(transactionReceiptJson, TransactionReceipt.class);
  }

  @Override
  public Transaction ethGetTransactionByHash(String transactionHash) throws IOException {
    String transactionJson =
        "{\"hash\":\"0x4980adef7b9a6d2cd709cdbd1f308ddff18c6486b3a06fd85387450a87bce8c4\",\"nonce\":77,\"blockHash\":\"0x8abe7ca191df7a3b0cc3f081d6d5a1bd2d542bb6a6b7a5c6a9e75244d90c5aad\",\"blockNumber\":108214,\"chainId\":1337,\"transactionIndex\":0,\"from\":\"0xaff0ca253b97e54440965855cec0a8a2e2399896\",\"to\":\"0x7cf92bd66acacf18f46a4e2dc0280cd67bfae923\",\"value\":0,\"gasPrice\":22000000000,\"gas\":4300000,\"input\":\"0x4ed3885e0000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000000000000000000e78697869786978692121217e7e7e000000000000000000000000000000000000\",\"creates\":null,\"publicKey\":null,\"raw\":null,\"r\":\"0x4e608162ddfc7e43284df9d8d6e40b151b116fcb0f7c14690c0a92ead1214c30\",\"s\":\"0x49789a1f93f74ef1060048a5871604c4419650ced2ab37786094f2cdd5677224\",\"v\":2710,\"type\":\"0x0\",\"maxFeePerGas\":null,\"maxPriorityFeePerGas\":null,\"accessList\":null,\"transactionIndexRaw\":\"0x0\",\"blockNumberRaw\":\"0x1a6b6\",\"maxPriorityFeePerGasRaw\":null,\"maxFeePerGasRaw\":null,\"nonceRaw\":\"0x4d\",\"valueRaw\":\"0x0\",\"gasPriceRaw\":\"0x51f4d5c00\",\"gasRaw\":\"0x419ce0\",\"chainIdRaw\":\"0x539\"}";
    return objectMapper.readValue(transactionJson, Transaction.class);
  }

  @Override
  public EthCall ethCall(org.web3j.protocol.core.methods.request.Transaction transaction)
      throws IOException {
    EthCall ethCall = new EthCall();
    ethCall.setId(1);
    ethCall.setJsonrpc("2.0");
    return ethCall;
  }

  @Override
  public BigInteger getNonce(String address) {
    SecureRandom random = new SecureRandom();
    return BigInteger.probablePrime(64, random);
  }

  @Override
  public BigInteger ethChainId() {
    return BigInteger.valueOf(1337);
  }

  @Override
  public BigInteger ethGasPrice() {
    return BigInteger.valueOf(22_000_000_000L);
  }

  @Override
  public BigInteger ethGasLimit() {
    return BigInteger.valueOf(4_300_000);
  }

  @Override
  public void subscribe(EthFilter ethFilter, Subscriber<Log> subscriber) {}

  @Override
  public String extractRevertReason(TransactionReceipt transactionReceipt, String data) {
    return "execution reverted: method reverted!";
  }
}
