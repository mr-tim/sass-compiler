package mrtim.sasscompiler;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Parameterized.class)
public class SassSpecTest {

    private File specFile;

    public SassSpecTest(String testName, File specFile) {
        this.specFile = specFile;
    }

    @Test
    public void testCompilation() throws IOException {
        Context c = new Context.Builder()
                            .entryPoint(specFile.getAbsolutePath())
                            .build();
        c.compileFile();

        File expectedOutput = new File(specFile.getParent() + File.separator + "expected_output.css");
        File actualOutput = new File(specFile.getParent() + File.separator + "input.css");

        assertEqualsIgnoringWhitespace("Wrong compiled file output", getFileContent(expectedOutput), getFileContent(actualOutput));
    }

    private void assertEqualsIgnoringWhitespace(String message, String rawExpected, String rawActual) {
        String expected = ignoreWhitespace(rawExpected);
        String actual = ignoreWhitespace(rawActual);
        assertEquals(message, expected, actual);
    }

    private String ignoreWhitespace(String str) {
        return StringUtils.join(FluentIterable.from(Arrays.asList(str.split("\\n+"))).transform(new Function<String, String>() {
            @Override
            public String apply(String input) {
                return StringUtils.stripEnd(input, null);
            }
        }).filter(new Predicate<String>() {
            @Override
            public boolean apply(String input) {
                return input.trim().length() > 0;
            }
        }).toList(), "\n");
    }

    private String getFileContent(File file) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
        return Charset.defaultCharset().decode(ByteBuffer.wrap(encoded)).toString();
    }

    @Parameterized.Parameters(name="{0}")
    public static List<Object[]> testFiles() {
        assertNotNull("In order to run tests against the sass spec, the sass-spec.dir property must be set", System.getProperty("sass-spec.dir"));
        List<Object[]> specFiles = new ArrayList<>();
        File path = new File(System.getProperty("sass-spec.dir") + File.separator + "spec");
        String suite = "basic";

        File suiteDir = new File(path.getAbsolutePath() + File.separator + suite);

        int count = 0;
        int targetTest = -1;
        for (File subdir: suiteDir.listFiles()) {
            if (targetTest < 0 || count == targetTest) {
                File testFile = new File(subdir.getAbsolutePath() + File.separator + "input.scss");
                String test = suite + "/" + subdir.getName();
                specFiles.add(new Object[] { test, testFile} );
            }
            count++;
        }

        return specFiles;
    }

}
