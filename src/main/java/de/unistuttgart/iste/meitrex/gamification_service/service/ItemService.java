package de.unistuttgart.iste.meitrex.gamification_service.service;

import de.unistuttgart.iste.meitrex.gamification_service.model.ColorTheme;
import de.unistuttgart.iste.meitrex.gamification_service.model.ItemData;
import de.unistuttgart.iste.meitrex.gamification_service.model.PatternTheme;
import de.unistuttgart.iste.meitrex.gamification_service.utility.ItemParser;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
@Transactional
public class ItemService {
    private String filePath = "src/main/resources/itemSchema.json";

    private ItemData items;

    public ItemService() {
        parseItemJson();
    }

    private void parseItemJson() {
        try {
            log.info("Parsing JSON file with path {}", filePath);
            items = ItemParser.parseFromFile(filePath);
            log.info("Finished Parsing JSON file with path {}", filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<ColorTheme> getColorThemes() {
        return items.getColorThemes();
    }
}
