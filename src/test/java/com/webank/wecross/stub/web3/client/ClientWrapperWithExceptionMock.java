package com.webank.wecross.stub.web3.client;

import java.io.IOException;
import java.math.BigInteger;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

public class ClientWrapperWithExceptionMock extends ClientWrapperImplMock {

  public ClientWrapperWithExceptionMock() {}

  @Override
  public EthBlock.Block ethGetBlockByNumber(BigInteger blockNumber) throws IOException {
    throw new IOException("IOException");
  }

  @Override
  public BigInteger ethBlockNumber() throws IOException {
    throw new IOException("IOException");
  }

  @Override
  public EthSendTransaction ethSendRawTransaction(String signedTransactionData) throws IOException {
    throw new IOException("IOException");
  }

  @Override
  public TransactionReceipt ethGetTransactionReceipt(String transactionHash) throws Exception {
    throw new IOException("IOException");
  }

  @Override
  public Transaction ethGetTransactionByHash(String transactionHash) throws IOException {
    throw new IOException("IOException");
  }

  @Override
  public EthCall ethCall(org.web3j.protocol.core.methods.request.Transaction transaction)
      throws IOException {
    throw new IOException("IOException");
  }
}
