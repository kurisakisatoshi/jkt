package just.killing.time;

import static net.lingala.zip4j.util.Zip4jConstants.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.apache.commons.io.FileUtils;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;

/**
 * Zipファイルにパスワードを付与します。
 * <p>
 * 元のZipファイルを一度展開し、パスワードを付与してZipファイルを作成し直します。<br>
 * （元ファイルを削除→パスワード付与版を作成）
 *
 * <pre>
 * {@code
 * ZipEncryptor ze = ZipEncryptor.builder().zipFile("Zipファイルのパス").password("test123").build();
 * ze.encrypt();
 * }
 *</pre>
 */
public class ZipEncryptor {

    private final Path zipFile;
    private final String password;

    private ZipEncryptor(Builder builder) {
        this.zipFile = Paths.get(builder.zipFile);
        this.password = builder.password;
    }

    public void encrypt() throws IOException {

        Path destTmpDir = createTmpDir();

        try {

            ZipFile srcZip = new ZipFile(zipFile.toFile());

            if (srcZip.isEncrypted()) {
                throw new IllegalArgumentException("Zip file is already encrypted. " + zipFile.toString());
            }

            srcZip.extractAll(destTmpDir.toString());

            Files.delete(zipFile);  // 展開したら消す

            // 元Zipと同じファイルパスを指定する
            // ここで指定するファイルが存在すると失敗する(ライブラリの仕様)ため
            // ↑で元Zip展開後、削除している
            ZipFile destZip = new ZipFile(zipFile.toFile());

            ZipParameters parameters = new ZipParameters();
            parameters.setIncludeRootFolder(false);
            parameters.setEncryptFiles(true);
            parameters.setCompressionMethod(COMP_DEFLATE);
            parameters.setCompressionLevel(DEFLATE_LEVEL_NORMAL);
            parameters.setEncryptionMethod(ENC_METHOD_STANDARD);
            parameters.setPassword(password);

            destZip.createZipFileFromFolder(destTmpDir.toString(), parameters, false, 0);

        } catch (ZipException ze) {
            throw new IOException(ze);

        } finally {
            FileUtils.deleteQuietly(destTmpDir.toFile());
        }
    }

    Path createTmpDir() throws IOException {
        return Files.createTempDirectory(this.zipFile.getParent(), Thread.currentThread().getName());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String zipFile;
        private String password;

        public Builder zipFile(String zipFile) {
            this.zipFile = zipFile;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public ZipEncryptor build() {

            Objects.requireNonNull(zipFile);
            Objects.requireNonNull(password);

            return new ZipEncryptor(this);
        }
    }
}
