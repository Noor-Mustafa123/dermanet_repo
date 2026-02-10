package io.github.nomus.dermanet.Authentication.Controllers;

import javax.naming.spi.DirectoryManager;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import io.github.nomus.dermanet.Authentication.Util.OAuthUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityManagerFactory;
import tools.jackson.databind.JsonNode;



@Log4j2
@NoArgsConstructor
@Getter
@Setter
public class OAuthController extends BaseController {

    /**
     * User Directory DAO Bean
     */
    private DirectoryDAO directoryDAO;

    /**
     * The Entity manager factory Bean.
     */
    private EntityManagerFactory entityManagerFactory;

    /**
     * The Auth Service Bean
     */
    private AuthService authService;

    /**
     * User Directory Manager
     */
    private DirectoryManager directoryManager;

    @Override
    public Response oauthUserDirectoriesList() {
        try {
            List< SuSUserDirectoryDTO > userDirectoryDTOList = directoryManager.getListOfAllOAuthUserDirectories();
            return ResponseUtils.success( userDirectoryDTOList );
        } catch ( final Exception e ) {
            return handleException( e );
        }
    }

    @Override
    public Response oauthAuthorize( UUID directoryId, String stateEndpoint ) {
        Response redirectionEndpoint = null;
        try {

            // gets the userDirectoryEntity with the attributes set
            SuSUserDirectoryEntity suSUserDirectoryEntity = OAuthUtil.getUserDirectoryEntityfromDB( directoryId );
            UserDirectoryAttributeEntity userDirectoryAttributeEntity = suSUserDirectoryEntity.getUserDirectoryAttribute();
            OAuthStateDTO oauthStateDTO = new OAuthStateDTO();
            oauthStateDTO.setDirectoryId( directoryId );
            oauthStateDTO.setStateEndpoint( stateEndpoint );
            oauthStateDTO.setProviderType( userDirectoryAttributeEntity.getProviderType() );

            // condition for OIDC provider
            if ( StringUtils.equals( oauthStateDTO.getProviderType(), OAuthConstants.OIDC ) ) {
                OAuthClientRequest authRequest = OAuthUtil.prepareAuthRequestForOIDCProvider( userDirectoryAttributeEntity, oauthStateDTO );
                redirectionEndpoint = Response.status( 302 ).location( new URI( authRequest.getLocationUri() ) ).build();
            }
            // condition for Non-OIDC provider
            if ( StringUtils.equals( oauthStateDTO.getProviderType(), OAuthConstants.NON_OIDC ) ) {
                OAuthClientRequest authRequest = OAuthUtil.prepareAuthRequestForNonOIDCProvider( userDirectoryAttributeEntity, oauthStateDTO );
                redirectionEndpoint = Response.status( 302 ).location( new URI( authRequest.getLocationUri() ) ).build();
            }

        } catch ( Exception e ) {
            e.printStackTrace();
            return handleException( e );
        }
        if ( redirectionEndpoint == null ) {
            log.warn( "redirectEndpointResponse: " + redirectionEndpoint + "its value is null so no response is being sent" );
            throw new SusException( MessageBundleFactory.getDefaultMessage( Messages.NO_SUCH_DIRECTORY_EXISTS.getKey() ) );
        }
        log.debug( "redirectURIForDebugging: " + redirectionEndpoint.getLocation() );
        return redirectionEndpoint;
    }

    @Override
    public Response oauthAccessToken( String stateJson, String authorization_code, String scope, String authUser, String prompt ) {
        Response redirectionEndpoint = null;
        log.debug( "State for oauthAccessToken : " + stateJson );
        try {
            stateJson = URLDecoder.decode( stateJson, StandardCharsets.UTF_8 );// Decoding
            if ( !OAuthUtil.checkIfStateParameterMatchesInOAuth( stateJson ) ) {
                log.warn( MessageBundleFactory.getDefaultMessage( Messages.STATE_MISMATCH.getKey() ) );
                throw new SusException( MessageBundleFactory.getDefaultMessage( Messages.STATE_MISMATCH.getKey() ) );
            }

            OAuthStateDTO oauthStateDTO = JsonUtils.jsonToObject( stateJson, OAuthStateDTO.class );

            // get tokenEndpoint
            SuSUserDirectoryEntity userDirectoryEntity = OAuthUtil.getUserDirectoryEntityfromDB( oauthStateDTO.getDirectoryId() );
            UserDirectoryAttributeEntity userDirectoryAttributeEntity = userDirectoryEntity.getUserDirectoryAttribute();
            String tokenEndpoint = userDirectoryAttributeEntity.getTokenEndpoint();
            String client_secret = EncryptAndDecryptUtils.decryptString( userDirectoryAttributeEntity.getClientSecret() );
            String redirectUri = userDirectoryAttributeEntity.getRedirectUri();

            // Build the token request using the authorization code
            OAuthAccessTokenResponse accessTokenResponse = OAuthUtil.prepareAndValidateAccessTokenResponse( tokenEndpoint,
                    userDirectoryAttributeEntity, client_secret, redirectUri, authorization_code, oauthStateDTO );

            //*  Retrieve access and refresh token values encoding to avoid path conflicts
            String accessToken = accessTokenResponse.getAccessToken();
            String encodedAccessToken = URLEncoder.encode( accessToken, StandardCharsets.UTF_8 );
            String refreshToken = accessTokenResponse.getRefreshToken();
            String idToken = null;
            if ( StringUtils.equals( oauthStateDTO.getProviderType(), OAuthConstants.OIDC ) ) {
                idToken = accessTokenResponse.getParam( "id_token" );
            }

            // for refresh token to facebook specifically we need to send a second request with to exchange the accessToken with the refreshToken
            if ( userDirectoryEntity.getName() != null && userDirectoryEntity.getName().toLowerCase().contains( "facebook" ) ) {
                // it is using facebook directory type
                JsonNode node = OAuthUtil.exchangeFacebookToken( userDirectoryAttributeEntity.getClientId(),
                        userDirectoryAttributeEntity.getClientSecret(), accessTokenResponse.getAccessToken() );
                log.debug( "facebook Token node: " + node.asText() );
                // changing the value of the refreshToken
                refreshToken = node.get( "access_token" ).asText();
            }
            String encodedIdToken = null;
            String encodedRefreshToken = null;
            if ( !StringUtils.isBlank( refreshToken ) ) {
                encodedRefreshToken = URLEncoder.encode( refreshToken, StandardCharsets.UTF_8 );
            }
            if ( !StringUtils.isBlank( idToken ) ) {
                encodedIdToken = URLEncoder.encode( idToken, StandardCharsets.UTF_8 );
            }

            JsonNode userInfoNOde = OAuthUtil.getUserProfileInfo( accessToken, userDirectoryAttributeEntity );
            log.debug( "JSON_NODE_USER_INFO: " + userInfoNOde.toPrettyString() );

            // Log token retrieval for debugging purpose
            log.debug( "Access token retrieved: " + accessToken );
            log.debug( "Refresh token retrieved: " + ( refreshToken != null ? refreshToken : "Not provided : " + refreshToken ) );
            log.debug( "ID token retrieved: " + ( idToken != null ? idToken : "Non-OIDC Type Provider" ) );

            // Get the qNumber for oidc and non-oidc type providers
            String qNumber = null;
            if ( StringUtils.equals( oauthStateDTO.getProviderType(), OAuthConstants.OIDC ) ) {
                qNumber = userInfoNOde.path( "sub" ).asText();
            } else if ( StringUtils.equals( oauthStateDTO.getProviderType(), OAuthConstants.NON_OIDC ) ) {
                qNumber = userInfoNOde.path( "id" ).asText();
            }
            stateJson = URLEncoder.encode( stateJson, StandardCharsets.UTF_8 ); // Encoding

            // Domain name in the cookies has to be specified because the request comes from the providers domain
            String webBaseUrl = PropertiesManager.getWebBaseURL();
            String domainName = OAuthUtil.extractDomainFromWebBaseUrl( webBaseUrl );

            // making cookies from encoded tokens
            NewCookie accessTokenCookie = new NewCookie( "access_token", encodedAccessToken, "/", domainName, null, 300, true, true );
            NewCookie refreshTokenCookie = new NewCookie( "refresh_token", encodedRefreshToken, "/", domainName, null, 300, true, true );
            NewCookie idTokenCookie = new NewCookie( "id_token", encodedIdToken, "/", domainName, null, 300, true, true );

            OAuthUtil.logCookieDetails( accessTokenCookie );
            OAuthUtil.logCookieDetails( refreshTokenCookie );
            OAuthUtil.logCookieDetails( idTokenCookie );

            // url pointing to the FE
            String url = webBaseUrl + "/loginoauth2/" + qNumber + "/internal-state/" + stateJson;
            log.info( "url:" + url );
            redirectionEndpoint = Response.status( 302 ).location( new URI( url ) )
                    .cookie( accessTokenCookie, refreshTokenCookie, idTokenCookie ).build();
        } catch ( Exception e ) {
            log.error( "Error during OAuth token fetch", e );
            return handleException( e );
        }
        return redirectionEndpoint;
    }



}



