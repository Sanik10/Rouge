package ru.crawl.datalayer.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.crawl.datalayer.json.leaderbordData.Leaderboard;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JsonLeaderboardRepository {

    private final Path path = Path.of("leaderboard.json");
    private final ObjectMapper mapper = new ObjectMapper();

    public List<Leaderboard> load() throws IOException {
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        return mapper.readValue(
                path.toFile(),
                new TypeReference<List<Leaderboard>>() {}
        );
    }

    public void save(List<Leaderboard> data) throws IOException {
        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(path.toFile(), data);
    }
}
