/*
 * Copyright (c) 2019, Okta, Inc. and/or its affiliates. All rights reserved.
 * The Okta software accompanied by this notice is provided pursuant to the Apache License,
 * Version 2.0 (the "License.")
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under the
 * License.
 */
package com.okta.oidc.util;

import android.net.Uri;

import com.okta.oidc.AuthenticateClient;
import com.okta.oidc.OIDCAccount;
import com.okta.oidc.net.request.HttpRequest;
import com.okta.oidc.net.request.HttpRequestBuilder;
import com.okta.oidc.net.request.ProviderConfiguration;
import com.okta.oidc.net.request.TokenRequest;
import com.okta.oidc.net.request.web.AuthorizeRequest;
import com.okta.oidc.net.response.web.AuthorizeResponse;
import com.okta.oidc.net.response.web.LogoutResponse;

import java.security.KeyPair;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class TestValues {
    public static final String CUSTOM_URL = "https://com.okta.test/";
    public static final String CUSTOM_STATE = "CUSTOM_STATE";
    public static final String CUSTOM_NONCE = "CUSTOM_NONCE";
    public static final String CUSTOM_CODE = "CUSTOM_CODE";
    public static final String LOGIN_HINT = "LOGIN_HINT";
    public static final String CLIENT_ID = "CLIENT_ID";
    public static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    public static final String ID_TOKEN = "ID_TOKEN";
    public static final String REFRESH_TOKEN = "REFRESH_TOKEN";
    public static final String CUSTOM_USER_AGENT = "CUSTOM_USER_AGENT";

    public static final String REDIRECT_URI = CUSTOM_URL + "callback";
    public static final String END_SESSION_URI = CUSTOM_URL + "logout";
    public static final String[] SCOPES = {"openid", "profile", "offline_access"};

    public static final String REVOCATION_ENDPOINT = "revoke";
    public static final String AUTHORIZATION_ENDPOINT = "authorize";
    public static final String TOKEN_ENDPOINT = "token";
    public static final String END_SESSION_ENDPOINT = "logout";
    public static final String USERINFO_ENDPOINT = "userinfo";

    public static final String ERROR = "error";
    public static final String ERROR_DESCRIPTION = "error_description";


    //Token response
    public static final String TYPE_BEARER = "Bearer";
    public static final String EXPIRES_IN = "3600";

    public static OIDCAccount getAccountWithUrl(String url) {
        return new OIDCAccount.Builder()
                .clientId(CLIENT_ID)
                .redirectUri(REDIRECT_URI)
                .endSessionRedirectUri(END_SESSION_URI)
                .scopes(SCOPES)
                .discoveryUri(url)
                .create();
    }

    public static ProviderConfiguration getProviderConfiguration(String url) {
        ProviderConfiguration configuration = new ProviderConfiguration();
        configuration.issuer = url;
        configuration.revocation_endpoint = url + REVOCATION_ENDPOINT;
        configuration.authorization_endpoint = url + AUTHORIZATION_ENDPOINT;
        configuration.token_endpoint = url + TOKEN_ENDPOINT;
        configuration.end_session_endpoint = url + END_SESSION_ENDPOINT;
        configuration.userinfo_endpoint = url + USERINFO_ENDPOINT;
        return configuration;
    }

    public static String getJwt(String issuer, String... audience) {
        return getJwt(issuer, DateUtil.getTomorrow(), DateUtil.getNow(), audience);
    }

    public static String getExpiredJwt(String issuer, String... audience) {
        return getJwt(issuer, DateUtil.getYesterday(), DateUtil.getNow(), audience);
    }

    public static String getJwtIssuedAtTimeout(String issuer, String... audience) {
        return getJwt(issuer, DateUtil.getExpiredFromTomorrow(), DateUtil.getTomorrow(), audience);
    }

    public static String getJwt(String issuer, Date expiredDate, Date issuedAt,
                                String... audience) {
        JwtBuilder builder = Jwts.builder();
        KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);
        Map<String, Object> map = new HashMap<>();
        map.put(Claims.AUDIENCE, Arrays.asList(audience));

        return builder
                .addClaims(map)
                .setIssuer(issuer)
                .setSubject("sub")
                .setExpiration(expiredDate)
                .setIssuedAt(issuedAt)
                .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
                .compact();
    }

    public static AuthorizeRequest getAuthorizeRequest(OIDCAccount account, String verifier) {
        return new AuthorizeRequest.Builder().codeVerifier(verifier)
                .authorizeEndpoint(account.getDiscoveryUri().toString())
                .redirectUri(account.getRedirectUri().toString())
                .scope(SCOPES)
                .nonce(CUSTOM_NONCE)
                .state(CUSTOM_STATE)
                .create();
    }

    public static AuthorizeResponse getAuthorizeResponse(String state, String code) {
        String uri = String.format("com.okta.test:/callback?code=%s&state=%s", code, state);
        return AuthorizeResponse.fromUri(Uri.parse(uri));
    }

    public static AuthorizeResponse getInvalidAuthorizeResponse(String error, String desc) {
        String uri = String.format("com.okta.test:/callback?error=%s&error_description=%s"
                , error, desc);
        return AuthorizeResponse.fromUri(Uri.parse(uri));
    }

    public static LogoutResponse getLogoutResponse(String state) {
        String uri = String.format("com.okta.test:/logout?state=%s", state);
        return LogoutResponse.fromUri(Uri.parse(uri));
    }

    public static TokenRequest getTokenRequest(OIDCAccount account, AuthorizeRequest request,
                                               AuthorizeResponse response) {
        return (TokenRequest) HttpRequestBuilder.newRequest()
                .request(HttpRequest.Type.TOKEN_EXCHANGE)
                .authRequest(request)
                .authResponse(response)
                .account(account)
                .createRequest();
    }
}
