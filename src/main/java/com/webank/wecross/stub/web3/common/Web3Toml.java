package com.webank.wecross.stub.web3.common;

import com.moandjiezana.toml.Toml;
import java.io.IOException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class Web3Toml {
  private final String path;

  public Web3Toml(String path) {
    this.path = path;
  }

  public String getPath() {
    return path;
  }

  public Toml getToml() throws IOException {
    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    Resource resource = resolver.getResource(getPath());
    return new Toml().read(resource.getInputStream());
  }
}
