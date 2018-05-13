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

    /** パスワード付与するZipファイルのパス */
    private final Path zipFile;

    /** 設定するパスワード */
    private final String password;

    /**
     * コンストラクタ
     *
     * @param builder {@link Builder ビルダークラス}
     */
    private ZipEncryptor(Builder builder) {
        this.zipFile = Paths.get(builder.zipFile);
        this.password = builder.password;
    }

    /**
     * Zipファイルにパスワード付与します。
     *
     * @throws IOException IO例外発生時にスロー
     */
    public void encrypt() throws IOException {

        Path destTmpDir = createTmpDir();

        try {

            ZipFile srcZip = new ZipFile(zipFile.toFile());

            if (srcZip.isEncrypted()) {
                throw new IllegalArgumentException("Zip file is already encrypted. " + zipFile.toString());
            }

            srcZip.extractAll(destTmpDir.toString());

            Files.delete(zipFile);  // 展開したら消す

            // 新たにZipファイルを出力する際、同名のファイルが存在すると失敗するため(ライブラリの仕様)、上で元Zipを削除している
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

    /**
     * 一時ディレクトリを作成します。
     *
     * @return 一時ディレクトリのパス
     * @throws IOException 一時ディレクトリ作成失敗時にスロー
     */
    Path createTmpDir() throws IOException {
        return Files.createTempDirectory(this.zipFile.getParent(), Thread.currentThread().getName() + "_");
    }

    /**
     * {@link Builder ビルダークラス}のインスタンスを生成します。
     *
     * @return {@link Builder ビルダークラス}のインスタンス
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * ビルダークラス
     */
    public static class Builder {

        /** パスワード付与するZipファイル */
        private String zipFile;

        /** 設定するパスワード */
        private String password;

        /**
         * パスワード付与するZipファイルを指定します。
         *
         * @param zipFile パスワード付与するZipファイルのパス（フルパス）
         * @return このインスタンス
         */
        public Builder zipFile(String zipFile) {
            this.zipFile = zipFile;
            return this;
        }

        /**
         * 設定するパスワードを指定します。
         *
         * @param password 設定するパスワード
         * @return このインスタンス
         */
        public Builder password(String password) {
            this.password = password;
            return this;
        }

        /**
         * {@link ZipEncryptor}のインスタンスを生成します。
         *
         * @return {@link ZipEncryptor}のインスタンス
         */
        public ZipEncryptor build() {

            Objects.requireNonNull(zipFile);
            Objects.requireNonNull(password);

            return new ZipEncryptor(this);
        }
    }
}
