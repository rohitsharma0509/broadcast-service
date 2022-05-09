package com.scb.rider.broadcast.config.security;

import com.auth0.jwk.GuavaCachedJwkProvider;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AwsCognitoRSAOpsKeyProvider implements RSAKeyProvider {

  private final JwkProvider provider;


  public AwsCognitoRSAOpsKeyProvider(@Value("${cognito.ops.url}") String cognitoUrl) {
    provider = new GuavaCachedJwkProvider(new JwkProviderBuilder(cognitoUrl).build());
  }

  @Override
  public RSAPublicKey getPublicKeyById(String kid) {
    try {
      return (RSAPublicKey) provider.get(kid).getPublicKey();
    } catch (JwkException e) {
      throw new RuntimeException(String.format("Failed to get JWT kid=%s from aws_kid_store_url=%s", kid, null));
    }
  }

  @Override
  public RSAPrivateKey getPrivateKey() {
    return null;
  }

  @Override
  public String getPrivateKeyId() {
    return null;
  }
}
