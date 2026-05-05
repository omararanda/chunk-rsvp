package com.chunkrsvp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class InputResolutionService {
    public InputStream resolve(String filePath, InputStream stdIn) throws FileNotFoundException {
        if (filePath != null && !filePath.isBlank()) {
            return new FileInputStream(new File(filePath));
        }
        return stdIn;
    }
}
