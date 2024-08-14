package com.webank.wecross.stub.web3.custom;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandHandlerDispatcher {
  private static final Logger logger = LoggerFactory.getLogger(CommandHandlerDispatcher.class);

  private final Map<String, CommandHandler> commandMapper = new HashMap<>();

  public void registerCommandHandler(String command, CommandHandler commandHandler) {
    commandMapper.putIfAbsent(command, commandHandler);
  }

  public CommandHandler matchCommandHandler(String command) {
    CommandHandler commandHandler = commandMapper.get(command);
    if (Objects.isNull(commandHandler)) {
      logger.warn(" Unsupported command: {}", command);
    }
    return commandHandler;
  }
}
