package com.webank.wecross.stub.web3;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.Response;
import com.webank.wecross.stub.web3.client.ClientWrapper;
import com.webank.wecross.stub.web3.client.ClientWrapperImpl;
import com.webank.wecross.stub.web3.common.ObjectMapperFactory;
import com.webank.wecross.stub.web3.common.Web3Constant;
import com.webank.wecross.stub.web3.common.Web3RequestType;
import com.webank.wecross.stub.web3.common.Web3StatusCode;
import com.webank.wecross.stub.web3.config.Web3StubConfig;
import com.webank.wecross.stub.web3.protocol.request.TransactionParams;
import com.webank.wecross.stub.web3.protocol.response.TransactionPair;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionDecoder;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

public class Web3Connection implements Connection {
  private static final Logger logger = LoggerFactory.getLogger(Web3Connection.class);
  public static final String RECEIPT_SUCCESS = "0x1";

  private final ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
  private final List<ResourceInfo> resourceInfoList = new ArrayList<>();
  private ConnectionEventHandler eventHandler;
  private final Map<String, String> properties = new ConcurrentHashMap<>();
  private final ClientWrapper clientWrapper;
  private final BigInteger chainId;
  private final String stubConfigFilePath;

  public Web3Connection(ClientWrapper clientWrapper, String web3StubConfigPath) throws IOException {
    this.objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    this.clientWrapper = clientWrapper;
    this.chainId = clientWrapper.ethChainId();
    this.stubConfigFilePath = web3StubConfigPath;
  }

  @Override
  public void asyncSend(Request request, Callback callback) {
    // request type
    int type = request.getType();
    if (type == Web3RequestType.CALL) {
      // constantCall constantCallWithXa
      handleAsyncCallRequest(request, callback);
    } else if (type == Web3RequestType.SEND_TRANSACTION) {
      // sendTransaction sendTransactionWithXa
      handleAsyncTransactionRequest(request, callback);
    } else if (type == Web3RequestType.GET_BLOCK_NUMBER) {
      handleAsyncGetBlockNumberRequest(callback);
    } else if (type == Web3RequestType.GET_BLOCK_BY_NUMBER) {
      handleAsyncGetBlockRequest(request, callback);
    } else if (type == Web3RequestType.GET_TRANSACTION) {
      handleAsyncGetTransaction(request, callback);
    } else {
      logger.warn(" unrecognized request type, type: {}", request.getType());
      Response response = new Response();
      response.setErrorCode(Web3StatusCode.UnrecognizedRequestType);
      response.setErrorMessage(
          Web3StatusCode.getStatusMessage(Web3StatusCode.UnrecognizedRequestType)
              + " ,type: "
              + request.getType());
      callback.onResponse(response);
    }
  }

  @Override
  public Map<String, String> getProperties() {
    return properties;
  }

  @Override
  public void setConnectionEventHandler(ConnectionEventHandler eventHandler) {
    this.eventHandler = eventHandler;
  }

  private void handleAsyncGetTransaction(Request request, Callback callback) {
    Response response = new Response();
    response.setErrorCode(Web3StatusCode.Success);
    response.setErrorMessage(Web3StatusCode.getStatusMessage(Web3StatusCode.Success));
    try {
      String transactionHash = new String(request.getData(), StandardCharsets.UTF_8);
      Transaction transaction = clientWrapper.ethGetTransactionByHash(transactionHash);
      TransactionReceipt transactionReceipt =
          clientWrapper.ethGetTransactionReceipt(transactionHash);

      // transaction or transactionReceipt is null
      if (Objects.isNull(transaction)
          || Objects.isNull(transaction.getHash())
          || Objects.isNull(transactionReceipt)
          || Objects.isNull(transactionReceipt.getTransactionHash())) {
        response.setErrorCode(Web3StatusCode.TransactionNotExist);
        response.setErrorMessage("transaction not found, tx hash: " + transactionHash);
        return;
      }

      // transaction is revert
      String receiptStatus = transactionReceipt.getStatus();
      String revertReason = transactionReceipt.getRevertReason();
      if (!Objects.equals(receiptStatus, RECEIPT_SUCCESS) && StringUtils.isBlank(revertReason)) {
        if (clientWrapper instanceof ClientWrapperImpl) {
          ClientWrapperImpl clientWrapperImpl = (ClientWrapperImpl) clientWrapper;
          clientWrapperImpl.extractRevertReason(transactionReceipt, transaction.getInput());
        }
      }
      response.setData(
          objectMapper.writeValueAsBytes(new TransactionPair(transaction, transactionReceipt)));

      if (logger.isDebugEnabled()) {
        logger.debug(
            "handleAsyncGetTransaction, tx hash: {}, transaction: {}, transactionReceipt: {}",
            transactionHash,
            transaction,
            transactionReceipt);
      }
    } catch (Exception e) {
      logger.error("handleAsyncGetTransaction Exception, e: ", e);
      response.setErrorCode(Web3StatusCode.HandleGetTransactionFailed);
      response.setErrorMessage(e.getMessage());
    } finally {
      callback.onResponse(response);
    }
  }

  private void handleAsyncGetBlockRequest(Request request, Callback callback) {
    Response response = new Response();
    response.setErrorCode(Web3StatusCode.Success);
    response.setErrorMessage(Web3StatusCode.getStatusMessage(Web3StatusCode.Success));
    try {
      BigInteger blockNumber = new BigInteger(request.getData());
      if (logger.isDebugEnabled()) {
        logger.debug("handleAsyncGetBlockRequest,blockNumber: {}", blockNumber);
      }
      // from chain
      EthBlock.Block block = clientWrapper.ethGetBlockByNumber(blockNumber);

      // block is null
      if (Objects.isNull(block)) {
        response.setErrorCode(Web3StatusCode.BlockNotExist);
        response.setErrorMessage(Web3StatusCode.getStatusMessage(Web3StatusCode.BlockNotExist));
        return;
      }

      response.setData(objectMapper.writeValueAsBytes(block));
      if (logger.isDebugEnabled()) {
        logger.debug("handleAsyncGetBlockRequest,blockNumber: {}, block: {}", blockNumber, block);
      }
    } catch (Exception e) {
      logger.error("handleAsyncGetBlockRequest Exception, e: ", e);
      response.setErrorCode(Web3StatusCode.HandleGetBlockFailed);
      response.setErrorMessage(e.getMessage());
    } finally {
      callback.onResponse(response);
    }
  }

  private void handleAsyncGetBlockNumberRequest(Callback callback) {
    Response response = new Response();
    response.setErrorCode(Web3StatusCode.Success);
    response.setErrorMessage(Web3StatusCode.getStatusMessage(Web3StatusCode.Success));
    try {
      BigInteger blockNumber = clientWrapper.ethBlockNumber();
      if (Objects.isNull(blockNumber)) {
        response.setErrorCode(Web3StatusCode.BlockNumberNotExist);
        response.setErrorMessage(
            Web3StatusCode.getStatusMessage(Web3StatusCode.BlockNumberNotExist));
        return;
      }

      if (logger.isDebugEnabled()) {
        logger.debug("handleAsyncGetBlockNumberRequest,blockNumber: {}", blockNumber);
      }

      response.setData(blockNumber.toByteArray());
    } catch (Exception e) {
      logger.error("handleGetBlockNumberRequest Exception, e: ", e);
      response.setErrorCode(Web3StatusCode.HandleGetBlockNumberFailed);
      response.setErrorMessage(e.getMessage());
    } finally {
      callback.onResponse(response);
    }
  }

  private void handleAsyncCallRequest(Request request, Callback callback) {
    Response response = new Response();
    response.setErrorCode(Web3StatusCode.Success);
    response.setErrorMessage(Web3StatusCode.getStatusMessage(Web3StatusCode.Success));
    try {
      TransactionParams transactionParams =
          objectMapper.readValue(request.getData(), TransactionParams.class);
      if (logger.isDebugEnabled()) {
        logger.debug("handleAsyncCallRequest: {}", transactionParams);
      }
      String from = transactionParams.getFrom();
      String to = transactionParams.getTo();
      String data = transactionParams.getData();

      // build Transaction
      org.web3j.protocol.core.methods.request.Transaction transaction =
          org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
              from, to, data);
      EthCall ethCall = clientWrapper.ethCall(transaction);

      if (Objects.isNull(ethCall)) {
        response.setErrorCode(Web3StatusCode.CallNotSuccessStatus);
        response.setErrorMessage("ethCall is null");
        return;
      }

      // ethCall has error
      if (ethCall.hasError()) {
        response.setErrorCode(Web3StatusCode.CallNotSuccessStatus);
        response.setErrorMessage(ethCall.getError().getMessage());
        return;
      }

      response.setData(objectMapper.writeValueAsBytes(ethCall));
    } catch (Exception e) {
      logger.error("handleAsyncCallRequest Exception:", e);
      response.setErrorCode(Web3StatusCode.HandleCallRequestFailed);
      response.setErrorMessage(e.getMessage());
    } finally {
      callback.onResponse(response);
    }
  }

  private void handleAsyncTransactionRequest(Request request, Callback callback) {
    Response response = new Response();
    response.setErrorCode(Web3StatusCode.Success);
    response.setErrorMessage(Web3StatusCode.getStatusMessage(Web3StatusCode.Success));
    try {
      TransactionParams transactionParams =
          objectMapper.readValue(request.getData(), TransactionParams.class);
      if (logger.isDebugEnabled()) {
        logger.debug("handleAsyncTransactionRequest: {}", transactionParams);
      }

      // send transaction
      String signedTransactionData = transactionParams.getData();
      EthSendTransaction ethSendTransaction =
          clientWrapper.ethSendRawTransaction(signedTransactionData);

      if (Objects.isNull(ethSendTransaction)) {
        response.setErrorCode(Web3StatusCode.TransactionReceiptNotExist);
        response.setErrorMessage(
            Web3StatusCode.getStatusMessage(Web3StatusCode.TransactionReceiptNotExist));
        return;
      }

      // ethSendTransaction has error
      if (ethSendTransaction.hasError()) {
        response.setErrorCode(Web3StatusCode.SendTransactionNotSuccessStatus);
        response.setErrorMessage(ethSendTransaction.getError().getMessage());
        return;
      }

      // get transactionReceipt
      String transactionHash = ethSendTransaction.getTransactionHash();
      if (StringUtils.isBlank(transactionHash)) {
        response.setErrorCode(Web3StatusCode.TransactionReceiptNotExist);
        response.setErrorMessage(
            Web3StatusCode.getStatusMessage(Web3StatusCode.TransactionReceiptNotExist));
        return;
      }

      TransactionReceipt transactionReceipt =
          clientWrapper.ethGetTransactionReceipt(transactionHash);

      // transactionReceipt is null
      if (Objects.isNull(transactionReceipt)) {
        response.setErrorCode(Web3StatusCode.TransactionReceiptNotExist);
        response.setErrorMessage(
            Web3StatusCode.getStatusMessage(Web3StatusCode.TransactionReceiptNotExist));
        return;
      }

      // transaction is revert
      String receiptStatus = transactionReceipt.getStatus();
      if (!Objects.equals(receiptStatus, RECEIPT_SUCCESS)) {
        // decode revertReason
        String revertReason = transactionReceipt.getRevertReason();
        if (StringUtils.isBlank(revertReason)) {
          if (clientWrapper instanceof ClientWrapperImpl) {
            ClientWrapperImpl clientWrapperImpl = (ClientWrapperImpl) clientWrapper;
            RawTransaction originalRawTransaction =
                TransactionDecoder.decode(signedTransactionData);
            revertReason =
                clientWrapperImpl.extractRevertReason(
                    transactionReceipt, originalRawTransaction.getData());
          }
        }
        response.setErrorCode(Web3StatusCode.SendTransactionNotSuccessStatus);
        response.setErrorMessage(revertReason);
        return;
      }

      response.setData(objectMapper.writeValueAsBytes(transactionReceipt));
    } catch (Exception e) {
      logger.error("handleAsyncTransactionRequest Exception:", e);
      response.setErrorCode(Web3StatusCode.HandleSendTransactionFailed);
      response.setErrorMessage(e.getMessage());
    } finally {
      callback.onResponse(response);
    }
  }

  public synchronized void refreshStubConfig(Web3StubConfig web3StubConfig) {
    // update local
    if (!resourceInfoList.isEmpty()) {
      resourceInfoList.clear();
    }
    resourceInfoList.addAll(web3StubConfig.convertToResourceInfos());

    if (!properties.isEmpty()) {
      properties.clear();
    }
    addProperty(Web3Constant.WEB3_PROPERTY_CHAIN_ID, chainId.toString());
    addProperty(Web3Constant.WEB3_PROPERTY_STUB_TYPE, web3StubConfig.getCommon().getType());
    addProperty(Web3Constant.WEB3_PROPERTY_CHAIN_URL, web3StubConfig.getService().getUrl());
    List<Web3StubConfig.Resource> resources = web3StubConfig.getResources();
    if (!resources.isEmpty()) {
      for (Web3StubConfig.Resource resource : resources) {
        String name = resource.getName();
        // name->address
        this.addProperty(name, resource.getAddress());
        // name+ABI->abi
        this.addAbi(name, resource.getAbi());
      }
    }

    // update remote
    if (Objects.nonNull(eventHandler)) {
      eventHandler.onResourcesChange(resourceInfoList);
    }
  }

  public boolean hasProxyDeployed() {
    return getProperties().containsKey(Web3Constant.WEB3_PROXY_NAME);
  }

  public boolean hasHubDeployed() {
    return getProperties().containsKey(Web3Constant.WEB3_HUB_NAME);
  }

  public List<ResourceInfo> getResourceInfoList() {
    return resourceInfoList;
  }

  public void addProperty(String key, String value) {
    this.properties.put(key, value);
  }

  public String getProperty(String key) {
    return this.properties.get(key);
  }

  public void addAbi(String key, String value) {
    addProperty(key + Web3Constant.WEB3_PROPERTY_ABI_SUFFIX, value);
  }

  public String getAbi(String key) {
    return getProperty(key + Web3Constant.WEB3_PROPERTY_ABI_SUFFIX);
  }

  public ClientWrapper getClientWrapper() {
    return clientWrapper;
  }

  public BigInteger getChainId() {
    return chainId;
  }

  public String getStubConfigFilePath() {
    return stubConfigFilePath;
  }

  public ConnectionEventHandler getEventHandler() {
    return eventHandler;
  }
}
