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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Configuration;

import es.gob.ad.eeutil.eeutilpdfatools.exception.EeutilPdfaToolsException;

@Configuration
public class IOUtil {

  protected static final Log logger = LogFactory.getLog(IOUtil.class);


  public static byte[] getBytesFromFile(File file) throws EeutilPdfaToolsException {
    try (InputStream is = new FileInputStream(file)) {
      long length = file.length();

      if (length > Integer.MAX_VALUE) {
      }

      byte[] bytes = new byte[(int) length];

      int offset = 0;
      int numRead = 0;
      while (offset < bytes.length
          && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
        offset += numRead;
      }
      if (offset < bytes.length) {
        throw new IOException("Could not completely read file " + file.getName());
      }

      return bytes;
    } catch (IOException e) {
      throw new EeutilPdfaToolsException(e.getMessage(), e);
    } catch (Exception e) {
      throw new EeutilPdfaToolsException(e.getMessage(), e);
    }
  }

}
