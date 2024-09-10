package com.webank.wecross.stub.web3;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Block;
import com.webank.wecross.stub.BlockManager;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stub.Request;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.Transaction;
import com.webank.wecross.stub.TransactionContext;
import com.webank.wecross.stub.TransactionException;
import com.webank.wecross.stub.TransactionRequest;
import com.webank.wecross.stub.TransactionResponse;
import com.webank.wecross.stub.web3.account.Web3Account;
import com.webank.wecross.stub.web3.client.ClientWrapper;
import com.webank.wecross.stub.web3.common.ObjectMapperFactory;
import com.webank.wecross.stub.web3.common.Web3RequestType;
import com.webank.wecross.stub.web3.common.Web3StatusCode;
import com.webank.wecross.stub.web3.common.Web3StubException;
import com.webank.wecross.stub.web3.contract.BlockUtility;
import com.webank.wecross.stub.web3.custom.CommandHandler;
import com.webank.wecross.stub.web3.custom.CommandHandlerDispatcher;
import com.webank.wecross.stub.web3.protocol.request.TransactionParams;
import com.webank.wecross.stub.web3.protocol.response.TransactionPair;
import com.webank.wecross.stub.web3.uaproof.Signer;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.fisco.bcos.sdk.abi.ABICodec;
import org.fisco.bcos.sdk.abi.wrapper.ABICodecJsonWrapper;
import org.fisco.bcos.sdk.abi.wrapper.ABIDefinition;
import org.fisco.bcos.sdk.abi.wrapper.ABIDefinitionFactory;
import org.fisco.bcos.sdk.abi.wrapper.ABIObject;
import org.fisco.bcos.sdk.abi.wrapper.ABIObjectFactory;
import org.fisco.bcos.sdk.abi.wrapper.ContractABIDefinition;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.model.CryptoType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionDecoder;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Numeric;

public class Web3Driver implements Driver {
  private static final Logger logger = LoggerFactory.getLogger(Web3Driver.class);
  private final ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
  private final ABICodecJsonWrapper codecJsonWrapper;
  private final ABICodec abiCodec;
  private final ABIDefinitionFactory abiDefinitionFactory;
  private final CommandHandlerDispatcher commandHandlerDispatcher;

  public Web3Driver(CommandHandlerDispatcher commandHandlerDispatcher) {
    CryptoSuite cryptoSuite = new CryptoSuite(CryptoType.ECDSA_TYPE);
    this.codecJsonWrapper = new ABICodecJsonWrapper(true);
    this.abiCodec = new ABICodec(cryptoSuite, true);
    this.abiDefinitionFactory = new ABIDefinitionFactory(cryptoSuite);
    this.commandHandlerDispatcher = commandHandlerDispatcher;
  }

  @Override
  public ImmutablePair<Boolean, TransactionRequest> decodeTransactionRequest(Request request) {

    int requestType = request.getType();
    if ((requestType != Web3RequestType.CALL)
        && (requestType != Web3RequestType.SEND_TRANSACTION)) {
      return new ImmutablePair<>(false, null);
    }
    try {
      TransactionParams transactionParams =
          objectMapper.readValue(request.getData(), TransactionParams.class);
      if (logger.isTraceEnabled()) {
        logger.trace(" TransactionParams: {}", transactionParams);
      }
      Objects.requireNonNull(
          transactionParams.getTransactionRequest(), "TransactionRequest is null");
      Objects.requireNonNull(transactionParams.getData(), "Data is null");
      Objects.requireNonNull(transactionParams.getSubType(), "type is null");
      TransactionRequest transactionRequest = transactionParams.getTransactionRequest();
      TransactionParams.SUB_TYPE subType = transactionParams.getSubType();
      String contractAbi = transactionParams.getAbi();
      String[] args = transactionRequest.getArgs();
      String method = transactionRequest.getMethod();
      String encodedFromInput;
      String encodedFromNow =
          abiCodec.encodeMethodFromString(
              contractAbi, method, args != null ? Arrays.asList(args) : new ArrayList<>());
      switch (subType) {
        case SEND_TX:
          String data = transactionParams.getData();
          RawTransaction rawTransaction = TransactionDecoder.decode(data);
          encodedFromInput = rawTransaction.getData();
          break;
        case CALL:
          encodedFromInput = transactionParams.getData();
          break;
        default:
          {
            return new ImmutablePair<>(true, null);
          }
      }
      if (Numeric.cleanHexPrefix(encodedFromNow).equals(Numeric.cleanHexPrefix(encodedFromInput))) {
        return new ImmutablePair<>(true, transactionRequest);
      }
      logger.warn(
          " encodedFromInput not meet expectations, encodedFromInput:{}, encodedFromNow:{}",
          encodedFromInput,
          encodedFromNow);
      return new ImmutablePair<>(true, null);

    } catch (Exception e) {
      logger.error("decodeTransactionRequest error: ", e);
      return new ImmutablePair<>(true, null);
    }
  }

  @Override
  public List<ResourceInfo> getResources(Connection connection) {
    if (connection instanceof Web3Connection) {
      return ((Web3Connection) connection).getResourceInfoList();
    }
    logger.error("Not Web3 connection, connection name: {}", connection.getClass().getName());
    return new ArrayList<>();
  }

  @Override
  public void asyncCall(
      TransactionContext context,
      TransactionRequest request,
      boolean byProxy,
      Connection connection,
      Callback callback) {

    try {
      String from = context.getAccount().getIdentity();
      Path path = context.getPath();
      String name = path.getResource();
      Web3Connection web3Connection = (Web3Connection) connection;
      Map<String, String> properties = connection.getProperties();
      String contractAbi = web3Connection.getAbi(name);
      String contractAddress = properties.get(name);
      checkContract(name, contractAddress, contractAbi);
      String[] args = request.getArgs();
      String method = request.getMethod();
      String encodedMethodWithArgs =
          abiCodec.encodeMethodFromString(
              contractAbi, method, args != null ? Arrays.asList(args) : new ArrayList<>());
      TransactionParams transaction =
          new TransactionParams(
              request, TransactionParams.SUB_TYPE.CALL, encodedMethodWithArgs, contractAbi);
      transaction.setFrom(from);
      transaction.setTo(contractAddress);
      transaction.setAbi(contractAbi);
      Request req =
          Request.newRequest(Web3RequestType.CALL, objectMapper.writeValueAsBytes(transaction));
      connection.asyncSend(
          req,
          response -> {
            try {
              if (response.getErrorCode() != Web3StatusCode.Success) {
                callback.onTransactionResponse(
                    new TransactionException(response.getErrorCode(), response.getErrorMessage()),
                    null);
                return;
              }
              TransactionResponse transactionResponse = new TransactionResponse();
              transactionResponse.setErrorCode(Web3StatusCode.Success);
              transactionResponse.setMessage(
                  Web3StatusCode.getStatusMessage(Web3StatusCode.Success));
              EthCall ethCall = objectMapper.readValue(response.getData(), EthCall.class);
              String ethCallValue = ethCall.getValue();
              if (StringUtils.isNotBlank(ethCallValue)) {
                ContractABIDefinition contractABIDefinition =
                    abiDefinitionFactory.loadABI(contractAbi);
                ABIDefinition abiDefinition =
                    contractABIDefinition.getFunctions().get(method).stream()
                        .filter(
                            function ->
                                function.getInputs().size() == (args == null ? 0 : args.length))
                        .findFirst()
                        .orElseThrow(
                            () ->
                                new Web3StubException(
                                    Web3StatusCode.MethodNotExist, "method not exist: " + method));
                ABIObject outputObject = ABIObjectFactory.createOutputObject(abiDefinition);

                transactionResponse.setResult(
                    codecJsonWrapper.decode(outputObject, ethCallValue).toArray(new String[0]));
              }
              callback.onTransactionResponse(null, transactionResponse);
            } catch (Exception e) {
              logger.warn(" e: ", e);
              callback.onTransactionResponse(
                  new TransactionException(Web3StatusCode.UnclassifiedError, e.getMessage()), null);
            }
          });
    } catch (Web3StubException wse) {
      logger.warn(" e: ", wse);
      callback.onTransactionResponse(
          new TransactionException(wse.getErrorCode(), wse.getMessage()), null);
    } catch (Exception e) {
      logger.warn(" e: ", e);
      callback.onTransactionResponse(
          new TransactionException(Web3StatusCode.UnclassifiedError, e.getMessage()), null);
    }
  }

  @Override
  public void asyncSendTransaction(
      TransactionContext context,
      TransactionRequest request,
      boolean byProxy,
      Connection connection,
      Callback callback) {
    try {
      Credentials credentials = ((Web3Account) context.getAccount()).getCredentials();
      Path path = context.getPath();
      String name = path.getResource();
      Web3Connection web3Connection = (Web3Connection) connection;
      Map<String, String> properties = connection.getProperties();
      String contractAddress = properties.get(name);
      String contractAbi = web3Connection.getAbi(name);
      checkContract(name, contractAddress, contractAbi);
      String[] args = request.getArgs();
      String method = request.getMethod();
      String encodedMethodWithArgs =
          abiCodec.encodeMethodFromString(
              contractAbi, method, args != null ? Arrays.asList(args) : new ArrayList<>());
      ClientWrapper clientWrapper = web3Connection.getClientWrapper();
      BigInteger nonce = clientWrapper.getNonce(context.getAccount().getIdentity());
      BigInteger gasLimit = clientWrapper.ethGasLimit();
      BigInteger gasPrice = clientWrapper.ethGasPrice();
      BigInteger chainId = web3Connection.getChainId();
      RawTransaction rawTransaction =
          RawTransaction.createTransaction(
              nonce, gasPrice, gasLimit, contractAddress, encodedMethodWithArgs);
      byte[] signedMessage =
          TransactionEncoder.signMessage(rawTransaction, chainId.longValue(), credentials);
      TransactionParams transaction =
          new TransactionParams(
              request,
              TransactionParams.SUB_TYPE.SEND_TX,
              Numeric.toHexString(signedMessage),
              contractAbi);
      transaction.setFrom(credentials.getAddress());
      transaction.setTo(contractAddress);
      transaction.setAbi(contractAbi);
      Request req =
          Request.newRequest(
              Web3RequestType.SEND_TRANSACTION, objectMapper.writeValueAsBytes(transaction));
      connection.asyncSend(
          req,
          response -> {
            try {
              if (response.getErrorCode() != Web3StatusCode.Success) {
                callback.onTransactionResponse(
                    new TransactionException(response.getErrorCode(), response.getErrorMessage()),
                    null);
                return;
              }
              TransactionResponse transactionResponse = new TransactionResponse();
              TransactionReceipt receipt =
                  objectMapper.readValue(response.getData(), TransactionReceipt.class);
              transactionResponse.setHash(receipt.getTransactionHash());
              transactionResponse.setBlockNumber(receipt.getBlockNumber().longValue());
              transactionResponse.setErrorCode(Web3StatusCode.Success);
              transactionResponse.setMessage(
                  Web3StatusCode.getStatusMessage(Web3StatusCode.Success));
              callback.onTransactionResponse(null, transactionResponse);
            } catch (Exception e) {
              logger.warn(" e: ", e);
              callback.onTransactionResponse(
                  new TransactionException(Web3StatusCode.UnclassifiedError, e.getMessage()), null);
            }
          });

    } catch (Web3StubException wse) {
      logger.warn(" e: ", wse);
      callback.onTransactionResponse(
          new TransactionException(wse.getErrorCode(), wse.getMessage()), null);
    } catch (Exception e) {
      logger.warn(" e: ", e);
      callback.onTransactionResponse(
          new TransactionException(Web3StatusCode.UnclassifiedError, e.getMessage()), null);
    }
  }

  private static void checkContract(String name, String contractAddress, String contractAbi)
      throws Web3StubException {
    if (StringUtils.isBlank(contractAbi)) {
      throw new Web3StubException(Web3StatusCode.ABINotExist, "resource ABI not exist: " + name);
    }
    if (StringUtils.isBlank(contractAddress)) {
      throw new Web3StubException(
          Web3StatusCode.AddressNotExist, "resource address not exist: " + name);
    }
  }

  @Override
  public void asyncGetBlockNumber(Connection connection, GetBlockNumberCallback callback) {
    Request request = Request.newRequest(Web3RequestType.GET_BLOCK_NUMBER, "");
    connection.asyncSend(
        request,
        response -> {
          if (response.getErrorCode() != Web3StatusCode.Success) {
            logger.warn(
                " errorCode: {},  errorMessage: {}",
                response.getErrorCode(),
                response.getErrorMessage());
            callback.onResponse(new Exception(response.getErrorMessage()), -1);
          } else {
            BigInteger blockNumber = new BigInteger(response.getData());
            logger.debug(" blockNumber: {}", blockNumber);
            callback.onResponse(null, blockNumber.longValue());
          }
        });
  }

  @Override
  public void asyncGetBlock(
      long blockNumber, boolean onlyHeader, Connection connection, GetBlockCallback callback) {
    Request request =
        Request.newRequest(
            Web3RequestType.GET_BLOCK_BY_NUMBER, BigInteger.valueOf(blockNumber).toByteArray());
    connection.asyncSend(
        request,
        response -> {
          if (response.getErrorCode() != Web3StatusCode.Success) {
            logger.warn(
                " asyncGetBlock, errorCode: {},  errorMessage: {}",
                response.getErrorCode(),
                response.getErrorMessage());
            callback.onResponse(new Exception(response.getErrorMessage()), null);
          } else {
            try {
              Block block = BlockUtility.convertToBlock(response.getData(), onlyHeader);
              callback.onResponse(null, block);
            } catch (Exception e) {
              logger.warn(" blockNumber: {}, e: ", blockNumber, e);
              callback.onResponse(e, null);
            }
          }
        });
  }

  @Override
  public void asyncGetTransaction(
      String transactionHash,
      long blockNumber,
      BlockManager blockManager,
      boolean isVerified,
      Connection connection,
      GetTransactionCallback callback) {
    Request request = Request.newRequest(Web3RequestType.GET_TRANSACTION, transactionHash);
    connection.asyncSend(
        request,
        response -> {
          try {
            if (logger.isDebugEnabled()) {
              logger.debug("Request get Transaction, transactionHash: {}", transactionHash);
            }
            if (response.getErrorCode() != Web3StatusCode.Success) {
              callback.onResponse(
                  new Web3StubException(response.getErrorCode(), response.getErrorMessage()), null);
              return;
            }
            TransactionPair transactionPair =
                objectMapper.readValue(response.getData(), TransactionPair.class);
            org.web3j.protocol.core.methods.response.Transaction transactionResponse =
                transactionPair.getTransaction();
            TransactionReceipt transactionReceipt = transactionPair.getTransactionReceipt();
            byte[] txBytes = objectMapper.writeValueAsBytes(transactionResponse);
            byte[] receiptBytes = objectMapper.writeValueAsBytes(transactionReceipt);
            Transaction transaction = new Transaction();
            transaction.setReceiptBytes(receiptBytes);
            transaction.setTxBytes(txBytes);
            transaction.setAccountIdentity(transactionReceipt.getFrom());
            transaction.setTransactionByProxy(false);
            transaction.getTransactionResponse().setHash(transactionHash);
            transaction
                .getTransactionResponse()
                .setBlockNumber(transactionReceipt.getBlockNumber().longValue());
            callback.onResponse(null, transaction);
            if (logger.isDebugEnabled()) {
              logger.debug(" transactionHash: {}, transaction: {}", transactionHash, transaction);
            }
          } catch (Exception e) {
            callback.onResponse(
                new Web3StubException(Web3StatusCode.UnclassifiedError, e.getMessage()), null);
          }
        });
  }

  @Override
  public void asyncCustomCommand(
      String command,
      Path path,
      Object[] args,
      Account account,
      BlockManager blockManager,
      Connection connection,
      CustomCommandCallback callback) {
    CommandHandler commandHandler = commandHandlerDispatcher.matchCommandHandler(command);
    if (Objects.isNull(commandHandler)) {
      callback.onResponse(new Exception("command not supported: " + command), null);
      return;
    }
    commandHandler.handle(path, args, account, blockManager, connection, callback);
  }

  @Override
  public byte[] accountSign(Account account, byte[] message) {
    if (!(account instanceof Web3Account)) {
      throw new UnsupportedOperationException(
          "Not Web3Account, account name: " + account.getClass().getName());
    }
    Credentials credentials = ((Web3Account) account).getCredentials();
    return Signer.sign(credentials.getEcKeyPair(), message);
  }

  @Override
  public boolean accountVerify(String identity, byte[] signBytes, byte[] message) {
    return Signer.verify(signBytes, message, identity);
  }
}
