package com.webank.wecross.stub.web3.common;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

import com.webank.wecross.stub.web3.config.Web3StubConfig;
import com.webank.wecross.stub.web3.config.Web3StubConfigParser;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import org.junit.Test;

public class Web3StubConfigParserTest {
  @Test
  public void stubConfigParserTest() throws IOException {
    Web3StubConfigParser web3StubConfigParser = new Web3StubConfigParser("./", "stub-sample.toml");
    Web3StubConfig web3StubConfig = web3StubConfigParser.loadConfig();
    Web3StubConfig.Common common = web3StubConfig.getCommon();
    Web3StubConfig.Service service = web3StubConfig.getService();
    List<Web3StubConfig.Resource> resources = web3StubConfig.getResources();

    assertTrue(Objects.nonNull(common));
    assertTrue(Objects.nonNull(service));
    assertTrue(Objects.nonNull(resources) && !resources.isEmpty());

    assertEquals(common.getName(), "web3");
    assertEquals(common.getType(), "WEB3");

    assertEquals(service.getUrl(), "http://localhost:8545");

    assertEquals(resources.size(), 2);
    assertEquals(resources.get(0).getName(), "WeCrossProxy");
    assertEquals(resources.get(0).getType(), "WEB3_CONTRACT");
    assertEquals(resources.get(0).getAddress(), "0xdbF599778641083c9717Ec69e984D67d3309B811");
    assertEquals(
        resources.get(0).getAbi(),
        "[{\"constant\":false,\"inputs\":[{\"name\":\"n\",\"type\":\"string\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"}]");
    assertEquals(resources.get(1).getName(), "WeCrossHub");
    assertEquals(resources.get(1).getType(), "WEB3_CONTRACT");
    assertEquals(resources.get(1).getAddress(), "0xdbF599778641083c9717Ec69e984D67d3309B811");
    assertEquals(
        resources.get(1).getAbi(),
        "[{\"constant\":false,\"inputs\":[{\"name\":\"n\",\"type\":\"string\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"}]");
  }
}
