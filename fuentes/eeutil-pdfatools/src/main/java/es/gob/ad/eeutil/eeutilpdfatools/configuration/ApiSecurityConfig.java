/*
 * Copyright (C) 2025, Gobierno de España This program is licensed and may be used, modified and
 * redistributed under the terms of the European Public License (EUPL), either version 1.1 or (at
 * your option) any later version as soon as they are approved by the European Commission. Unless
 * required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing permissions and more details. You
 * should have received a copy of the EUPL1.1 license along with this program; if not, you may find
 * it at http://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 */

package es.gob.ad.eeutil.eeutilpdfatools.configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import es.gob.ad.eeutil.eeutilpdfatools.filter.APIKeyAuthFilter;


@Configuration
@PropertySource("file:${eeutil-pdfa-tools.config.path}/credential.properties")
@EnableWebSecurity
@Order(1)
public class ApiSecurityConfig extends WebSecurityConfigurerAdapter {

  protected static final Log logger = LogFactory.getLog(ApiSecurityConfig.class);

  @Value("${eeutil-pdfa-tools.http.auth-token-header-name}")
  private String principalRequestHeader;

  @Value("${eeutil-pdfa-tools.http.auth-token}")
  private String principalRequestValue;

  @Override
  protected void configure(HttpSecurity httpSecurity) throws Exception {
    APIKeyAuthFilter filter = new APIKeyAuthFilter(principalRequestHeader);
    filter.setAuthenticationManager(new AuthenticationManager() {

      @Override
      public Authentication authenticate(Authentication authentication)
          throws AuthenticationException {
        String principal = (String) authentication.getPrincipal();
        if (!principalRequestValue.equals(principal)) {
          throw new BadCredentialsException("The API key was not found or not the expected value.");
        }
        authentication.setAuthenticated(true);
        return authentication;
      }

    });
    httpSecurity.antMatcher("/eeutilpdfatools/**").csrf(csrf -> csrf.disable())
        .sessionManagement(
            management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilter(filter).authorizeHttpRequests(requests -> requests.anyRequest().authenticated());
  }

}
