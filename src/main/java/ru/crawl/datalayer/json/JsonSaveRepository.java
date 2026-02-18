package ru.crawl.datalayer.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.crawl.datalayer.json.saveData.SaveGame;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class JsonSaveRepository {

    private final Path path = Path.of("save.json");
    private final ObjectMapper mapper = new ObjectMapper();

    public boolean exists() {
        return Files.exists(path);
    }

    public SaveGame load() throws IOException {
        return mapper.readValue(path.toFile(), SaveGame.class);
    }

    public void save(SaveGame saveGame) throws IOException {
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(path.toFile(), saveGame);
    }

    public Optional<SaveGame> tryLoad() {
        if (!exists()) {
            return Optional.empty();
        }
        try {
            return Optional.of(load());
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}