package com.chunkrsvp.cli;

import com.chunkrsvp.model.Chunk;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChunkLoader {
    public static List<Chunk> load(InputStream defaultStream) {
        try {
            if (defaultStream.available() > 0) {
                return readFromStream(defaultStream);
            } else {
                InputStream mock = ChunkLoader.class.getClassLoader().getResourceAsStream("mock_chunks.txt");
                return mock != null ? readFromStream(mock) : new ArrayList<>();
            }
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private static List<Chunk> readFromStream(InputStream is) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return reader.lines().filter(line -> !line.isBlank()).map(Chunk::new).collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
