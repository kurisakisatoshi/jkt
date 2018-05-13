package just.killing.time;

import static java.nio.charset.StandardCharsets.*;
import static java.util.stream.Collectors.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import net.lingala.zip4j.core.ZipFile;

@RunWith(JUnit4.class)
public class ZipEncryptorTest {

    @Test
    public void zipファイルにパスワード付与() throws Exception {

        Path zip = zipFile("zip_test.zip");

        ZipEncryptor ze = ZipEncryptor.builder().zipFile(zip.toString()).password("test123").build();
        ze.encrypt();

        ZipFile assertion = new ZipFile(zip.toFile());
        assertion.setPassword("test123");
        assertThat(assertion.getFile().exists(), equalTo(true));
        assertThat(assertion.isEncrypted(), equalTo(true));
        assertZip(assertion);
    }

    @Test(expected = IllegalArgumentException.class)
    public void パスワード設定済みは例外() throws Exception {

        Path zip = zipFile("zip_test_encrypted.zip");

        ZipEncryptor ze = ZipEncryptor.builder().zipFile(zip.toString()).password("test123").build();
        ze.encrypt();

        fail();
    }



    void assertZip(ZipFile assertion) throws Exception {

        Path assertPath = thisDir().resolve("assert");

        assertion.extractAll(assertPath.toString());

        String actualFiles = Files.walk(assertPath.resolve("zip_test"))
                .map(p -> p.toString().replace(assertPath.resolve("zip_test").toString(), ""))
                .map(p -> p.replace("\\", "/"))
                .collect(joining("\n"));

        String expectedFile = Files.readAllLines(thisDir().resolve("expected_file_list.txt"), UTF_8).stream().collect(joining("\n"));

        assertThat(actualFiles, is(expectedFile));
    }



    Path thisDir() throws Exception {
        return Paths.get(getClass().getResource("/" + getClass().getPackageName().replace('.', '/')).toURI());
    }

    Path zipFile(String fileName) throws Exception {
        return thisDir().resolve(fileName);
    }



    @Before
    public void setup() throws Exception {
        Files.copy(zipFile("zip_test.zip"), zipFile("zip_test_org.zip"), StandardCopyOption.REPLACE_EXISTING);
    }

    @After
    public void teardown() throws Exception {
        FileUtils.deleteQuietly(thisDir().resolve("assert").toFile());
        Files.move(zipFile("zip_test_org.zip"), zipFile("zip_test.zip"), StandardCopyOption.REPLACE_EXISTING);
    }
}
