package no.nav.pensjondokdist.oidc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

@RunWith(MockitoJUnitRunner.class)
public class OidcProviderTest {
    private OidcProvider oidcProvider;
    private static final String URL = "http://OidcUrl.com";
    /* JWT-body
  {
       "at_hash": "8w_KiL4KeIzEEjNFvWOBZg",
       "sub": "Z992960",
       "auditTrackingId": "b9b83b77-a1f3-4130-816b-0199e8db7024-26057236",
       "iss": "https://isso-q.adeo.no:443/isso/oauth2",
       "tokenName": "id_token",
       "nonce": "fimbSJJTS4HJDspFfdF4TCXveRKBniF60pybHD7Hl_A",
       "aud": "pensjon-dokdist-localhost",
       "c_hash": "p1z80r2R6nUZMp2WHQghqA",
       "org.forgerock.openidconnect.ops": "d4ce8cb7-bde6-4a1b-b249-5b60a336e9ab",
       "azp": "pensjon-dokdist-localhost",
       "auth_time": 1590565340,
       "realm": "/",
       "exp": 3600568940,
       "tokenType": "JWTToken",
       "iat": 1590565340
   }
   */
    private static final String TOKEN_2084 = "eyJ0eXAiOiJKV1QiLCJraWQiOiIxbDJKa0NvVEwxMGJtZUF4eWxnNHhSaThqMlk9IiwiYWxnIjoiSFM1MTIifQ.eyJhdF9oYXNoIjoiOHdfS2lMNEtlSXpFRWpORnZXT0JaZyIsInN1YiI6Ilo5OTI5NjAiLCJhdWRpdFRyYWNraW5nSWQiOiJiOWI4M2I3Ny1hMWYzLTQxMzAtODE2Yi0wMTk5ZThkYjcwMjQtMjYwNTcyMzYiLCJpc3MiOiJodHRwczovL2lzc28tcS5hZGVvLm5vOjQ0My9pc3NvL29hdXRoMiIsInRva2VuTmFtZSI6ImlkX3Rva2VuIiwibm9uY2UiOiJmaW1iU0pKVFM0SEpEc3BGZmRGNFRDWHZlUktCbmlGNjBweWJIRDdIbF9BIiwiYXVkIjoicGVuc2pvbi1kb2tkaXN0LWxvY2FsaG9zdCIsImNfaGFzaCI6InAxejgwcjJSNm5VWk1wMldIUWdocUEiLCJvcmcuZm9yZ2Vyb2NrLm9wZW5pZGNvbm5lY3Qub3BzIjoiZDRjZThjYjctYmRlNi00YTFiLWIyNDktNWI2MGEzMzZlOWFiIiwiYXpwIjoicGVuc2pvbi1kb2tkaXN0LWxvY2FsaG9zdCIsImF1dGhfdGltZSI6MTU5MDU2NTM0MCwicmVhbG0iOiIvIiwiZXhwIjozNjAwNTY4OTQwLCJ0b2tlblR5cGUiOiJKV1RUb2tlbiIsImlhdCI6MTU5MDU2NTM0MH0.sT8hzfpYmhFKAAN-7OBTUeQdrzIos4YcOSyQ6uAViNG-J9_b-PmIrttqdh8lLHMDZ_nm5Y8N8SYzCmHo-y97Ug";

    @Mock
    private OAuth2AuthorizedClientService authorizedClientService;

    @Before
    public void init() {
        oidcProvider = new OidcProvider(URL, authorizedClientService);
        SecurityContext securityContext = mock(SecurityContext.class);
        OAuth2AuthenticationToken authentication = mock(OAuth2AuthenticationToken.class);
        OidcUser principal = mock(OidcUser.class);
        OidcIdToken oidcIdToken = mock(OidcIdToken.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(principal.getIdToken()).thenReturn(oidcIdToken);
        when(oidcIdToken.getTokenValue()).thenReturn(TOKEN_2084);

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void shouldNotRefreshTokenIfTokenIsValid() {
        String token  = oidcProvider.getIdToken();
        assertThat(token).isEqualTo(TOKEN_2084);
    }

}
