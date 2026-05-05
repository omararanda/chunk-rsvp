package com.chunkrsvp.cli;

import com.chunkrsvp.model.Chunk;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChunkLoader {
    public static List<Chunk> load(InputStream is) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return reader.lines().filter(line -> !line.isBlank()).map(Chunk::new).collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
