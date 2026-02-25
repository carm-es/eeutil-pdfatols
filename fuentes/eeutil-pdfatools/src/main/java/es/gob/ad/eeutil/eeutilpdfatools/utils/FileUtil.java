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

package es.gob.ad.eeutil.eeutilpdfatools.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import es.gob.ad.eeutil.eeutilpdfatools.exception.EeutilPdfaToolsException;

@PropertySource("file:${eeutil-pdfa-tools.config.path}/pdfatools-config.properties")
@Configuration
public class FileUtil {

  protected static final Log logger = LogFactory.getLog(FileUtil.class);

  private static final String FILE_SEPARATOR = "file.separator";

  @Value("${eeutil-pdfa-tools.rutaDirTmp}")
  private String rutaTmpDir;

  /**
   * Devuelve la ruta del directorio temporal.
   * 
   * @return
   */
  public String getTmpDir() {
    return rutaTmpDir;
  }

  /**
   * Devuelve un nombre de fichero aleatorio en el directorio temporal, segÃºn el patrÃ³n:
   * currentTimeMillis () + id-thread-actual.
   * 
   * @return
   */
  public String createFilePath() {
    return createFilePath(null);
  }

  /**
   * Devuelve un nombre de fichero aleatorio en el directorio temporal, segun el patron: prefix +
   * uniqueName().
   * 
   * @param prefix Si es nulo, no se mete en el nombre.
   * @return
   */
  public String createFilePath(String prefix) {

    String s = getTmpDir();
    s += System.getProperty(FILE_SEPARATOR);
    s += prefix != null ? prefix : "";
    s += uniqueName();
    // System.out.println(s);

    /*
     * String s = getTmpDir() + System.getProperty("file.separator") + prefix != null ? prefix : ""
     * + System.currentTimeMillis() + Thread.currentThread().getId();
     */
    return s;
  }

  /**
   * Devuelve un nombre de fichero aleatorio en el directorio temporal, segÃºn el patrÃ³n: prefix +
   * uniqueName().
   * 
   * @param prefix Si es nulo, no se mete en el nombre.
   * @return
   * @throws IOException
   */
  public String createFilePath(String prefix, byte[] contenido) throws EeutilPdfaToolsException {
    String inputPathFile = null;
    inputPathFile = createFilePath(prefix);

    try (FileOutputStream fos = new FileOutputStream(inputPathFile);) {
      if (contenido != null) {
        fos.write(contenido);
      }
    } catch (IOException e) {
      throw new EeutilPdfaToolsException(e.getMessage(), e);
    }
    return inputPathFile;
  }

  /**
   * Metodo para generar un nombre unico de fichero En principio sera el milisegundo actual mas el
   * identificador de thread. Si ese nombre coincide con el ultimo que se genera, entonces se le
   * aÃ±ade un sufijo.
   * 
   * @return
   */
  private static String uniqueName() {

    return UUID.randomUUID().toString();

  }

}
