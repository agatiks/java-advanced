package info.kgeorgiy.ja.shevchenko.walk;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class Walker {
    private static final String ZEROS = String.format("%040d", 0);
    private final Path inputFilePath;
    private final Path outputFilePath;

    private BufferedWriter output;
    private BufferedReader input;
    private final MessageDigest digest;


    Walker(String[] args) throws WalkerException {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            //наш класс должен проходить и закидывать в файл значения
            //если мы где-то в стороннем месте вызовем класс,
            // то хотим ловить ошибки или получить хороший итоговый результат
            throw new WrongArgumentsException("Wrong arguments");
        }

        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new SHA1UsageException("Can't use SHA-1 algorithm", e);
        }

        //Добавляем пути к файлам
        try {
            inputFilePath = Paths.get(args[0]);
        } catch (InvalidPathException e) {
            throw new IncorrectPathException(String.format("Can't find input file %s", args[0]), e);
        }
        try {
            outputFilePath = Paths.get(args[1]);
        } catch (InvalidPathException e) {
            throw new IncorrectPathException(String.format("Can't find output file %s", args[1]), e);
        }

        //Проверяем, что 1. можем читать из входного файла
        // 2. найти или создать выходной файл по указанному пути
        openFiles();
    }

    void walk() throws WalkerException {
        String line;
        try {
            while ((line = input.readLine()) != null) {
                Path currPath;
                try {
                    currPath = Paths.get(line);
                } catch (InvalidPathException e) {
                    try {
                        output.write(ZEROS + " " + line);
                        output.newLine();
                    } catch (IOException ex) {
                        throw new IOException("Can't write to output file");
                    }
                    continue;
                }

                try (Stream<Path> children = Files.walk(currPath)) {
                    Stream<Path> find = filterChild(children);
                    List<String> res = find.map(it ->
                            makeSHA1(it) + " " + it.toString()).collect(Collectors.toList());
                    res.forEach(it -> {
                        try {
                            output.write(it);
                        } catch (IOException ex) {
                            ex.printStackTrace(); //TODO
                        }
                        try {
                            output.newLine();
                        } catch (IOException ex) {
                            ex.printStackTrace(); //TODO
                        }
                    });
                } catch (IOException e) {
                    try {
                        output.write(ZEROS + " " + line);
                        output.newLine();
                    } catch (IOException ex) {
                        throw new IOException("Can't write to output file");
                    }
                }
            }
        } catch (IOException e) {
            throw new WalkerIOException(String.format("Can't read line from input file %s", input), e);
        }
    }

    void close() throws WalkerException {
        try {
            output.close();
            input.close();
        } catch (IOException e) {
            throw new WalkerIOException("Can't close files", e);
        }
    }

    private void openFiles() throws WalkerException {
        //output хотим проверить раньше, потому что какой смысл читать из входного,
        // если в выходной в итоге не запишем
        Path parent = outputFilePath.getParent();

        try {
            //создаём папку до нашего
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (IOException e) {
            throw new WalkerIOException("Can't create directory for non existing output file", e);
        }

        try {
            output = Files.newBufferedWriter(outputFilePath);
        } catch (IOException e) {
            throw new WalkerIOException("Can't open output file for writing", e);
        }
        try {
            input = Files.newBufferedReader(inputFilePath);
        } catch (IOException e) {
            throw new WalkerIOException("Can't open input file for reading", e);
        }
    }

    private String makeSHA1(Path file) {
        try (InputStream input = new FileInputStream(file.toString())) {
            byte[] buffer = new byte[1024];
            int len = input.read(buffer);
            while (len != -1) {
                digest.update(buffer, 0, len);
                len = input.read(buffer);
            }
            byte[] hash = digest.digest();
            return String.format("%0" + (hash.length << 1) + "x", new BigInteger(1, hash));
        } catch (IOException e) {
            e.printStackTrace();
            return ZEROS;
        }
    }

    abstract Stream<Path> filterChild(Stream<Path> children);
}