package io.github.nomus.dermanet.Authentication.Util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpEntity;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityManager;
import tools.jackson.databind.JsonNode;

public class OAuthUtil {

    public static String extractDomainFromWebBaseUrl( String url ) {
        // Removing the protocol part http:// or https:// both
        String domain = url.replaceFirst( "^(https?://)", "" );

        // Removing everything after the first slash (if any)
        int slashIndex = domain.indexOf( '/' );
        if ( slashIndex != -1 ) {
            domain = domain.substring( 0, slashIndex );
        }

        return domain;
    }

    public static void logCookieDetails( NewCookie cookie ) {
        log.debug( "Cookie '{}' - Value: {}, Domain: {}, Path: {}, MaxAge: {}, Secure: {}, HttpOnly: {}", cookie.getName(),
                cookie.getValue(), cookie.getDomain(), cookie.getPath(), cookie.getMaxAge(), cookie.isSecure(), cookie.isHttpOnly() );
    }

    public static JsonNode callWellKnownUrl( String wellKnownUrl ) {

        JsonNode wellKnownUrlsJsonNode = null;
        // Request header
        Map< String, String > requestHeaders = new HashMap<>();
        requestHeaders.put( ConstantRequestHeader.CONTENT_TYPE, ConstantRequestHeader.APPLICATION_JSON );
        try {
            // Send request to oauth Provider using SusClient.getExternalRequest()
            CloseableHttpResponse httpResponse = SuSClient.getExternalRequest( wellKnownUrl, requestHeaders );
            HttpEntity responseEntity = httpResponse.getEntity();
            String responseJsonString = EntityUtils.toString( responseEntity, "UTF-8" );
            log.debug( "WELL_KNOWN_URL RESPONSE: " + responseJsonString );
            wellKnownUrlsJsonNode = JsonUtils.toJsonNode( responseJsonString );
        } catch ( Exception e ) {
            throw new SusException( MessageBundleFactory.getDefaultMessage( Messages.COMMUNICATION_FAILURE.getKey() ), e );
        }
        return wellKnownUrlsJsonNode;
    }

    // WILL RETURN THE CURRENT OAUTH_DIRECTORY ATTRIBUTES THAT ARE BEING USED TO SIGN UP USER
    public static SuSUserDirectoryEntity getUserDirectoryEntityfromDB( UUID directoryID ) {
        SuSUserDirectoryEntity responseEntity = null;
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            // Retrieve the OAuth Provider from the db by name
            SuSUserDirectoryEntity suSUserDirectoryEntity = directoryDAO.readDirectory( entityManager, directoryID );
            UserDirectoryAttributeEntity userDirectoryAttributeEntity = suSUserDirectoryEntity.getUserDirectoryAttribute();
            // Check if the type is OIDC then use the wellKnownUrl by default
            if ( StringUtils.equals( userDirectoryAttributeEntity.getProviderType(), OAuthConstants.OIDC ) ) {
                // Call the well-known URL to retrieve the necessary information
                JsonNode wellKnownUrlsJsonNode = callWellKnownUrl( userDirectoryAttributeEntity.getWellKnownUrl() );
                // Loops over the json node field names to get the authorizationEndpoint without case problems for different wellKnownUrl formats
                Iterator< String > fieldNames = wellKnownUrlsJsonNode.fieldNames();
                log.debug( fieldNames );
                while ( fieldNames.hasNext() ) {
                    String fieldName = fieldNames.next();
                    if ( fieldName.equalsIgnoreCase( "authorization_endpoint" ) ) {
                        String authEndpoint = wellKnownUrlsJsonNode.get( fieldName ).asText();
                        userDirectoryAttributeEntity.setAuthorizationEndpoint( authEndpoint );
                        log.debug( "Authorization Endpoint: " + authEndpoint );
                    }
                    if ( fieldName.equalsIgnoreCase( "token_endpoint" ) ) {
                        String tokenEndpoint = wellKnownUrlsJsonNode.get( fieldName ).asText();
                        userDirectoryAttributeEntity.setTokenEndpoint( tokenEndpoint );
                        log.debug( "Token Endpoint: " + tokenEndpoint );
                    }
                    if ( fieldName.equalsIgnoreCase( "userinfo_endpoint" ) ) {
                        String user_info_endpoint = wellKnownUrlsJsonNode.get( fieldName ).asText();
                        userDirectoryAttributeEntity.setUserInfoEndpoint( user_info_endpoint );
                        log.debug( "userInfoEndpoint: " + user_info_endpoint );
                    }
                    if ( fieldName.equalsIgnoreCase( "revocation_endpoint" ) ) {
                        String revocation_endpoint = wellKnownUrlsJsonNode.get( fieldName ).asText();
                        userDirectoryAttributeEntity.setRevocationEndpoint( revocation_endpoint );
                        log.debug( "revocationEndpoint: " + revocation_endpoint );
                    }
                }
            } else if ( StringUtils.equals( userDirectoryAttributeEntity.getProviderType(), OAuthConstants.NON_OIDC ) ) {
                log.debug( "the directory is of type non-oidc" );
            }
            suSUserDirectoryEntity.setUserDirectoryAttribute( userDirectoryAttributeEntity );
            responseEntity = suSUserDirectoryEntity;
        } catch ( Exception e ) {
            handleException( e );
        } finally {
            entityManager.close();
            //! Close the EntityManager
//            if ( entityManager != null && entityManager.isOpen() ) {
//                entityManager.close();
//            }
        }

        // Return user entity with set attributes
        return responseEntity;
    }

    public static JsonNode getUserProfileInfo( String authToken, UserDirectoryAttributeEntity directoryAttributeEntity ) {
        JsonNode jsonNode = null;
        try {
            final Map< String, String > requestHeaders = new HashMap<>();
            requestHeaders.put( "authorization", "Bearer " + authToken );

            CloseableHttpResponse closeableHttpResponse = SuSClient.getExternalRequest( directoryAttributeEntity.getUserInfoEndpoint(),
                    requestHeaders );
            HttpEntity entity = closeableHttpResponse.getEntity();
            String responseString = EntityUtils.toString( entity, "UTF-8" );
            jsonNode = JsonUtils.toJsonNode( responseString );
        } catch ( final Exception e ) {
            throw new SusException( e );
        }
        return jsonNode;
    }

    public static boolean checkIfStateParameterMatchesInOAuth( String stateJson ) {
        //DESERIALIZING STATE JSON
        OAuthStateDTO oauthStateDTO = JsonUtils.jsonToObject( stateJson, OAuthStateDTO.class );
        String state = oauthStateDTO.getState();
        //  checking state for oidc
        if ( StringUtils.isNotBlank( state ) && StringUtils.equals( oauthStateDTO.getProviderType(), OAuthConstants.OIDC ) ) {
            String decryptedString = EncryptAndDecryptUtils.decryptString( state );
            if ( StringUtils.equals( decryptedString, OAuthConstants.STATE_OIDC ) ) {
                return true;
            } else {
                return false;
            }
        }
        //  checking state for non-oidc
        if ( StringUtils.isNotBlank( state ) && StringUtils.equals( oauthStateDTO.getProviderType(), OAuthConstants.NON_OIDC ) ) {
            String decryptedString = EncryptAndDecryptUtils.decryptString( state );
            if ( StringUtils.equals( decryptedString, OAuthConstants.STATE_NON_OIDC ) ) {
                return true;
            } else {
                return false;
            }
        }
//         if no state used
        return true;
    }

    public static static JsonNode exchangeFacebookToken( String appId, String clientSecret, String shortLivedToken ) {
        JsonNode jsonNode = null;
        try {
            String clientSecretDecrypted = EncryptAndDecryptUtils.decryptString( clientSecret );
            //! Build the Facebook token exchange URL as facebook requires exchange of shortlived token for refresh Token
            String fbTokenUrlFormat = PropertiesManager.getInstance().getProperty( ConstantsFileProperties.FACEBOOK_REFRESH_TOKEN_URL );
            String fbUrl = String.format( fbTokenUrlFormat, URLEncoder.encode( appId, StandardCharsets.UTF_8 ),
                    URLEncoder.encode( clientSecretDecrypted, StandardCharsets.UTF_8 ),
                    URLEncoder.encode( shortLivedToken, StandardCharsets.UTF_8 ) );
            // Optionally, set any headers if required (not needed for simple refreshToken request)
            Map< String, String > requestHeaders = new HashMap<>();

            // Perform the GET request using your existing external request method
            CloseableHttpResponse response = SuSClient.getExternalRequest( fbUrl, requestHeaders );
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString( entity, "UTF-8" );

            // Convert the response string into a JSON node
            jsonNode = JsonUtils.toJsonNode( responseString );
        } catch ( Exception e ) {
            throw new SusException( e );
        }
        return jsonNode;
    }

    public static OAuthClientRequest prepareAuthRequestForOIDCProvider( UserDirectoryAttributeEntity userDirectoryAttributeEntity,
            OAuthStateDTO oauthStateDTO ) throws OAuthSystemException {
        String encryptedState = EncryptAndDecryptUtils.encryptString( OAuthConstants.STATE_OIDC );
        oauthStateDTO.setState( encryptedState ); // setting state
        // Create OAuthClientRequest for authorization
        log.debug( "OIDC CONDITION RAN" );
        // ! cant directly write the string in the setRedirectURI because google automatically adds the ""
        String redirectUri = userDirectoryAttributeEntity.getRedirectUri();
        log.debug( "oidc scope" + String.join( " ", userDirectoryAttributeEntity.getScope() ) );
        String clientId = userDirectoryAttributeEntity.getClientId();
        String stateJson = JsonUtils.toJsonString( oauthStateDTO );
        stateJson = URLEncoder.encode( stateJson, StandardCharsets.UTF_8 );// Encoding
        OAuthClientRequest authRequest = OAuthClientRequest.authorizationLocation( userDirectoryAttributeEntity.getAuthorizationEndpoint() )
                .setClientId( clientId ).setRedirectURI( redirectUri ).setResponseType( userDirectoryAttributeEntity.getResponseType() )
                .setScope( String.join( " ", userDirectoryAttributeEntity.getScope() ) ).setState( stateJson )
                .setParameter( "nonce", "nonce_OIDC" )
//                        .setParameter( "prompt", "consent" ) // ! Added for testing as google remembers that consent was already given the first time
//                        .setParameter( "access_type", "offline" ) // ! Added to get a refresh token everytime
                .buildQueryMessage();
        return authRequest;
    }

    public static OAuthClientRequest prepareAuthRequestForNonOIDCProvider( UserDirectoryAttributeEntity userDirectoryAttributeEntity,
            OAuthStateDTO oauthStateDTO ) throws OAuthSystemException {
        String encryptedStateNON = EncryptAndDecryptUtils.encryptString( OAuthConstants.STATE_NON_OIDC );
        oauthStateDTO.setState( encryptedStateNON );
        String stateJson = JsonUtils.toJsonString( oauthStateDTO );
        stateJson = URLEncoder.encode( stateJson, StandardCharsets.UTF_8 );// Encoding
        log.debug( "NON-OIDC CONDITION RAN" );
        String redirectUri = userDirectoryAttributeEntity.getRedirectUri();
        OAuthClientRequest authRequest = OAuthClientRequest.authorizationLocation( userDirectoryAttributeEntity.getAuthorizationEndpoint() )
                .setClientId( userDirectoryAttributeEntity.getClientId() ).setRedirectURI( redirectUri )
                .setResponseType( userDirectoryAttributeEntity.getResponseType() )
                .setScope( String.join( ",", userDirectoryAttributeEntity.getScope() ) ).setState( stateJson )
                .setParameter( "prompt", "login" ) // Force login
                .setParameter( "max_age", "0" ) // Expire session immediately
                .setParameter( "auth_type", "reauthenticate" ) // Force re-authentication
                .buildQueryMessage();
        return authRequest;
    }

    public static OAuthAccessTokenResponse prepareAndValidateAccessTokenResponse( String tokenEndpoint,
            UserDirectoryAttributeEntity userDirectoryAttributeEntity, String clientSecret, String redirectUri, String authorizationCode,
            OAuthStateDTO oauthStateDTO ) throws OAuthSystemException, OAuthProblemException {
        // Initialize the OAuth client using Apache's URLConnectionClient
        OAuthClient oAuthClient = new OAuthClient( new URLConnectionClient() );
        // Build the token request using the authorization code
        OAuthClientRequest accessClientRequest = OAuthClientRequest.tokenLocation( tokenEndpoint )
                .setGrantType( GrantType.AUTHORIZATION_CODE ).setClientId( userDirectoryAttributeEntity.getClientId() )
                .setClientSecret( clientSecret ).setRedirectURI( redirectUri ).setCode( authorizationCode )
                .buildBodyMessage(); // Google requires post request which has a body

        // Request the access token from the OAuth provider
        OAuthAccessTokenResponse accessTokenResponse = oAuthClient.accessToken( accessClientRequest );
        // CHECK FOR FAILURE
        if ( accessTokenResponse.getParam( "error" ) != null ) {
            log.warn( MessageBundleFactory.getDefaultMessage( Messages.AUTHORIZATION_FAILURE.getKey() ) );
            throw new SusException( MessageBundleFactory.getDefaultMessage( Messages.AUTHORIZATION_FAILURE.getKey() ) );
        }

        return accessTokenResponse;

    }
}
