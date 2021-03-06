package tests;

import org.checkerframework.framework.test.CheckerFrameworkTest;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;

public class RegexTest extends CheckerFrameworkTest {

    public RegexTest(File testFile) {
        super(testFile,
                org.checkerframework.checker.regex.RegexChecker.class,
                "regex",
                "-Anomsgtext");
    }

    @Parameters
    public static String[] getTestDirs() {
        return new String[]{"regex", "regex_poly", "regex_qual_poly", "all-systems"};
    }
}
