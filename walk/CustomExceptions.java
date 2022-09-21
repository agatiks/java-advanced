package info.kgeorgiy.ja.shevchenko.walk;

abstract class WalkerException extends Exception {
    WalkerException(String message) {
        super(message);
    }

    WalkerException(String message, Throwable cause) {
        super(message, cause);
    }
}

class WrongArgumentsException extends WalkerException {
    WrongArgumentsException(String errorMessage) {
        super(errorMessage);
    }
}

class IncorrectPathException extends WalkerException {
    IncorrectPathException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }
}

class WalkerIOException extends WalkerException {
    WalkerIOException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }
}

class SHA1UsageException extends WalkerException {
    SHA1UsageException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }
}



