package com.webank.wecross.stub.web3.client;

import java.io.IOException;
import java.math.BigInteger;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

/** Wrapper interface for JavaSDK */
public interface ClientWrapper {

  EthBlock.Block ethGetBlockByNumber(BigInteger blockNumber) throws IOException;

  BigInteger ethBlockNumber() throws IOException;

  EthSendTransaction ethSendRawTransaction(String signedTransactionData) throws IOException;

  TransactionReceipt ethGetTransactionReceipt(String transactionHash) throws Exception;

  Transaction ethGetTransactionByHash(String transactionHash) throws IOException;

  EthCall ethCall(org.web3j.protocol.core.methods.request.Transaction transaction)
      throws IOException;

  BigInteger getNonce(String address) throws IOException;

  BigInteger ethChainId() throws IOException;

  BigInteger ethGasPrice() throws IOException;

  BigInteger ethGasLimit() throws IOException;
}
