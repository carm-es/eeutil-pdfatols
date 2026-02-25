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

package es.gob.ad.eeutil.eeutilpdfatools.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MDCWrapper {

  @JsonProperty("idApli")
  private String idApli;

  @JsonProperty("uUId")
  private String uUId;

  @JsonProperty("ipClient")
  private String ipClient;

  @JsonProperty("clientHost")
  private String clientHost;

  @JsonProperty("clientURI")
  private String clientURI;

  @JsonProperty("contentLengh")
  private String contentLengh;

  @JsonProperty("ExtraParaM")
  private String ExtraParaM;

  public String getIdApli() {
    return idApli;
  }

  public void setIdApli(String idApli) {
    this.idApli = idApli;
  }

  public String getuUId() {
    return uUId;
  }

  public void setuUId(String uUId) {
    this.uUId = uUId;
  }

  public String getIpClient() {
    return ipClient;
  }

  public void setIpClient(String ipClient) {
    this.ipClient = ipClient;
  }

  public String getClientHost() {
    return clientHost;
  }

  public void setClientHost(String clientHost) {
    this.clientHost = clientHost;
  }

  public String getClientURI() {
    return clientURI;
  }

  public void setClientURI(String clientURI) {
    this.clientURI = clientURI;
  }

  public String getContentLengh() {
    return contentLengh;
  }

  public void setContentLengh(String contentLengh) {
    this.contentLengh = contentLengh;
  }

  public String getExtraParaM() {
    return ExtraParaM;
  }

  public void setExtraParaM(String extraParaM) {
    ExtraParaM = extraParaM;
  }

  public MDCWrapper(String idApli, String uUId, String ipClient, String clientHost,
      String clientURI, String contentLengh, String extraParaM) {
    super();
    this.idApli = idApli;
    this.uUId = uUId;
    this.ipClient = ipClient;
    this.clientHost = clientHost;
    this.clientURI = clientURI;
    this.contentLengh = contentLengh;
    ExtraParaM = extraParaM;
  }

  public MDCWrapper() {
    super();
  }



}
