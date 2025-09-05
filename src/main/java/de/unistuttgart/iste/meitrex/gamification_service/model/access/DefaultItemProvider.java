package de.unistuttgart.iste.meitrex.gamification_service.model.access;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.unistuttgart.iste.meitrex.gamification_service.model.ItemData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

@Component
public class DefaultItemProvider implements IItemProvider {

    private final String path;

    public DefaultItemProvider(
            @Value("${item.file.path}") String path
    ) {
        this.path = Objects.requireNonNull(path);
    }

    @Override
    public ItemData load()
            throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(this.path), ItemData.class);
    }
}
