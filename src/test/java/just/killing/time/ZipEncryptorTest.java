package just.killing.time;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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

        Path zip = zipFile("zip4j_examples_1.3.2.zip");

        ZipEncryptor ze = ZipEncryptor.builder().zipFile(zip.toString()).password("test123").build();
        ze.encrypt();

        ZipFile assertion = new ZipFile(zip.toFile());
        assertion.setPassword("test123");
        assertThat(assertion.getFile().exists(), equalTo(true));
        assertThat(assertion.isEncrypted(), equalTo(true));
//        assertion.extractAll(thisDir().resolve("test").toString());
    }





    Path thisDir() throws Exception {
        return Paths.get(getClass().getResource("/" + getClass().getPackageName().replace('.', '/')).toURI());
    }

    Path zipFile(String fileName) throws Exception {
        return thisDir().resolve(fileName);
    }



    @Before
    public void setup() throws Exception {
        Files.copy(zipFile("zip4j_examples_1.3.2.zip"), zipFile("zip4j_examples_1.3.2_org.zip"), StandardCopyOption.REPLACE_EXISTING);
    }

    @After
    public void teardown() throws Exception {
        Files.move(zipFile("zip4j_examples_1.3.2_org.zip"), zipFile("zip4j_examples_1.3.2.zip"), StandardCopyOption.REPLACE_EXISTING);
    }
}
