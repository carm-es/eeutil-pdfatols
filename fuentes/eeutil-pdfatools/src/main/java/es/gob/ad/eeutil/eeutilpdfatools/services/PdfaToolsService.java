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

package es.gob.ad.eeutil.eeutilpdfatools.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.verapdf.core.EncryptedPdfException;
import org.verapdf.core.ModelParsingException;
import org.verapdf.core.ValidationException;
import org.verapdf.pdfa.Foundries;
import org.verapdf.pdfa.PDFAParser;
import org.verapdf.pdfa.PDFAValidator;
import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.results.ValidationResult;
import org.verapdf.pdfa.validation.profiles.RuleId;

import com.lowagie.text.exceptions.BadPasswordException;
import com.lowagie.text.pdf.PdfEncryptor;
import com.lowagie.text.pdf.PdfReader;

import es.gob.ad.eeutil.eeutilpdfatools.exception.EeutilPdfaToolsException;
import es.gob.ad.eeutil.eeutilpdfatools.model.ConvertirPDFAModelOut;
import es.gob.ad.eeutil.eeutilpdfatools.utils.FileUtil;
import es.gob.ad.eeutil.eeutilpdfatools.utils.StringUtil;

@Service
@PropertySource("file:${eeutil-pdfa-tools.config.path}/pdfatools-config.properties")
public class PdfaToolsService {

  protected static final Log logger = LogFactory.getLog(PdfaToolsService.class);

  public final static String PDFA_CONV_PREFIX = "PDFAConv-";

  public final static String PDFA_CLONE_PREFIX = "PDFAClone-";

  @Autowired
  public FileUtil fileUtil;

  @Autowired
  public StringUtil stringUtil;

  public boolean validatePDFACompilanceLevel(InputStream inputFilePdfa, Integer level,
      String compilance, String password) throws EeutilPdfaToolsException {
    return validatePDFALevelCompilanceBusiness(level, compilance, inputFilePdfa, password);
  }

  public boolean validatePDFA(InputStream inputFilePdfa, String password)
      throws EeutilPdfaToolsException {
    return validatePDFABusiness(inputFilePdfa, password);
  }

  boolean validatePDFALevelCompilanceBusiness(Integer level, String compilance,
      InputStream inputStream, String password) throws EeutilPdfaToolsException {

    // VeraGreenfieldFoundryProvider.initialise();

    boolean bresultado = false;

    String levelCompilance = String.valueOf(level) + compilance;
    PDFAFlavour flavour = PDFAFlavour.fromString(levelCompilance);
    try (
        PDFAParser parser =
            Foundries.defaultInstance().createParser(inputStream, flavour, password);
        PDFAValidator validator = Foundries.defaultInstance().createValidator(flavour, false);) {
      ValidationResult result = validator.validate(parser);
      if (result.isCompliant()) {
        logger.info("El fichero es valido para la compilacion PDFA/" + result.getPDFAFlavour());
        bresultado = true;
      } else {
        logger.error("El fichero no es valido para la compilacion PDFA/" + levelCompilance);
        for (Map.Entry<RuleId, Integer> oError : result.getFailedChecks().entrySet()) {
          RuleId ruleId = oError.getKey();

          logger.error("Error encontrado Regla: " + ruleId.getSpecification().toString()
              + " clausula" + ruleId.getClause() + " " + oError.getValue());
        }
      }
    } catch (IOException | ValidationException | ModelParsingException
        | EncryptedPdfException exception) {
      throw new EeutilPdfaToolsException(exception.getMessage(), exception);
    }

    return bresultado;
  }

  // If you are not sure what specification to use you can let the software
  // decide:
  boolean validatePDFABusiness(InputStream inputStream, String password)
      throws EeutilPdfaToolsException {

    // VeraGreenfieldFoundryProvider.initialise();

    boolean bresultado = false;

    if (inputStream == null)
      throw new EeutilPdfaToolsException(
          "Error al procesar el fichero PDFA, el stream no puede ser nulo");

    PDFAParser parser = null;
    PDFAValidator validator = null;

    try {
      if (!stringUtil.esVacioOrNull(password)) {
        parser = Foundries.defaultInstance().createParser(inputStream, PDFAFlavour.fromString("2b"),
            password);
      } else {
        parser = Foundries.defaultInstance().createParser(inputStream);
      }


      validator = Foundries.defaultInstance().createValidator(parser.getFlavour(), false);
      ValidationResult result = validator.validate(parser);
      if (result.isCompliant()) {
        bresultado = true;
        System.out
            .println("El fichero es valido para la compilacion PDFA/" + result.getPDFAFlavour());
      } else {
        // it isn't
        // System.err.println(result.getPDFAFlavour().toString());
        System.err.println("El fichero no es valido para la compilacion PDFA/"
            + result.getPDFAFlavour().toString());
        for (Map.Entry<RuleId, Integer> oError : result.getFailedChecks().entrySet()) {
          RuleId ruleId = oError.getKey();

          logger.error("Error encontrado Regla: " + ruleId.getSpecification().toString()
              + " clausula" + ruleId.getClause() + " " + oError.getValue());
        }
      }
    } catch (ValidationException | ModelParsingException | EncryptedPdfException exception) {
      throw new EeutilPdfaToolsException(exception.getMessage(), exception);
    } finally {
      try {
        if (validator != null)
          validator.close();
        if (parser != null)
          parser.close();
      } catch (IOException e) {
        throw new EeutilPdfaToolsException(e.getMessage(), e);
      }
    }

    return bresultado;
  }

  public ConvertirPDFAModelOut convertirPDFA(Integer level, byte[] contenidoPDF, String password)
      throws EeutilPdfaToolsException {
    Process process = null;
    String pathIn = null;
    String pathOut = null;
    String pathLog = null;
    // guarda en temporales el fichero de entrada
    File fileIn = null;
    File fileOut = null;
    File fileLog = null;

    ConvertirPDFAModelOut oConvRes = null;

    boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

    try {

      pathIn = fileUtil.createFilePath(PDFA_CONV_PREFIX, contenidoPDF);
      pathOut = new StringBuilder(pathIn).append(".out").toString();
      pathLog = new StringBuilder(pathIn).append(".log").toString();
      // guarda en temporales el fichero de entrada
      fileIn = new File(pathIn);
      // escribimos el fichero en temp
      // FileUtils.writeByteArrayToFile(new File(pathIn), Base64.decodeBase64(contenidoPDF));
      if (isWindows) {
        if (!stringUtil.esVacioOrNull(password)) {

          String cmd = String.format(
              "gswin64c -dBATCH -dNOPROMPT -dPDFA=%s -dNOPAUSE -dNOSAFER -dNOOUTERSAVE -sProcessColorModel=DeviceRGB -sColorConversionStrategy=RGB -dAutoRotatePages=/None -sDEVICE=pdfwrite -sPDFPassword=%s"
                  + " -sOwnerPassword=%s" + " -sUserPassword=%s"
                  + " -o %s PDFA_def.ps -dPDFACompatibilityPolicy=1 %s",
              String.valueOf(level), password, password, password, pathOut, pathIn);
          process = Runtime.getRuntime().exec(cmd);
        } else {
          String cmd = String.format(
              "gswin64c -dBATCH -dNOPROMPT -dPDFA=%s -dNOPAUSE -dNOSAFER -dNOOUTERSAVE -sProcessColorModel=DeviceRGB -sColorConversionStrategy=RGB -dAutoRotatePages=/None -sDEVICE=pdfwrite -o %s PDFA_def.ps -dPDFACompatibilityPolicy=1 %s",
              String.valueOf(level), pathOut, pathIn);
          process = Runtime.getRuntime().exec(cmd);
        }
        logger.info("Waiting for conversion file ...");
        waitProcessTerminar(process, pathLog, level);
        if (!Files.exists(Paths.get(pathOut))) {
          throw new EeutilPdfaToolsException(
              "El fichero de salida en la conversion pdfa: " + pathOut + " no existe");
        }
        // waitProcessWorkerTerminar(process);
        logger.info("Conversion file file done.");

        logger.info("EJECUTADO EN WINDOWS");

      } else {
        // en linux no le damos la responsabildad al bash a ejecutar.
        if (!stringUtil.esVacioOrNull(password)) {
          String cmd = String.format(
              "gs -dBATCH -dNOPROMPT -dPDFA=%s -dNOPAUSE -dNOSAFER -dNOOUTERSAVE -sProcessColorModel=DeviceRGB -sColorConversionStrategy=RGB -dAutoRotatePages=/None -sDEVICE=pdfwrite -sPDFPassword=%s"
                  + " -sOwnerPassword=%s" + " -sUserPassword=%s"
                  + " -o %s PDFA_def.ps -dPDFACompatibilityPolicy=1 %s",
              String.valueOf(level), password, password, password, pathOut, pathIn);
          process = Runtime.getRuntime().exec(cmd);
        } else {
          String cmd = String.format(
              "gs -dBATCH -dNOPROMPT -dPDFA=%s -dNOPAUSE -dNOSAFER -dNOOUTERSAVE -sProcessColorModel=DeviceRGB -sColorConversionStrategy=RGB -dAutoRotatePages=/None -sDEVICE=pdfwrite -o %s PDFA_def.ps -dPDFACompatibilityPolicy=1 %s",
              String.valueOf(level), pathOut, pathIn);
          process = Runtime.getRuntime().exec(cmd);
        }
        logger.info("Waiting for conversion file ...");
        waitProcessTerminar(process, pathLog, level);
        // waitProcessWorkerTerminar(process);
        if (!Files.exists(Paths.get(pathOut))) {
          throw new EeutilPdfaToolsException(
              "El fichero de salida en la conversion pdfa: " + pathOut + " no existe");
        }
        logger.info("Conversion file done.");

        logger.info("EJECUTADO EN LINUX");
      }

      // fichero de salida (presunto fichero pdfa
      fileOut = new File(pathOut);
      // fichero de log (para verificar si la conversion se ha realizado
      fileLog = new File(pathLog);

      // if (!fileLog.exists()) {
      // throw new EeutilPdfaToolsException("Error al generar el fichero de log " + pathLog);
      // }

      byte[] arrResultado = Files.readAllBytes(fileOut.toPath());

      // vamos a proteger el fichero.
      // arrResultado = cloneLckPdf(arrResultado, password);

      oConvRes = new ConvertirPDFAModelOut(arrResultado, "application/pdf");

    } catch (Exception e) {
      throw new EeutilPdfaToolsException(e.getMessage(), e);
    } finally {
      if (process.isAlive()) {
        // si despues de la excepcion sigue vivo lo forzamos a cerrar
        process.destroyForcibly();
      }
      // borramos el fichero de entrada
      if (fileIn != null && fileIn.exists()) {
        fileIn.delete();
      }
      // borramos el fichero de salida
      if (fileOut != null && fileOut.exists()) {
        fileOut.delete();
      }
      // borramos el fichero log
      if (fileLog != null && fileLog.exists()) {
        fileLog.delete();
      }

    }

    return oConvRes;

  }

  /**
   * Metodo para ejecutar un proceso con un worker gestionando (OPCION A)
   * 
   * @param process
   * @return
   * @throws TimeoutException
   * @throws InterruptedException
   */
  private boolean waitProcessWorkerTerminar(Process process)
      throws TimeoutException, InterruptedException {
    WorkerProcessConvert worker = new WorkerProcessConvert(process);
    worker.start();
    try {
      // timeout 120 sg
      worker.join(120000);
      if (worker.isbExit())
        return worker.isbExit();
      else
        throw new TimeoutException();
    } catch (InterruptedException ex) {
      worker.interrupt();
      // Thread.currentThread().interrupt();
      throw ex;
    } finally {
      process.destroyForcibly();
    }
  }

  /**
   * Metodo para ejecutar un proceso sin worker gestionado (OPCION B)
   * 
   * @param process
   * @throws InterruptedException
   * @throws IOException
   */
  private void waitProcessTerminar(Process process, String pathLog, Integer level)
      throws InterruptedException, IOException, EeutilPdfaToolsException {

    if (process.isAlive()) {
      if (!process.waitFor(120, TimeUnit.SECONDS)) {
        // forzamos el cierre del proceso al minuto
        process.destroyForcibly();
      } else {
        logger.info("Proceso de conversion terminado correctamente");
      }
    } else {

    }

    String trazaError = null;

    // Aqui el proceso ya ha terminado, pero sacamos el fichero de log.
    try (InputStream stream = process.getErrorStream();) {
      // FileUtils.copyInputStreamToFile(stream, new File(pathLog));


      // String trazaError = IOUtils.toString(stream, "UTF-8");


      try (BufferedReader buffReader =
          new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));) {

        trazaError = buffReader.lines().collect(Collectors.joining(System.lineSeparator()));
      }

      if (trazaError != null && trazaError.contains("reverting to normal PDF output")) {
        throw new EeutilPdfaToolsException(
            "Error al convertir el fichero origen a pdfa " + level + "b. " + trazaError);
      }



      // InputStream stream2 = process.getInputStream();
      // FileUtils.copyInputStreamToFile(stream2, new File("c:/d/log_in.txt"));
      // InputStream stream3 = process.getErrorStream();
      // FileUtils.copyInputStreamToFile(stream3, new File("c:/d/log_out.txt"));
    }

  }

  /**
   * 
   * @param contenidoPDF
   * @param password
   * @return [] => [0] boolean [1] pdfreader
   * @throws EeutilPdfaToolsException
   */
  public boolean isEncrypted(byte[] contenidoPDF, String password) throws EeutilPdfaToolsException {
    boolean isEncrypted = false;

    if (!stringUtil.esVacioOrNull(password)) {

      try (PdfReader reader = new PdfReader(contenidoPDF, password.getBytes());) {
        isEncrypted = reader.isEncrypted();

      } catch (BadPasswordException e) {
        throw new EeutilPdfaToolsException(e.getMessage(), e);
      } catch (IOException e) {
        throw new EeutilPdfaToolsException(e.getMessage(), e);
      }

    } else {
      isEncrypted = false;
    }

    return isEncrypted;
  }

  /***
   * Solo tiene sentido ejecutar para un pdf protegido.
   * 
   * @param contenidoPDF
   * @param password
   * @return
   * @throws EeutilPdfaToolsException
   */
  public byte[] cloneUnlkPdf(byte[] contenidoPDF, String password) throws EeutilPdfaToolsException {

    FileOutputStream fio = null;
    PdfReader reader = null;
    byte[] bPdfSinPwd = null;
    File fOutConverted = null;
    try {
      String pathClonePDF = fileUtil.createFilePath(PDFA_CLONE_PREFIX, null);
      fio = new FileOutputStream(pathClonePDF);
      reader = new PdfReader(contenidoPDF, password.getBytes());
      PdfEncryptor.encrypt(reader, fio, null, null, reader.getPermissions(), true);
      fOutConverted = new File(pathClonePDF);

      if (fOutConverted.exists() && fOutConverted.isFile()) {
        bPdfSinPwd = Files.readAllBytes(fOutConverted.toPath());
      }

    } catch (IOException e) {
      throw new EeutilPdfaToolsException(e.getMessage(), e);
    } finally {
      if (fio != null) {
        try {
          fio.close();
        } catch (IOException e) {
          throw new EeutilPdfaToolsException(e.getMessage(), e);
        }
      }
      if (reader != null) {
        reader.close();
      }
      if (fOutConverted != null && fOutConverted.exists() && fOutConverted.isFile()) {
        fOutConverted.delete();
      }
    }

    return bPdfSinPwd;
  }

  /**
   * Sobre un fichero no protegido vamos a protegerlo con un password.
   * 
   * @param contenidoPDF
   * @param password
   * @return
   * @throws EeutilPdfaToolsException
   */
  public byte[] cloneLckPdf(byte[] contenidoPDF, String password) throws EeutilPdfaToolsException {

    FileOutputStream fio = null;
    PdfReader reader = null;
    byte[] bPdfSinPwd = null;
    File fOutConverted = null;
    try {
      String pathClonePDF = fileUtil.createFilePath(PDFA_CLONE_PREFIX, null);
      fio = new FileOutputStream(pathClonePDF);
      reader = new PdfReader(contenidoPDF);
      PdfEncryptor.encrypt(reader, fio, password.getBytes(), password.getBytes(),
          reader.getPermissions(), true);
      fOutConverted = new File(pathClonePDF);

      if (fOutConverted.exists() && fOutConverted.isFile()) {
        bPdfSinPwd = Files.readAllBytes(fOutConverted.toPath());
      }

    } catch (IOException e) {
      throw new EeutilPdfaToolsException(e.getMessage(), e);
    } finally {
      if (fio != null) {
        try {
          fio.close();
        } catch (IOException e) {
          throw new EeutilPdfaToolsException(e.getMessage(), e);
        }
      }
      if (reader != null) {
        reader.close();
      }
      if (fOutConverted != null && fOutConverted.exists() && fOutConverted.isFile()) {
        fOutConverted.delete();
      }
    }

    return bPdfSinPwd;
  }

  /***
   * Solo tiene sentido ejecutar para un pdf protegido.
   * 
   * @param contenidoPDF
   * @param password
   * @return
   * @throws EeutilPdfaToolsException
   */
  public byte[] cloneUnlkPdf(PdfReader reader, String password) throws EeutilPdfaToolsException {

    FileOutputStream fio = null;
    String pathClonePDF = fileUtil.createFilePath(PDFA_CLONE_PREFIX, null);
    byte[] bPdfSinPwd = null;
    File fOutConverted = null;
    try {
      fio = new FileOutputStream(pathClonePDF);
      PdfEncryptor.encrypt(reader, fio, null, null, reader.getPermissions(), true);
      fOutConverted = new File(pathClonePDF);

      if (fOutConverted.exists() && fOutConverted.isFile()) {
        bPdfSinPwd = Files.readAllBytes(fOutConverted.toPath());
      }

    } catch (IOException e) {
      throw new EeutilPdfaToolsException(e.getMessage(), e);
    } finally {
      if (fio != null) {
        try {
          fio.close();
        } catch (IOException e) {
          throw new EeutilPdfaToolsException(e.getMessage(), e);
        }
      }
      if (reader != null) {
        reader.close();
      }
      if (fOutConverted != null && fOutConverted.exists() && fOutConverted.isFile()) {
        fOutConverted.delete();
      }
    }

    return bPdfSinPwd;
  }

  private class WorkerProcessConvert extends Thread {
    private final Process process;
    private boolean bExit;

    public boolean isbExit() {
      return bExit;
    }

    private WorkerProcessConvert(Process process) {
      this.process = process;
    }

    public void run() {
      try {
        bExit = process.waitFor(120, TimeUnit.SECONDS);
      } catch (InterruptedException ignore) {
        bExit = false;
        return;
      }

    }

  }

}
