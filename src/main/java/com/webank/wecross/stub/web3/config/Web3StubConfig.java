package com.webank.wecross.stub.web3.config;

import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.stub.web3.common.Web3Constant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Web3StubConfig {
  private static final Logger logger = LoggerFactory.getLogger(Web3StubConfig.class);

  private Common common;
  private Service service;
  private List<Resource> resources;

  public List<ResourceInfo> convertToResourceInfos() {
    List<ResourceInfo> resourceInfos = new ArrayList<>();
    for (Resource resource : this.getResources()) {
      ResourceInfo resourceInfo = new ResourceInfo();
      resourceInfo.setName(resource.getName());
      resourceInfo.setStubType(this.getCommon().getType());

      Map<Object, Object> properties = resourceInfo.getProperties();
      properties.put(resource.getName(), resource.getAddress());
      properties.put(resource.getName() + Web3Constant.WEB3_PROPERTY_ABI_SUFFIX, resource.getAbi());
      resourceInfos.add(resourceInfo);
    }
    return resourceInfos;
  }

  public static class Common {
    private String name;
    private String type;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    @Override
    public String toString() {
      return "Common{" + "name='" + name + '\'' + ", type='" + type + '\'' + '}';
    }
  }

  public static class Service {
    private String url;

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }
  }

  public static class Resource {
    private String name;
    private String type;
    private String address;
    private String abi;

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getAddress() {
      return address;
    }

    public void setAddress(String address) {
      this.address = address;
    }

    public String getAbi() {
      return abi;
    }

    public void setAbi(String abi) {
      this.abi = abi;
    }

    @Override
    public String toString() {
      return "Resource{"
          + "type='"
          + type
          + '\''
          + ", name='"
          + name
          + '\''
          + ", address='"
          + address
          + '\''
          + ", abi='"
          + abi
          + '\''
          + '}';
    }
  }

  public Common getCommon() {
    return common;
  }

  public void setCommon(Common common) {
    this.common = common;
  }

  public Service getService() {
    return service;
  }

  public void setService(Service service) {
    this.service = service;
  }

  public List<Resource> getResources() {
    return resources;
  }

  public void setResources(List<Resource> resources) {
    this.resources = resources;
  }

  @Override
  public String toString() {
    return "Web3StubConfig{"
        + "common="
        + common
        + ", service="
        + service
        + ", resources="
        + resources
        + '}';
  }
}
