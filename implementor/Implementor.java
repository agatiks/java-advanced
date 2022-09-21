// :NOTE: Не проходят тесты
// === Running test21_encoding
//    Loading class info.kgeorgiy.java.advanced.implementor.full.lang.ArabicImpl
//    Loading class info.kgeorgiy.java.advanced.implementor.full.lang.?????Impl
//    Loading class info.kgeorgiy.java.advanced.implementor.full.lang.HebrewImpl
//    Loading class info.kgeorgiy.java.advanced.implementor.full.lang.??Impl
//    Loading class info.kgeorgiy.java.advanced.implementor.full.lang.GreekImpl
//    Loading class info.kgeorgiy.java.advanced.implementor.full.lang.????Impl
//    Loading class info.kgeorgiy.java.advanced.implementor.full.lang.RussianImpl
//    Loading class info.kgeorgiy.java.advanced.implementor.full.lang.������Impl
//test21_encoding\info\kgeorgiy\java\advanced\implementor\full\lang\ArabicImpl.java:3: error: illegal character: '\u2026'
//    public  ArabicImpl (info.kgeorgiy.java.advanced.implementor.full.lang.Arabic.مرحبا arg0) throws info.kgeorgiy.java.advanced.implementor.full.lang.Arabic.مرحبا {
//                                                                                  ^
//test21_encoding\info\kgeorgiy\java\advanced\implementor\full\lang\ArabicImpl.java:3: error: illegal character: '\u00b1'
//    public  ArabicImpl (info.kgeorgiy.java.advanced.implementor.full.lang.Arabic.مرحبا arg0) throws info.kgeorgiy.java.advanced.implementor.full.lang.Arabic.مرحبا {

package info.kgeorgiy.ja.shevchenko.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * Implementation class for {@link JarImpler} interface.
 *
 * @author Agata Shevchenko
 * @version 1.0
 */
public class Implementor implements JarImpler {
    /**
     * {@link List} of incorrect modifiers.
     */
    private static final List<Integer> INCORRECT_MODIFIERS = List.of(
            Modifier.ABSTRACT,
            Modifier.VOLATILE,
            Modifier.NATIVE,
            Modifier.TRANSIENT);

    /**
     * Suffix for the name of the file to create
     */
    private static final String CLASS_SUFFIX = "Impl";

    /**
     * Suffix for the created java file.
     */
    private static final String JAVA_FILE_SUFFIX = ".java";
    /**
     * Suffix for the created java file.
     */
    private static final String CLASS_FILE_SUFFIX = ".class";

    /**
     * Static command {@link String string} implements.
     */
    private static final String IMPLEMENT_INHERITANCE = "implements";
    /**
     * Static command {@link String string} extends.
     */
    private static final String EXTENDS_INHERITANCE = "extends";
    /**
     * Static command {@link String string} return.
     */
    private static final String RETURN = "return";

    /**
     * {@link Enum} for types of braces.
     */
    private enum BRACES {
        /**
         * Parenthesis type.
         */
        PARENTHESIS,
        /**
         * Brace type.
         */
        BRACE
    }

    /**
     * Punctuation used in code: whitespace.
     */
    private static final String SPACE = " ";
    /**
     * Punctuation used in code: tab.
     */
    private static final String TAB = "    ";
    /**
     * Punctuation used in code: left brace.
     */
    private static final String LEFT_BRACE = "{";
    /**
     * Punctuation used in code: right brace.
     */
    private static final String RIGHT_BRACE = "}";
    /**
     * Punctuation used in code: empty string.
     */
    private static final String EMPTY = "";
    /**
     * Punctuation used in code: comma + space.
     */
    private static final String DELIMITER = ", ";
    /**
     * Punctuation used in code: left parenthesis.
     */
    private static final String LEFT_PARENTHESIS = "(";
    /**
     * Punctuation used in code: right parenthesis.
     */
    private static final String RIGHT_PARENTHESIS = ")";
    /**
     * Punctuation used in code: semicolon.
     */
    private static final String END_OF_CODE_LINE = ";";
    /**
     * Punctuation used in code: line separator.
     */
    private static final String NEW_LINE = System.lineSeparator();

    /**
     * Default values to return in primitive functions.
     */
    private static final String PRIMITIVE_DEFAULT_VALUE = "0";
    /**
     * Default values to return in boolean functions.
     */
    private static final String BOOLEAN_DEFAULT_VALUE = "false";
    /**
     * Default values to return in void functions.
     */
    private static final String VOID_DEFAULT_VALUE = EMPTY;
    /**
     * Default values to return in object functions.
     */
    private static final String OBJECT_DEFAULT_VALUE = "null";

    /**
     * Variable for currently implemented class.
     */
    private static Class<?> token;

    /**
     * Checks arguments and makes .jar or .java implementation of class.
     *
     * @param args two options:
     *             <code>-jar <var>token</var> <var>root</var></code>,
     *             <code><var>token</var> <var>root</var></code>.
     */
    public static void main(String[] args) {
        if (args == null || (args.length != 2 && args.length != 3)) {
            System.err.println("Incorrect number of arguments.");
            return;
        }
        if (Arrays.stream(args).filter(Objects::isNull).toArray().length != 0) {
            System.err.println("Arguments can't be null.");
            return;
        }
        JarImpler implementor = new Implementor();
        try {
            if (args.length == 2) {
                implementor.implement(Class.forName(args[0]), Paths.get(args[1]));
            } else if (args[0].equals("-jar")) {
                implementor.implementJar(Class.forName(args[1]), Paths.get(args[2]));
            } else {
                System.err.println("Incorrect arguments.");
            }
        } catch (ClassNotFoundException | LinkageError e) {
            System.err.println("Invalid class name: " + e.getMessage());
        } catch (InvalidPathException e) {
            System.err.println("Invalid path of class: " + e.getMessage());
        } catch (ImplerException e) {
            System.err.println("Exception while make implementation: " + e.getMessage());
        }
    }

    /**
     * Produces code implementing class or interface specified by provided {@code token}.
     * <p>
     * Generated class classes name should be same as classes name of the type token with {@code Impl} suffix
     * added. Generated source code should be placed in the correct subdirectory of the specified
     * {@code root} directory and have correct file name. For example, the implementation of the
     * interface {@link java.util.List} should go to {@code $root/java/util/ListImpl.java}
     *
     * @param token type token to create implementation for.
     * @param root  root directory.
     * @throws info.kgeorgiy.java.advanced.implementor.ImplerException when implementation cannot be
     *                                                                 generated.
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        Implementor.token = token;
        if (tokenIsNotCorrect()) {
            throw new ImplerException("Invalid class.");
        }

        //returns path for new java file with implementation
        Path path = makeFilePath(root, JAVA_FILE_SUFFIX);

        // creates directories for {path}
        makeJavaFileAndDirs(path);

        try (BufferedWriter impl = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            impl.write(toUnicode(classPackage()));
            impl.newLine();
            impl.write(toUnicode(classHeader()));
            impl.write(LEFT_BRACE); //TODO: how to use writeWithBraces?
            impl.newLine();

            if (!token.isInterface()) {
                writeAllConstructors(impl);
            }

            writeAllMethods(impl);
            impl.newLine();
            impl.write(RIGHT_BRACE);
        } catch (IOException e) {
            throw new ImplerException("Can't write into file for implementation.", e);
        }
        Implementor.token = null;
    }

    /**
     * Produces <var>.jar</var> file implementing class or interface specified by provided <var>token</var>.
     * <p>
     * Generated class classes name should be same as classes name of the type token with <var>Impl</var> suffix
     * added.
     *
     * @param token   type token to create implementation for.
     * @param jarFile target <var>.jar</var> file.
     * @throws ImplerException when implementation cannot be generated.
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        Implementor.token = token;
        if (tokenIsNotCorrect()) {
            throw new ImplerException("Invalid class.");
        }
        makeJavaFileAndDirs(jarFile);

        Path jarTempDirectory;
        try {
            jarTempDirectory = Files.createTempDirectory(jarFile.toAbsolutePath().getParent(), "tmp");
        } catch (IOException e) {
            throw new ImplerException("Can't create temp directory.", e);
        }

        try {
            implement(token, jarTempDirectory);
            Implementor.token = token;
            compile(jarTempDirectory);
            makeJar(jarFile, jarTempDirectory);
        } finally {
            try {
                delete(jarTempDirectory);
            } catch (IOException e) {
                System.err.println("Can't delete temporary directory: " + e.getMessage());
            }
        }

        Implementor.token = null;
    }

    /**
     * Removes temporary directory with path <var>dir</var>.
     *
     * @param dir target directory.
     * @throws IOException when can't walk directory.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void delete(Path dir) throws IOException {
        Files.walk(dir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    }

    /**
     * Creates <var>.jar</var> file from compiled <var>.class</var> files.
     *
     * @param path             target path for <var>.jar</var> file.
     * @param jarTempDirectory directory with compiled <var>.class</var> files.
     * @throws ImplerException when can't make jar output and write into it.
     */
    private void makeJar(Path path, Path jarTempDirectory) throws ImplerException {
        final Manifest manifest = new Manifest();
        final Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attributes.put(Attributes.Name.IMPLEMENTATION_VENDOR, "Agatha Shevchenko");
        try (final JarOutputStream writer = new JarOutputStream(Files.newOutputStream(path), manifest)) {
            writer.putNextEntry(new ZipEntry(token.getPackageName().replace('.', '/')
                    + "/" + token.getSimpleName() + "Impl.class"));
            //TODO: replace with smth else or make fun
            Files.copy(makeFilePath(jarTempDirectory, CLASS_FILE_SUFFIX), writer);
            writer.closeEntry();
        } catch (IOException e) {
            throw new ImplerException("Can't create zip-file.", e);
        }
    }

    /**
     * Compiles <var>.java</var> files into directory with path <var>jarTempDirectory</var>.
     * <p>
     * Result is directory with compiled <var>.class</var> files with the same names.
     *
     * @param jarTempDirectory directory where we want to compile files.
     * @throws ImplerException when can't convert <var>token</var> location to URI
     *                         or can't compile files or
     */
    private void compile(Path jarTempDirectory) throws ImplerException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) throw new ImplerException("Can't find compiler.");
        String[] compilationArgs;
        try {
            compilationArgs = new String[]{
                    "-cp",
                    jarTempDirectory.toString() + File.pathSeparator
                            + Path.of(token.getProtectionDomain().getCodeSource().getLocation().toURI()),
                    "-encoding",
                    "UTF8",
                    makeFilePath(jarTempDirectory, JAVA_FILE_SUFFIX).toString()
            };
        } catch (URISyntaxException |
                IllegalArgumentException |
                FileSystemNotFoundException e) {
            throw new ImplerException("Path is incorrect.", e);
        }
        if (compiler.run(null, null, null, compilationArgs) != 0) {
            throw new ImplerException("Can't compile files.");
        }
    }

    /**
     * Writes all methods of {@link Implementor#token} into writer.
     *
     * @param impl writer to file for implementation.
     * @throws ImplerException when can't write method.
     */
    private void writeAllMethods(BufferedWriter impl) throws ImplerException {
        ComparableMethod[] allMethods = takeMethods(Class::getMethods);
        ComparableMethod[] declMethods = takeMethods(Class::getDeclaredMethods);
        Set<ComparableMethod> methods = new HashSet<>(List.of(declMethods));
        methods.addAll(List.of(allMethods));
        for (ComparableMethod method : methods) {
            writeMethod(method.getMethod(), impl);
        }
    }

    /**
     * Returns list of {@link ComparableMethod methods} of {@link Implementor#token}: declared or super.
     *
     * @param getMethodsFunction function helps to get methods. One of two options: {@link Class#getMethods()} or {@link Class#getDeclaredMethods()}.
     * @return list of methods.
     */
    private ComparableMethod[] takeMethods(Function<Class<?>, Method[]> getMethodsFunction) {
        return Arrays.stream(getMethodsFunction.apply(token))
                .filter(exec -> !Modifier.isPrivate(exec.getModifiers())
                        && !Modifier.isFinal(exec.getModifiers()))
                .map(ComparableMethod::new)
                .toArray(ComparableMethod[]::new);
    }

    /**
     * Makes return {@link String string value} for function with provided <var>returnType</var>.
     *
     * @param returnType return type of function.
     * @return default string value for every type
     */
    private String defaultTypeValue(Class<?> returnType) {
        if (returnType.equals(void.class)) {
            return VOID_DEFAULT_VALUE;
        } else if (returnType.equals(boolean.class)) {
            return BOOLEAN_DEFAULT_VALUE;
        } else if (returnType.isPrimitive()) {
            return PRIMITIVE_DEFAULT_VALUE;
        }
        return OBJECT_DEFAULT_VALUE;
    }

    /**
     * Writes all constructors of {@link Implementor#token} into writer.
     *
     * @param impl writer to file for implementation.
     * @throws ImplerException when can't write constructor or don't have any available.
     */
    private void writeAllConstructors(BufferedWriter impl) throws ImplerException {
        Constructor<?>[] constructors = Arrays.stream(token.getDeclaredConstructors())
                .filter(constr -> !Modifier.isPrivate(constr.getModifiers()))
                .toArray(Constructor<?>[]::new);
        if (constructors.length == 0)
            throw new ImplerException("All class constructors are not available.");
        for (Constructor<?> constructor : constructors) {
            writeConstructor(constructor, impl);
        }
    }

    /**
     * Writes {@link Method one method} of {@link Implementor#token} into writer.
     *
     * @param method method needed to write.
     * @param impl   writer to file for implementation.
     * @throws ImplerException when can't write method.
     */
    private void writeMethod(Method method, BufferedWriter impl) throws ImplerException {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s%s %s %s ",
                TAB,
                getModifier(method),
                method.getReturnType().getCanonicalName(),
                method.getName()));
        appendExecutableBody(sb, makeBody(method), method);
        try {
            impl.write(toUnicode(sb.toString()));
            impl.newLine();
        } catch (IOException e) {
            throw new ImplerException("Can't write method", e);
        }
    }

    /**
     * Writes {@link Constructor one constructor} of {@link Implementor#token} into writer.
     *
     * @param constructor constructor needed to write.
     * @param impl        writer to file for implementation.
     * @throws ImplerException when can't write into writer.
     */
    private void writeConstructor(Constructor<?> constructor, BufferedWriter impl) throws ImplerException {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s%s %s ",
                TAB,
                getModifier(constructor),
                newClassName()));
        appendExecutableBody(sb, makeBody(constructor), constructor);
        try {
            impl.write(toUnicode(sb.toString()));
            impl.newLine();
        } catch (IOException e) {
            throw new ImplerException("Can't write constructor", e);
        }
    }

    /**
     * Adds to string builder bloc of executable.
     * <p>
     * Adds to string builder <var>sb</var> bloc with parameters, exceptions to {@link Executable executable object}
     * and <var>body</var> with <var>.java</var> syntax.
     * <p>
     * ({@link #getTypedParameters parameters}) throws {@link #getExceptions exceptiions} {
     * <var>body</var>
     * }
     *
     * @param sb   {@link StringBuilder} where we want to add text.
     * @param body body string for executable.
     * @param exec one of two options: {@link Constructor} or {@link Method}.
     */
    private void appendExecutableBody(StringBuilder sb, String body, Executable exec) {
        appendWithBraces(sb, BRACES.PARENTHESIS, getTypedParameters(exec));
        sb.append(getExceptions(exec))
                .append(SPACE);
        appendWithBraces(sb, BRACES.BRACE, body);
        sb.append(NEW_LINE);
    }

    /**
     * Returns {@link String correct modifiers} of method or constructor.
     * <p>
     * Deletes from {@link Executable#getModifiers() modifiers} {@link Implementor#INCORRECT_MODIFIERS wrong modifiers}.
     *
     * @param executable {@link Executable object} to be implemented
     * @return string with modifiers split whitespace for <var>executable</var>
     */
    private String getModifier(Executable executable) {
        int modifier = executable.getModifiers();
        for (int wrongModifier : INCORRECT_MODIFIERS) {
            modifier &= ~wrongModifier;
        }

        String modifierValue = Modifier.toString(modifier);
        if (modifierValue.length() > 0) {
            return modifierValue + SPACE;
        }
        return EMPTY;
    }

    /**
     * Returns {@link String constructor body} for constructor.
     *
     * @param constructor {@link Constructor} which body want to make
     * @return implementation, string: "    super({@link #getParameters parameters});"
     */
    private String makeBody(Constructor<?> constructor) {
        StringBuilder sb = new StringBuilder();
        sb.append(TAB)
                .append("super");
        appendWithBraces(sb, BRACES.PARENTHESIS, getParameters(constructor));
        sb.append(END_OF_CODE_LINE);
        return sb.toString();
    }

    /**
     * Returns {@link String method body} for constructor.
     *
     * @param method {@link Method} which body want to make
     * @return implementation, string: "    return({@link #defaultTypeValue default value});"
     */
    private String makeBody(Method method) {
        StringBuilder sb = new StringBuilder(TAB + RETURN);
        Class<?> returnType = method.getReturnType();

        if (!returnType.equals(void.class)) {
            sb.append(SPACE)
                    .append(defaultTypeValue(returnType));
        }

        sb.append(END_OF_CODE_LINE);
        return sb.toString();
    }

    /**
     * Adds to string builder <var>sb</var> text bloc <var>value</var> with braces.
     *
     * @param sb    target {@link StringBuilder}.
     * @param type  type of braces from enum <var>BRACES</var>.
     * @param value what we want between braces.
     */
    private void appendWithBraces(StringBuilder sb, BRACES type, String value) {
        String left, right, delimiter;

        if (type == BRACES.PARENTHESIS) {
            left = LEFT_PARENTHESIS;
            right = RIGHT_PARENTHESIS;
            delimiter = EMPTY;
        } else {
            left = LEFT_BRACE;
            right = RIGHT_BRACE;
            delimiter = NEW_LINE + TAB;
        }

        sb.append(left)
                .append(delimiter)
                .append(value)
                .append(delimiter)
                .append(right);
    }

    /**
     * Returns {@link String string of list} of executable exceptions.
     *
     * @param executable {@link Executable} which exceptions we want.
     * @return string of list of exceptions.
     */
    private String getExceptions(Executable executable) {
        String[] exceptions = Arrays.stream(executable.getExceptionTypes())
                .map(elem -> elem.getCanonicalName().replace("$", ".")).toArray(String[]::new);
        if (exceptions.length == 0)
            return EMPTY;
        return " throws " + String.join(DELIMITER, exceptions);
    }

    /**
     * Returns {@link String string of list} of executable typed parameters.
     *
     * @param executable {@link Executable} which parameters we want.
     * @return string of list of typed parameters.
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private String getTypedParameters(Executable executable) {
        String[] parameters = Arrays.stream(executable.getParameters())
                .map(elem ->
                        (new StringBuilder(elem.getType().getCanonicalName())
                                .append(SPACE)
                                .append(elem.getName()))
                                .toString()
                ).toArray(String[]::new);
        return String.join(DELIMITER, parameters);
    }

    /**
     * Returns {@link String string of list} of constructor not typed parameters.
     *
     * @param constructor {@link Constructor} which parameters we want.
     * @return string of list of non typed parameters.
     */
    private String getParameters(Constructor<?> constructor) {
        String[] parameters = Arrays.stream(constructor.getParameters())
                .map(Parameter::getName).toArray(String[]::new);
        return String.join(DELIMITER, parameters);
    }

    /**
     * Return implementation {@link String} prints package of {@link #token}.
     *
     * @return string "package <var>token package</var>;".
     */
    private String classPackage() {
        String packageName = token.getPackageName();
        if (!packageName.isEmpty()) {
            return String.format("package %s;", packageName);
        }
        return EMPTY;
    }

    /**
     * Return implementation {@link String} prints header of {@link #token} implementation: new class.
     *
     * @return string "public class <var>new name</var> implements/extends <var>old name</var>".
     */
    private String classHeader() {
        String inheritance = token.isInterface() ? IMPLEMENT_INHERITANCE : EXTENDS_INHERITANCE;
        return String.format("public class %s %s %s ",
                newClassName(),
                inheritance,
                token.getCanonicalName());
    }

    /**
     * Creates parents directories for {@link Path path}.
     *
     * @param path path to {@link #token} class.
     * @throws ImplerException when can't create directories.
     */
    private void makeJavaFileAndDirs(Path path) throws ImplerException {
        if (path.getParent() != null) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException e) {
                throw new ImplerException("Can't create directories for implementation file.", e);
            }
        }
    }

    /**
     * Makes path to file to be created from {@link #token}.
     *
     * @param root   path to root for new file.
     * @param suffix type of file to be created: <code>.java</code> or <code>.class</code>.
     * @return path for new file with new name.
     */
    private Path makeFilePath(Path root, String suffix) {
        //makes path from root with our package name and new classname from start classname
        return root.resolve(tokenPackagePathString()).resolve(newFileName(suffix));
    }

    /**
     * Creates from {@link #token} package name path string.
     *
     * @return package name with file separator.
     */
    private String tokenPackagePathString() {
        //gets package of token name with fs
        return token.getPackageName().replace('.', File.separatorChar);
    }

    /**
     * Returns name for file with implementation.
     *
     * @param suffix type of file: {@link #JAVA_FILE_SUFFIX} or {@link #CLASS_FILE_SUFFIX}.
     * @return name for implementation file.
     */
    private String newFileName(String suffix) {
        return newClassName().concat(suffix);
    }

    /**
     * Returns name for class with implementation.
     * <p>
     * Adds to {@link #token} class name suffix Impl.
     *
     * @return string with new classname.
     */
    private String newClassName() {
        return token.getSimpleName().concat(CLASS_SUFFIX);
    }

    /**
     * Validates class if we can make implementation.
     *
     * @return true if class is not correct.
     */
    private static boolean tokenIsNotCorrect() {
        return token.isArray() || token.isPrimitive()
                || token == Enum.class
                || Modifier.isFinal(token.getModifiers())
                || Modifier.isPrivate(token.getModifiers())
                || token.isEnum();
    }

    /**
     * Converts {@link String} to Unicode.
     *
     * @param str provided string
     * @return result string
     */
    private String toUnicode(String str) {
        StringBuilder res = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (c >= 128) {
                res.append(String.format("\\u%04X", (int) c));
            } else {
                res.append(c);
            }
        }
        return res.toString();
    }


    /**
     * Class to compare methods and add them into {@link Set}.
     */
    private class ComparableMethod {
        /**
         * Method to compare.
         */
        private final Method method;
        /**
         * Individual method string.
         */
        private final String signature;

        /**
         * Constructor creates signature from name and typed parameters.
         * @param method provided {@link Method method}.
         */
        ComparableMethod(Method method) {
            this.method = method;
            this.signature = method.getName() + getTypedParameters(method);
        }

        /**
         * Getter for {@link Method method}.
         * @return {@link #method method}
         */
        public Method getMethod() {
            return method;
        }

        /**
         * Makes hash for elem from {@link #signature}
         * @return hash from string signature.
         */
        @Override
        public int hashCode() {
            return signature.hashCode();
        }

        /**
         * Returns is methods equal by typed parameters and name.
         * @param obj {@link Object object} to compare.
         * @return true, if object equals by signature, else false.
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            return ((ComparableMethod) obj).signature.equals(signature);
        }
    }
}
