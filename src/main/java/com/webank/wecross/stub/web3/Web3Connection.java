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
import com.webank.wecross.stub.web3.common.Web3StubException;
import com.webank.wecross.stub.web3.config.Web3StubConfig;
import com.webank.wecross.stub.web3.protocol.request.TransactionParams;
import com.webank.wecross.stub.web3.protocol.response.TransactionPair;
import com.webank.wecross.stub.web3.utils.FunctionUtility;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionDecoder;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Numeric;

public class Web3Connection implements Connection {
  private static final Logger logger = LoggerFactory.getLogger(Web3Connection.class);
  public static final String RECEIPT_SUCCESS = "0x1";

  private final ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
  private List<ResourceInfo> resourceInfoList = new ArrayList<>();
  private List<ResourceInfo> resourcesCache = new ArrayList<>();
  private ConnectionEventHandler eventHandler;
  private final Map<String, String> properties = new HashMap<>();
  private final ClientWrapper clientWrapper;

  private final BigInteger chainId;
  private final BigInteger gasPrice;
  private final BigInteger gasLimit;

  public Web3Connection(
      BigInteger chainId,
      Web3StubConfig web3StubConfig,
      ClientWrapper clientWrapper,
      ScheduledExecutorService scheduledExecutorService) {
    this.objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    this.clientWrapper = clientWrapper;
    this.chainId = chainId;
    this.gasPrice = web3StubConfig.getService().getGasPrice();
    this.gasLimit = web3StubConfig.getService().getGasLimit();

    // refresh resource
    scheduledExecutorService.scheduleAtFixedRate(
        () -> {
          if (Objects.nonNull(eventHandler)) {
            noteOnResourcesChange();
          }
        },
        10000,
        30000,
        TimeUnit.MILLISECONDS);
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
      BigInteger nonce = clientWrapper.getNonce(from);

      // build Transaction
      org.web3j.protocol.core.methods.request.Transaction transaction =
          org.web3j.protocol.core.methods.request.Transaction.createFunctionCallTransaction(
              from, nonce, gasPrice, gasLimit, to, data);
      EthCall ethCall = clientWrapper.ethCall(transaction);

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

      // ethSendTransaction has error
      if (ethSendTransaction.hasError()) {
        response.setErrorCode(Web3StatusCode.SendTransactionNotSuccessStatus);
        response.setErrorMessage(ethSendTransaction.getError().getMessage());
        return;
      }

      // get transactionReceipt
      String transactionHash = ethSendTransaction.getTransactionHash();
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

  private void noteOnResourcesChange() {
    synchronized (this) {
      List<ResourceInfo> resources = getResources();
      if (!resources.equals(resourcesCache) && !resources.isEmpty()) {
        eventHandler.onResourcesChange(resources);
        resourcesCache = resources;
        if (logger.isDebugEnabled()) {
          logger.debug("resources notify, resources: {}", resources);
        }
      }
    }
  }

  public List<ResourceInfo> getResources() {
    List<ResourceInfo> resourceInfos =
        new ArrayList<ResourceInfo>() {
          {
            addAll(resourceInfoList);
          }
        };
    String[] resources = listResources();
    if (Objects.nonNull(resources)) {
      for (String resource : resources) {
        ResourceInfo resourceInfo = new ResourceInfo();
        resourceInfo.setStubType(getProperty(Web3Constant.WEB3_PROPERTY_STUB_TYPE));
        resourceInfo.setName(resource);
        Map<Object, Object> resourceProperties = resourceInfo.getProperties();
        resourceProperties.put(
            Web3Constant.WEB3_PROPERTY_CHAIN_ID, getProperty(Web3Constant.WEB3_PROPERTY_CHAIN_ID));
        resourceInfos.add(resourceInfo);
      }
    }
    return resourceInfos;
  }

  public String[] listResources() {
    try {
      Function function =
          FunctionUtility.newDefaultFunction(FunctionUtility.ProxyGetResourcesMethodName, null);
      String data = FunctionEncoder.encode(function);
      String from = Web3Constant.DEFAULT_ADDRESS;
      String to = properties.get(Web3Constant.WEB3_PROXY_NAME);
      BigInteger nonce = clientWrapper.getNonce(from);
      org.web3j.protocol.core.methods.request.Transaction callTransaction =
          org.web3j.protocol.core.methods.request.Transaction.createFunctionCallTransaction(
              from, nonce, gasPrice, gasLimit, to, data);
      EthCall ethCall = clientWrapper.ethCall(callTransaction);

      if (ethCall.hasError()) {
        logger.error("listResources failed, error {}", ethCall.getError().getMessage());
        throw new Web3StubException(
            Web3StatusCode.ListResourcesFailed, ethCall.getError().getMessage());
      }
      String[] resources = FunctionUtility.decodeDefaultOutput(ethCall.getResult());
      Set<String> set = new LinkedHashSet<>();
      if (Objects.nonNull(resources) && resources.length != 0) {
        for (int i = resources.length - 1; i >= 0; i--) {
          set.add(resources[i]);
        }
      } else {
        logger.debug("No path found and add system resources");
      }
      return set.toArray(new String[0]);
    } catch (Exception e) {
      logger.error("listPaths failed,", e);
      return null;
    }
  }

  public void registerCNS(String path, String address) {
    try {
      // todo credentials where get
      Credentials credentials = null;

      Function function = FunctionUtility.newRegisterCNSProxyFunction(path, address);
      String data = FunctionEncoder.encode(function);

      String to = properties.get(Web3Constant.WEB3_PROXY_NAME);
      BigInteger nonce = clientWrapper.getNonce(credentials.getAddress());
      RawTransaction rawTransaction =
          RawTransaction.createTransaction(nonce, gasPrice, gasLimit, to, data);
      byte[] signedMessage =
          TransactionEncoder.signMessage(rawTransaction, chainId.longValue(), credentials);
      String signedTransactionData = Numeric.toHexString(signedMessage);
      EthSendTransaction ethSendTransaction =
          clientWrapper.ethSendRawTransaction(signedTransactionData);
      if (ethSendTransaction.hasError()) {
        logger.error("registerCNS failed, error {}", ethSendTransaction.getError().getMessage());
        throw new Web3StubException(
            Web3StatusCode.RegisterContractFailed, ethSendTransaction.getError().getMessage());
      }
    } catch (Exception e) {
      logger.error("registerCNS fail,path:{},address:{}", path, address, e);
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

  public void setResourceInfoList(List<ResourceInfo> resourceInfoList) {
    this.resourceInfoList = resourceInfoList;
  }

  @Override
  public Map<String, String> getProperties() {
    return properties;
  }

  @Override
  public void setConnectionEventHandler(ConnectionEventHandler eventHandler) {
    this.eventHandler = eventHandler;
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
}
