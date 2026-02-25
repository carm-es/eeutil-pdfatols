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

public class ValidationModelIn {

  @JsonProperty("bytesEntrada")
  private byte[] bytesEntrada;

  @JsonProperty("level")
  private Integer level;

  @JsonProperty("compilance")
  private String compilance;

  @JsonProperty("password")
  private String password;

  public ValidationModelIn() {
    super();
  }

  public ValidationModelIn(byte[] bytesEntrada, Integer level, String compilance, String password) {
    super();
    this.bytesEntrada = bytesEntrada;
    this.level = level;
    this.compilance = compilance;
    this.password = password;
  }


  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public byte[] getBytesEntrada() {
    return bytesEntrada;
  }


  public void setBytesEntrada(byte[] bytesEntrada) {
    this.bytesEntrada = bytesEntrada;
  }


  public Integer getLevel() {
    return level;
  }


  public void setLevel(Integer level) {
    this.level = level;
  }


  public String getCompilance() {
    return compilance;
  }


  public void setCompilance(String compilance) {
    this.compilance = compilance;
  }



}
