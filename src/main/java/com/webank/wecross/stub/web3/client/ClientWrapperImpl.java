package com.webank.wecross.stub.web3.client;

import com.webank.wecross.stub.web3.common.LRUCache;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.RevertReasonExtractor;

public class ClientWrapperImpl implements ClientWrapper {
  private static final Logger logger = LoggerFactory.getLogger(ClientWrapperImpl.class);

  private static final int SLEEP_DURATION = 2000;
  private static final int ATTEMPTS = 40;

  private final Web3j web3j;

  private final LRUCache<BigInteger, EthBlock.Block> blockLRUCache = new LRUCache<>(100);
  private final LRUCache<String, TransactionReceipt> transactionReceiptLRUCache =
      new LRUCache<>(100);
  private final LRUCache<String, Transaction> transactionLRUCache = new LRUCache<>(100);

  public ClientWrapperImpl(Web3j web3j) {
    this.web3j = web3j;
  }

  @Override
  public EthBlock.Block ethGetBlockByNumber(BigInteger blockNumber) throws IOException {
    EthBlock.Block block = blockLRUCache.get(blockNumber);
    if (Objects.nonNull(block)) {
      return block;
    }
    block =
        web3j
            .ethGetBlockByNumber(DefaultBlockParameter.valueOf(blockNumber), true)
            .send()
            .getBlock();
    if (Objects.nonNull(block)) {
      blockLRUCache.put(blockNumber, block);
    }
    return block;
  }

  @Override
  public BigInteger ethBlockNumber() throws IOException {
    return web3j.ethBlockNumber().send().getBlockNumber();
  }

  @Override
  public EthSendTransaction ethSendRawTransaction(String signedTransactionData) throws IOException {
    return web3j.ethSendRawTransaction(signedTransactionData).send();
  }

  @Override
  public TransactionReceipt ethGetTransactionReceipt(String transactionHash) throws Exception {
    TransactionReceipt transactionReceipt = transactionReceiptLRUCache.get(transactionHash);
    if (Objects.nonNull(transactionReceipt)) {
      return transactionReceipt;
    }
    transactionReceipt = waitForTransactionReceipt(transactionHash, SLEEP_DURATION, ATTEMPTS);
    if (Objects.nonNull(transactionReceipt)) {
      transactionReceiptLRUCache.put(transactionHash, transactionReceipt);
    }
    return transactionReceipt;
  }

  @Override
  public Transaction ethGetTransactionByHash(String transactionHash) throws IOException {
    Transaction transaction = transactionLRUCache.get(transactionHash);
    if (Objects.nonNull(transaction)) {
      return transaction;
    }
    Optional<Transaction> optionalTransaction =
        web3j.ethGetTransactionByHash(transactionHash).send().getTransaction();
    if (optionalTransaction.isPresent()) {
      transaction = optionalTransaction.get();
      transactionLRUCache.put(transactionHash, transaction);
    }
    return transaction;
  }

  @Override
  public EthCall ethCall(org.web3j.protocol.core.methods.request.Transaction transaction)
      throws IOException {
    return web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();
  }

  @Override
  public BigInteger getNonce(String address) throws IOException {
    EthGetTransactionCount ethGetTransactionCount =
        web3j.ethGetTransactionCount(address, DefaultBlockParameterName.LATEST).send();
    return ethGetTransactionCount.getTransactionCount();
  }

  @Override
  public BigInteger ethChainId() throws IOException {
    return web3j.ethChainId().send().getChainId();
  }

  @Override
  public BigInteger ethGasPrice() throws IOException {
    return web3j.ethGasPrice().send().getGasPrice();
  }

  @Override
  public BigInteger ethGasLimit() throws IOException {
    return web3j.ethMaxPriorityFeePerGas().send().getMaxPriorityFeePerGas();
  }

  @Override
  public void subscribe(EthFilter ethFilter, Subscriber<Log> subscriber) {
    web3j.ethLogFlowable(ethFilter).safeSubscribe(subscriber);
  }

  public String extractRevertReason(TransactionReceipt transactionReceipt, String data)
      throws IOException {
    String revertReason =
        RevertReasonExtractor.extractRevertReason(
            transactionReceipt, data, web3j, true, BigInteger.ZERO);
    // refresh receipt
    transactionReceiptLRUCache.put(transactionReceipt.getTransactionHash(), transactionReceipt);
    return revertReason;
  }

  private TransactionReceipt waitForTransactionReceipt(
      String transactionHash, int sleepDuration, int attempts) throws Exception {
    Optional<TransactionReceipt> transactionReceiptOptional =
        getTransactionReceipt(transactionHash, sleepDuration, attempts);

    if (!transactionReceiptOptional.isPresent()) {
      logger.error(
          "Transaction receipt not generated after {} , attempts {}", sleepDuration, attempts);
      return null;
    }
    return transactionReceiptOptional.get();
  }

  private Optional<TransactionReceipt> getTransactionReceipt(
      String transactionHash, int sleepDuration, int attempts) throws Exception {
    Optional<TransactionReceipt> receiptOptional = sendTransactionReceiptRequest(transactionHash);
    for (int i = 0; i < attempts; i++) {
      if (!receiptOptional.isPresent()) {
        Thread.sleep(sleepDuration);
        receiptOptional = sendTransactionReceiptRequest(transactionHash);
      } else {
        break;
      }
    }
    return receiptOptional;
  }

  private Optional<TransactionReceipt> sendTransactionReceiptRequest(String transactionHash)
      throws Exception {
    EthGetTransactionReceipt transactionReceipt =
        web3j.ethGetTransactionReceipt(transactionHash).sendAsync().get();
    return transactionReceipt.getTransactionReceipt();
  }

  public Web3j getWeb3j() {
    return web3j;
  }
}
