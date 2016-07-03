package gtranslator.domain.converter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

@Converter
public class ZipConverter implements AttributeConverter<String, byte[]> {

    @Override
    public byte[] convertToDatabaseColumn(String rawStr) {
        if (StringUtils.isBlank(rawStr)) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GZIPOutputStream z = new GZIPOutputStream(out)) {
            z.write(rawStr.getBytes("UTF-8"));
        } catch (IOException ex) {
            //TODO RuntimeException
            throw new RuntimeException(ex);
        }
        return out.toByteArray();
    }

    @Override
    public String convertToEntityAttribute(byte[] blob) {
        if (blob == null || blob.length == 0) {
            return null;
        }
        ByteArrayInputStream in = new ByteArrayInputStream(blob);
        try (GZIPInputStream z = new GZIPInputStream(in)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            IOUtils.copy(z, out);
            return new String(out.toByteArray(), "UTF-8");
        } catch (IOException ex) {
            //TODO RuntimeException
            throw new RuntimeException(ex);
        }
    }
}
