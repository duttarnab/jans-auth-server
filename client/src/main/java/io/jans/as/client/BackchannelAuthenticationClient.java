/*
 * Janssen Project software is available under the Apache License (2004). See http://www.apache.org/licenses/ for full text.
 *
 * Copyright (c) 2020, Janssen Project
 */

package io.jans.as.client;

import io.jans.as.model.common.AuthenticationMethod;
import io.jans.as.model.util.Util;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import javax.ws.rs.HttpMethod;

import static io.jans.as.model.ciba.BackchannelAuthenticationRequestParam.*;
import static io.jans.as.model.ciba.BackchannelAuthenticationResponseParam.*;

/**
 * Encapsulates functionality to make backchannel authentication request calls to an authorization server via REST Services.
 *
 * @author Javier Rojas Blum
 * @version September 4, 2019
 */
public class BackchannelAuthenticationClient extends BaseClient<BackchannelAuthenticationRequest, BackchannelAuthenticationResponse> {

    private static final Logger LOG = Logger.getLogger(BackchannelAuthenticationClient.class);

    /**
     * Constructs a backchannel authentication client by providing a REST url where the
     * backchannel authentication service is located.
     *
     * @param url The REST Service location.
     */
    public BackchannelAuthenticationClient(String url) {
        super(url);
    }

    @Override
    public String getHttpMethod() {
        return HttpMethod.POST;
    }

    /**
     * Executes the call to the REST Service and processes the response.
     *
     * @return The authorization response.
     */
    public BackchannelAuthenticationResponse exec() {
        BackchannelAuthenticationResponse response = null;

        try {
            initClientRequest();
            response = exec_();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        } finally {
            closeConnection();
        }

        return response;
    }

    private BackchannelAuthenticationResponse exec_() throws Exception {
        // Prepare request parameters
        clientRequest.setHttpMethod(getHttpMethod());
        clientRequest.header("Content-Type", request.getContentType());
        if (request.getAuthenticationMethod() == AuthenticationMethod.CLIENT_SECRET_BASIC && request.hasCredentials()) {
            clientRequest.header("Authorization", "Basic " + request.getEncodedCredentials());
        }

        final String scopesAsString = Util.listAsString(getRequest().getScope());
        final String acrValuesAsString = Util.listAsString(getRequest().getAcrValues());

        if (StringUtils.isNotBlank(scopesAsString)) {
            clientRequest.formParameter(SCOPE, scopesAsString);
        }
        if (StringUtils.isNotBlank(getRequest().getClientNotificationToken())) {
            clientRequest.formParameter(CLIENT_NOTIFICATION_TOKEN, getRequest().getClientNotificationToken());
        }
        if (StringUtils.isNotBlank(acrValuesAsString)) {
            clientRequest.formParameter(ACR_VALUES, acrValuesAsString);
        }
        if (StringUtils.isNotBlank(getRequest().getLoginHintToken())) {
            clientRequest.formParameter(LOGIN_HINT_TOKEN, getRequest().getLoginHintToken());
        }
        if (StringUtils.isNotBlank(getRequest().getIdTokenHint())) {
            clientRequest.formParameter(ID_TOKEN_HINT, getRequest().getIdTokenHint());
        }
        if (StringUtils.isNotBlank(getRequest().getLoginHint())) {
            clientRequest.formParameter(LOGIN_HINT, getRequest().getLoginHint());
        }
        if (StringUtils.isNotBlank(getRequest().getBindingMessage())) {
            clientRequest.formParameter(BINDING_MESSAGE, getRequest().getBindingMessage());
        }
        if (StringUtils.isNotBlank(getRequest().getUserCode())) {
            clientRequest.formParameter(USER_CODE, getRequest().getUserCode());
        }
        if (getRequest().getRequestedExpiry() != null) {
            clientRequest.formParameter(REQUESTED_EXPIRY, getRequest().getRequestedExpiry());
        }
        if (StringUtils.isNotBlank(getRequest().getClientId())) {
            clientRequest.formParameter(CLIENT_ID, getRequest().getClientId());
        }
        if (StringUtils.isNotBlank(getRequest().getRequest())) {
            clientRequest.formParameter(REQUEST, getRequest().getRequest());
        }
        if (StringUtils.isNotBlank(getRequest().getRequestUri())) {
            clientRequest.formParameter(REQUEST_URI, getRequest().getRequestUri());
        }
        new ClientAuthnEnabler(clientRequest).exec(getRequest());

        // Call REST Service and handle response
        clientResponse = clientRequest.post(String.class);

        setResponse(new BackchannelAuthenticationResponse(clientResponse));
        String entity = clientResponse.getEntity(String.class);
        getResponse().setEntity(entity);
        getResponse().setHeaders(clientResponse.getMetadata());
        if (StringUtils.isNotBlank(entity)) {
            JSONObject jsonObj = new JSONObject(entity);

            if (jsonObj.has(AUTH_REQ_ID)) {
                getResponse().setAuthReqId(jsonObj.getString(AUTH_REQ_ID));
            }
            if (jsonObj.has(EXPIRES_IN)) {
                getResponse().setExpiresIn(jsonObj.getInt(EXPIRES_IN));
            }
            if (jsonObj.has(INTERVAL)) {
                getResponse().setInterval(jsonObj.getInt(INTERVAL));
            }
        }

        return getResponse();
    }
}