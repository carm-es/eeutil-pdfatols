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

package es.gob.ad.eeutil.eeutilpdfatools.controller;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import es.gob.ad.eeutil.eeutilpdfatools.exception.EeutilPdfaToolsException;
import es.gob.ad.eeutil.eeutilpdfatools.model.ConvertirPDFAModelIn;
import es.gob.ad.eeutil.eeutilpdfatools.model.ConvertirPDFAModelOut;
import es.gob.ad.eeutil.eeutilpdfatools.model.ValidationModelIn;
import es.gob.ad.eeutil.eeutilpdfatools.services.PdfaToolsService;
import es.gob.ad.eeutil.eeutilpdfatools.utils.StringUtil;

@RestController
@PropertySource("file:${eeutil-pdfa-tools.config.path}/pdfatools-config.properties")
public class PdfaToolsController {

  protected static final Log logger = LogFactory.getLog(PdfaToolsController.class);

  @Autowired
  private PdfaToolsService pdfaToolsService;

  @Autowired
  private StringUtil stringUtil;

  @PostMapping(path = "api/ping", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> ping() {
    return ResponseEntity.ok().body("PING");
  }

  @PostMapping(path = "api/validarPDFACompilanceLevel", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Boolean> validarPDFACompilanceLevel(
      @RequestBody ValidationModelIn validationModelIn,
      @RequestHeader("uUId_traza") String uuidTraza) throws EeutilPdfaToolsException {


    boolean bValidado = false;

    byte[] bytesPeticion = validationModelIn.getBytesEntrada();
    // byte[] bytesEncoder = Base64.encodeBase64(bytesPeticion);

    String password = validationModelIn.getPassword();

    // bytesPeticion = processDecryptAsPossiblePDF(bytesPeticion, password);

    boolean estaProtegidoPdf =
        pdfaToolsService.isEncrypted(bytesPeticion, validationModelIn.getPassword());

    // si no esta protegido el pdf ponemos el password a null
    if (!estaProtegidoPdf) {
      validationModelIn.setPassword(null);
    }

    try (InputStream inputStream = new ByteArrayInputStream(bytesPeticion);) {
      bValidado =
          pdfaToolsService.validatePDFACompilanceLevel(inputStream, validationModelIn.getLevel(),
              validationModelIn.getCompilance(), validationModelIn.getPassword());

    } catch (Exception e) {
      logger.error("UUID traza " + uuidTraza + ". " + e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          "UUID traza " + uuidTraza + ". " + e.getMessage(), e);
    }

    return ResponseEntity.ok().body(bValidado);

  }

  @PostMapping(path = "api/validarPDFA", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Boolean> validatePDFA(@RequestBody ValidationModelIn validationModelIn,
      @RequestHeader("uUId_traza") String uuidTraza) throws EeutilPdfaToolsException {

    boolean bValidado = false;

    byte[] bytesPeticion = validationModelIn.getBytesEntrada();
    // byte[] bytesEncoder = Base64.encodeBase64(bytesPeticion);

    String password = validationModelIn.getPassword();

    // bytesPeticion = processDecryptAsPossiblePDF(bytesPeticion, password);

    boolean estaProtegidoPdf =
        pdfaToolsService.isEncrypted(bytesPeticion, validationModelIn.getPassword());

    // si no esta protegido el pdf ponemos el password a null
    if (!estaProtegidoPdf) {
      validationModelIn.setPassword(null);
    }

    try (InputStream inputStream = new ByteArrayInputStream(bytesPeticion);) {

      if (!stringUtil.esVacioOrNull(validationModelIn.getCompilance())
          && validationModelIn.getLevel() != null) {
        bValidado = pdfaToolsService.validatePDFACompilanceLevel(inputStream,
            validationModelIn.getLevel(), validationModelIn.getCompilance(), null);
      } else {
        bValidado = pdfaToolsService.validatePDFA(inputStream, validationModelIn.getPassword());
      }
    } catch (Exception e) {
      logger.error("UUID traza " + uuidTraza + ". " + e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          "UUID traza " + uuidTraza + ". " + e.getMessage(), e);
    }

    return ResponseEntity.ok().body(bValidado);

  }

  @PostMapping(path = "api/convertirPDFA", consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ConvertirPDFAModelOut> convertirPDFA(
      @RequestBody ConvertirPDFAModelIn convertirPDFAModelIn,
      @RequestHeader("uUId_traza") String uuidTraza) throws EeutilPdfaToolsException {

    ConvertirPDFAModelOut resultado = null;

    try {

      byte[] contenido = convertirPDFAModelIn.getContenido();

      String password = convertirPDFAModelIn.getPassword();

      boolean estaProtegidoPdf = pdfaToolsService.isEncrypted(contenido, password);

      // si no esta protegido el pdf ponemos el password a null
      if (!estaProtegidoPdf) {
        convertirPDFAModelIn.setPassword(null);
      }
      resultado = pdfaToolsService.convertirPDFA(convertirPDFAModelIn.getLevel(),
          convertirPDFAModelIn.getContenido(), convertirPDFAModelIn.getPassword());

    } catch (Exception e) {
      logger.error("UUID traza " + uuidTraza + ". " + e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          "UUID traza " + uuidTraza + ". " + e.getMessage(), e);
    }

    return ResponseEntity.ok().body(resultado);

    // return null;

  }

  private byte[] processDecryptAsPossiblePDF(byte[] bytePeticionOriginal, String password)
      throws EeutilPdfaToolsException {
    boolean estaProtegido = false;

    byte[] bytesResultado = null;

    if (!stringUtil.esVacioOrNull(password)) {
      estaProtegido = pdfaToolsService.isEncrypted(bytePeticionOriginal, password);

      if (estaProtegido) {
        bytesResultado = pdfaToolsService.cloneUnlkPdf(bytePeticionOriginal, password);
      }
    } else {
      bytesResultado = bytePeticionOriginal;
    }

    return bytesResultado;
  }

}
