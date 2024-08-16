package com.webank.wecross.stub.web3.event;

import com.webank.wecross.stub.web3.client.ClientWrapper;
import com.webank.wecross.stub.web3.config.Web3StubConfig;
import java.util.Arrays;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;

public class Web3ChainEventManager {
  private static final Logger logger = LoggerFactory.getLogger(Web3ChainEventManager.class);

  private final ClientWrapper clientWrapper;

  public static final String LUYU_CALL = "LuyuCall";
  public static final String LUYU_SEND_TRANSACTION = "LuyuSendTransaction";

  public static final Event LUYUCALL_EVENT =
      new Event(
          LUYU_CALL,
          Arrays.asList(
              new TypeReference<Utf8String>() {},
              new TypeReference<Utf8String>() {},
              new TypeReference<DynamicArray<Utf8String>>() {},
              new TypeReference<Uint256>() {},
              new TypeReference<Utf8String>() {},
              new TypeReference<Utf8String>() {},
              new TypeReference<Address>() {}));

  public static final Event LUYUSENDTRANSACTION_EVENT =
      new Event(
          LUYU_SEND_TRANSACTION,
          Arrays.asList(
              new TypeReference<Utf8String>() {},
              new TypeReference<Utf8String>() {},
              new TypeReference<DynamicArray<Utf8String>>() {},
              new TypeReference<Uint256>() {},
              new TypeReference<Utf8String>() {},
              new TypeReference<Utf8String>() {},
              new TypeReference<Address>() {}));

  public Web3ChainEventManager(ClientWrapper clientWrapper) {
    this.clientWrapper = clientWrapper;
  }

  public void registerEvent(Web3StubConfig.Resource resource) {
    registerEvent(resource.getName(), resource.getAddress(), LUYUCALL_EVENT);
    registerEvent(resource.getName(), resource.getAddress(), LUYUSENDTRANSACTION_EVENT);
  }

  private void registerEvent(String resourceName, String address, Event event) {
    EthFilter ethFilter =
        new EthFilter(
            DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST, address);
    String encodedEventSignature = EventEncoder.encode(event);
    ethFilter.addSingleTopic(encodedEventSignature);

    clientWrapper.subscribe(
        ethFilter,
        new Subscriber<Log>() {
          @Override
          public void onSubscribe(Subscription subscription) {
            subscription.request(Long.MAX_VALUE);
          }

          @Override
          public void onNext(Log log) {
            // TODO handle events based on event name
            String eventName = event.getName();
          }

          @Override
          public void onError(Throwable throwable) {}

          @Override
          public void onComplete() {
            registerEvent(resourceName, address, event);
          }
        });
  }
}
