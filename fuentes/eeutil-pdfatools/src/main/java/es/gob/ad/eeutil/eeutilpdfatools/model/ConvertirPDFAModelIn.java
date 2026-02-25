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

public class ConvertirPDFAModelIn {

  @JsonProperty("contenido")
  private byte[] contenido;

  @JsonProperty("level")
  private Integer level;

  @JsonProperty("password")
  private String password;

  public byte[] getContenido() {
    return contenido;
  }

  public void setContenido(byte[] contenido) {
    this.contenido = contenido;
  }

  public Integer getLevel() {
    return level;
  }

  public void setLevel(Integer level) {
    this.level = level;
  }



  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public ConvertirPDFAModelIn(byte[] contenido, Integer level, String password) {
    super();
    this.contenido = contenido;
    this.level = level;
    this.password = password;
  }

  public ConvertirPDFAModelIn() {
    super();
  }



}
