package com.webank.wecross.stub.web3.client;

import java.math.BigInteger;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

public class ClientWrapperWithNullMock extends ClientWrapperImplMock {

  @Override
  public BigInteger ethBlockNumber() {
    return null;
  }

  @Override
  public EthBlock.Block ethGetBlockByNumber(BigInteger blockNumber) {
    return null;
  }

  @Override
  public EthSendTransaction ethSendRawTransaction(String signedTransactionData) {
    return null;
  }

  @Override
  public TransactionReceipt ethGetTransactionReceipt(String transactionHash) {
    return null;
  }

  @Override
  public Transaction ethGetTransactionByHash(String transactionHash) {
    return null;
  }

  @Override
  public EthCall ethCall(org.web3j.protocol.core.methods.request.Transaction transaction) {
    return null;
  }
}
